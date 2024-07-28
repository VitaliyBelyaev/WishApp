//
//  DBICloudFilesManager.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 28/7/24.
//

import Foundation
import shared
import DeviceKit

final class DBICloudFilesManager {
    
    private let localBackupDbName = "backup.db"
    private let localBackupsDirName = "local_backups"
    private let backupMetaInfoFileName = "backup_meta_info.json"
    
    private let jsonDecoder = JSONDecoder()
    private let jsonEncoder = JSONEncoder()
    
    func crateBackup(
        originDbName: String
    ) throws {
        
        print("Start create backup")
        
        let originFileUrl: URL = getDatabaseUrlWithAppending(originDbName)
        let originFileShmUrl: URL = getDatabaseUrlWithAppending(getShmFileName(originDbName))
        let originFileWalUrl: URL = getDatabaseUrlWithAppending(getWalFileName(originDbName))
        
        let updateTimestamp: Int64 = Int64(Date.now.timeIntervalSince1970)
        let deviceName: String = getDeviceNameString()
        let metaInfo: BackupMetaInfo = BackupMetaInfo(updateTimestamp: updateTimestamp, deviceNameString: deviceName)
        
        if let containerUrl: URL = FileManager.default.getAppContainerUrlInICloud() {
            
            // Remove whole dir with contents in iCloud
            do {
                try FileManager.default.removeItem(at: containerUrl)
            } catch {
                let customError = CreateBackupError.removeOldBackupInICloudError(error)
                print("\(customError.localizedDescription)")
                throw customError
            }
            
            // Create dir
            FileManager.default.createDirIfNotExists(dirUrl: containerUrl)
            
            // Create files URLs
            let backupFileUrl: URL = containerUrl.getURLWithAppendingPath(localBackupDbName)
            let backupFileShmUrl: URL = containerUrl.getURLWithAppendingPath(getShmFileName(localBackupDbName))
            let backupFileWalUrl: URL = containerUrl.getURLWithAppendingPath(getWalFileName(localBackupDbName))
            let backupMetaInfoUrl: URL = containerUrl.getURLWithAppendingPath(backupMetaInfoFileName)
            
            // Copy files to iCloud
            do {
                print("Try to copy backup from app to iCloud storage")
                try FileManager.default.copyItem(at: originFileUrl, to: backupFileUrl)
                try FileManager.default.copyItem(at: originFileShmUrl, to: backupFileShmUrl)
                try FileManager.default.copyItem(at: originFileWalUrl, to: backupFileWalUrl)
                
                let metaInfoData: Data = try jsonEncoder.encode(metaInfo)
                
                
                try metaInfoData.write(to: backupMetaInfoUrl, options: [.atomic, .completeFileProtection])
                
                
                
                print("copy backup from app to iCloud storage done")
            }
            catch {
                let customError = CreateBackupError.copyNewBackuptoICloudError(error)
                print("\(customError.localizedDescription)")
                throw customError
            }
            
            print("End create backup")
        } else {
            let error = CreateBackupError.iCloudContainerNotExists
            print("\(error)")
            throw error
        }
    }
    
    func restoreBackup(
        originDbName: String,
        sdk: WishAppSdk
    ) throws {
        print("Start restore backup")
        
        if let containerUrl: URL = FileManager.default.getAppContainerUrlInICloud() {
            
            // Backup files urls in iCloud dir
            let backupFileUrl: URL = containerUrl.getURLWithAppendingPath(localBackupDbName)
            let backupFileShmUrl: URL = containerUrl.getURLWithAppendingPath(getShmFileName(localBackupDbName))
            let backupFileWalUrl: URL = containerUrl.getURLWithAppendingPath(getWalFileName(localBackupDbName))
            
            // Backup temp files urls in app databases dir
            let backupFileUrlInDbDir = getDatabaseUrlWithAppending(localBackupDbName)
            let backupFileUrlInDbDirShm = getDatabaseUrlWithAppending(getShmFileName(localBackupDbName))
            let backupFileUrlInDbDirWal = getDatabaseUrlWithAppending(getWalFileName(localBackupDbName))
            
            let tempDBFilesUrls = [backupFileUrlInDbDir, backupFileUrlInDbDirShm, backupFileUrlInDbDirWal]
            
            do {
                // Copy files from iCloud dir to app databases dir
                try FileManager.default.copyItem(at: backupFileUrl, to: backupFileUrlInDbDir)
                try FileManager.default.copyItem(at: backupFileShmUrl, to: backupFileUrlInDbDirShm)
                try FileManager.default.copyItem(at: backupFileWalUrl, to: backupFileUrlInDbDirWal)
                
                // copy contents of backup db to current db
                let isSuccess = sdk.doCopyContentFromBackupDatabase(backupDbName: localBackupDbName)
                print("Copy contents of backup db to current db done, isSuccess:\(isSuccess)")
                
                if isSuccess {
                    // Remove temp backup files from app databases dir
                    removeFiles(urls: tempDBFilesUrls)
                } else {
                    throw ResoreBackupError.restoreCopyContentsError
                }
            } catch {
                // Remove temp backup files from app databases dir
                removeFiles(urls: tempDBFilesUrls)
                
                let customError = ResoreBackupError.restoreGeneralError(error)
                print("\(customError)")
                throw customError
            }
            
            print("End restore backup")
        } else {
            let error = ResoreBackupError.iCloudContainerNotExists
            print("\(error)")
            throw error
        }
    }
    
    func getBackupData() -> BackupData? {
        if let containerUrl: URL = FileManager.default.getAppContainerUrlInICloud() {
            
            let backupFileUrl: URL = containerUrl.getURLWithAppendingPath(localBackupDbName)
            let backupFileShmUrl: URL = containerUrl.getURLWithAppendingPath(getShmFileName(localBackupDbName))
            let backupFileWalUrl: URL = containerUrl.getURLWithAppendingPath(getWalFileName(localBackupDbName))
            
            let backupMetaInfoUrl: URL = containerUrl.getURLWithAppendingPath(backupMetaInfoFileName)
            
            do {
                let backupFileAttrs = try FileManager.default.attributesOfItem(atPath: backupFileUrl.path) as NSDictionary
                let backupFileShmAttrs = try FileManager.default.attributesOfItem(atPath: backupFileShmUrl.path) as NSDictionary
                let backupFileWalAttrs = try FileManager.default.attributesOfItem(atPath: backupFileWalUrl.path) as NSDictionary
                
                let totalSizeBytes: UInt64 = backupFileAttrs.fileSize() + backupFileShmAttrs.fileSize() + backupFileWalAttrs.fileSize()
                let totalSizeString: String = ByteCountFormatter.string(fromByteCount: Int64(totalSizeBytes), countStyle: .file)
                
                guard let metaInfoData: Data = FileManager.default.contents(atPath: backupMetaInfoUrl.path) else {
                    return nil
                }
                
                let metaInfo: BackupMetaInfo = try jsonDecoder.decode(BackupMetaInfo.self, from: metaInfoData)
                let modificationDate: Date = Date(timeIntervalSince1970: TimeInterval(metaInfo.updateTimestamp))
                
                return BackupData(
                    updateDate: modificationDate,
                    sizeFormattedString: totalSizeString,
                    deviceName: metaInfo.deviceNameString
                )
            } catch {
                return nil
            }
        } else {
            return nil
        }
    }
    
    private func getDeviceNameString() -> String {
        let device = Device.current
        return if let name = device.name {
            name
        } else {
            device.safeDescription
        }
    }
    
    private func removeFiles(urls: [URL]) {
        do {
            try urls.forEach { url in try FileManager.default.removeItem(at: url) }
        } catch {
            print("Error removing temp files:\(urls), error: \(error)")
        }
    }
    
    private func getWalFileName(_ baseName: String) -> String {
        return "\(baseName)-wal"
    }
    
    private func getShmFileName(_ baseName: String) -> String {
        return "\(baseName)-shm"
    }
    
    
    private func getDatabaseUrlWithAppending(_ component: String) -> URL {
        return URL.applicationSupportDirectory.getURLWithAppendingPath("databases/\(component)")
    }
    
    private func isFileExists(path: String) -> Bool {
        var isDir: ObjCBool = false
        return FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
    }
}

