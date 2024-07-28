//
//  DBICloudFilesManager.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 28/7/24.
//

import Foundation
import shared

final class DBICloudFilesManager {
    
    private let localBackupDbName = "backup.db"
    private let localBackupsDirName = "local_backups"
    
    
    func crateBackup(
        originDbName: String
    ) throws {
        
        print("Start create backup")
        
        let originFileUrl: URL = getDatabaseUrlWithAppending(originDbName)
        let originFileShmUrl: URL = getDatabaseUrlWithAppending(getShmFileName(originDbName))
        let originFileWalUrl: URL = getDatabaseUrlWithAppending(getWalFileName(originDbName))
        
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
            let backupFileUrl: URL = containerUrl.appendingPathComponent(localBackupDbName)
            let backupFileShmUrl: URL = containerUrl.appendingPathComponent(getShmFileName(localBackupDbName))
            let backupFileWalUrl: URL = containerUrl.appendingPathComponent(getWalFileName(localBackupDbName))
            
            // Copy files to iCloud
            do {
                print("Try to copy backup from app to iCloud storage")
                try FileManager.default.copyItem(at: originFileUrl, to: backupFileUrl)
                try FileManager.default.copyItem(at: originFileShmUrl, to: backupFileShmUrl)
                try FileManager.default.copyItem(at: originFileWalUrl, to: backupFileWalUrl)
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
            let backupFileUrl: URL = containerUrl.appendingPathComponent(localBackupDbName)
            let backupFileShmUrl: URL = containerUrl.appendingPathComponent(getShmFileName(localBackupDbName))
            let backupFileWalUrl: URL = containerUrl.appendingPathComponent(getWalFileName(localBackupDbName))
            
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
            logDirContents()
            
        } else {
            let error = ResoreBackupError.iCloudContainerNotExists
            print("\(error)")
            throw error
        }
    }
    
    func isBackupExistsInICloud() -> Bool {
        if let containerUrl: URL = FileManager.default.getAppContainerUrlInICloud() {
            
            let backupFileUrl: URL = containerUrl.appendingPathComponent(localBackupDbName)
            let backupFileShmUrl: URL = containerUrl.appendingPathComponent(getShmFileName(localBackupDbName))
            let backupFileWalUrl: URL = containerUrl.appendingPathComponent(getWalFileName(localBackupDbName))
            
            return FileManager.default.fileExists(atPath: backupFileUrl.path) &&
                FileManager.default.fileExists(atPath: backupFileShmUrl.path) &&
                FileManager.default.fileExists(atPath: backupFileWalUrl.path)
        } else {
            return false
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
        return URL.applicationSupportDirectory.appendingPathComponent("databases/\(component)")
    }
    
    private func isFileExists(path: String) -> Bool {
        var isDir: ObjCBool = false
        return FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
    }
    
    private func logDirContents() {
        do {
            
            let suppDirUrl = URL.applicationSupportDirectory
            
            let suppItems = try FileManager.default.contentsOfDirectory(atPath: suppDirUrl.path)
            
            for supItem in suppItems {
                print("Found support item: \(supItem)")
            }
            
            let localBackupsDirUrl = URL.applicationSupportDirectory.appendingPathComponent(localBackupsDirName)
            let localBackupsItems = try FileManager.default.contentsOfDirectory(atPath: localBackupsDirUrl.path)
            for backupItem in localBackupsItems {
                print("Found local backup item: \(backupItem)")
                
                if backupItem == "backup.db" {
                    do {
                        let fileUrl = localBackupsDirUrl.appendingPathComponent(backupItem)
                        
                        let map = try FileManager.default
                            .attributesOfItem(atPath: fileUrl.path)
                            .sorted() { $0.key.rawValue < $1.key.rawValue }
                        
                        map.forEach { (key: FileAttributeKey, value: Any) in
                            print("local backup file attr, key: \(key), value: \(value)")
                        }
                    } catch {
                        
                    }
                }
                
                
            }
            
            let dbDirUrl = URL.applicationSupportDirectory.appendingPathComponent("databases")
            let dbItems = try FileManager.default.contentsOfDirectory(atPath: dbDirUrl.path)
            for dbItem in dbItems {
                
                print("Found db item: \(dbItem)")
                
                if dbItem == "ru_vitaliy_belyaev_wishapp.db" {
                    do {
                        let dbFileUrl = dbDirUrl.appendingPathComponent(dbItem)
                        
                        let map = try FileManager.default
                            .attributesOfItem(atPath: dbFileUrl.path)
                            .sorted() { $0.key.rawValue < $1.key.rawValue }
                        
                        
                        map.forEach { (key: FileAttributeKey, value: Any) in
                            print("dbFileUrl attr, key: \(key), value: \(value)")
                        }
                        
                    } catch {
                        
                    }
                }
            }
        } catch {
            // failed to read directory – bad permissions, perhaps?
        }
    }
}

