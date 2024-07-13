//
//  DBFilesManager.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 23/6/24.
//

import Foundation
import shared

final class DBFilesManager {
    
    private let localBackupDbName = "backup.db"
    private let localBackupsDirName = "local_backups"
    
    
    func crateBackup(
        deleteExisting: Bool,
        originDbName: String
    ) {
        
        print("Start create backup")
        
        let originFileUrl: URL = getDatabaseUrlWithAppending(originDbName)
        let originFileShmUrl: URL = getDatabaseUrlWithAppending(getShmFileName(originDbName))
        let originFileWalUrl: URL = getDatabaseUrlWithAppending(getWalFileName(originDbName))
        
        let localBackupDirUrl: URL = URL.applicationSupportDirectory.appendingPathComponent(localBackupsDirName)
        FileManager.default.createDirIfNotExists(dirUrl: localBackupDirUrl)
        let backupFileUrl = localBackupDirUrl.appendingPathComponent(localBackupDbName)
        let backupFileShmUrl = localBackupDirUrl.appendingPathComponent(getShmFileName(localBackupDbName))
        let backupFileWalUrl = localBackupDirUrl.appendingPathComponent(getWalFileName(localBackupDbName))
        
        if deleteExisting && isFileExists(path: backupFileUrl.path) {
            do {
                try FileManager.default.removeItem(at: backupFileUrl)
                try FileManager.default.removeItem(at: backupFileShmUrl)
                try FileManager.default.removeItem(at: backupFileWalUrl)
            }
            catch {
                print("Error remove backup from local backupFileUrl: \(backupFileUrl)")
            }
        }
        
        do {
            print("Try to copy backup from app to local storage")
            try FileManager.default.copyItem(at: originFileUrl, to: backupFileUrl)
            try FileManager.default.copyItem(at: originFileShmUrl, to: backupFileShmUrl)
            try FileManager.default.copyItem(at: originFileWalUrl, to: backupFileWalUrl)
            print("copy backup from app to local storage done")
        }
        catch {
            //Error handling
            print("Error in copy item:\(error.localizedDescription)")
        }
        
        print("End create backup")
        logDirContents()
    }
    
    func restoreBackup(
        originDbName: String,
        sdk: WishAppSdk
    ) {
        print("Start restore backup")
        
        // Backup files urls in separate dir
        let localBackupDirUrl: URL = URL.applicationSupportDirectory.appendingPathComponent(localBackupsDirName)
        FileManager.default.createDirIfNotExists(dirUrl: localBackupDirUrl)
        let backupFileUrl = localBackupDirUrl.appendingPathComponent(localBackupDbName)
        let backupFileShmUrl = localBackupDirUrl.appendingPathComponent(getShmFileName(localBackupDbName))
        let backupFileWalUrl = localBackupDirUrl.appendingPathComponent(getWalFileName(localBackupDbName))
        
        // Backup files urls in app databases dir
        let backupFileUrlInDbDir = getDatabaseUrlWithAppending(localBackupDbName)
        let backupFileUrlInDbDirShm = getDatabaseUrlWithAppending(getShmFileName(localBackupDbName))
        let backupFileUrlInDbDirWal = getDatabaseUrlWithAppending(getWalFileName(localBackupDbName))
        
        do {
            // Copy files from separate dir to app databases die
            try FileManager.default.copyItem(at: backupFileUrl, to: backupFileUrlInDbDir)
            try FileManager.default.copyItem(at: backupFileShmUrl, to: backupFileUrlInDbDirShm)
            try FileManager.default.copyItem(at: backupFileWalUrl, to: backupFileUrlInDbDirWal)
            
            // copy contents of backup db to current db
            let isSuccess = sdk.doCopyContentFromBackupDatabase(backupDbName: localBackupDbName)
            print("Copy contents of backup db to current db done, isSuccess:\(isSuccess)")
            
            
            // Remove backup files from app databases dir
            try FileManager.default.removeItem(at: backupFileUrlInDbDir)
            try FileManager.default.removeItem(at: backupFileUrlInDbDirWal)
            try FileManager.default.removeItem(at: backupFileUrlInDbDirShm)
        } catch let error{
            //Error handling
            print("Error in resotre backup:\(error)")
        }
        
        print("End restore backup")
        logDirContents()
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
