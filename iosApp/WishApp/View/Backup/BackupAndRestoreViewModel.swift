//
//  BackupAndRestoreViewModel.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 29/3/24.
//

import Foundation
import shared

@MainActor
final class BackupAndRestoreViewModel: ObservableObject {
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    
    init() {
       logDirContents()
    }
    
    func onRestoreClicked() {
        if let containerUrl = FileManager.default.getAppContainerUrlInICloud() {
            
            
            print("containerUrl: \(containerUrl)")
            FileManager.default.createDirIfNotExists(dirUrl: containerUrl)
            
            let cloudFileUrl = containerUrl.appendingPathComponent("wishapp.backup")
            
            print("cloudFileUrl: \(cloudFileUrl)")
            
            
            do {
                let cloudAttr = try FileManager.default.attributesOfItem(atPath: cloudFileUrl.path)
                print("cloudAttr: \(cloudAttr)")
            } catch {
                
            }
            
            var isDir: ObjCBool = false
            if !FileManager.default.fileExists(atPath: cloudFileUrl.path, isDirectory: &isDir) {
                print("backup not exists in iCloud")
                return
            }
            
            let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
            
            print("originFileUrl: \(originFileUrl)")
            
            if FileManager.default.fileExists(atPath: originFileUrl.path, isDirectory: &isDir) {
                do {
                    print("Try to remove originFileUrl: \(originFileUrl)")
                    try FileManager.default.removeItem(at: originFileUrl)
                    
                    print("remove originFileUrl: \(originFileUrl) DONE")
                    logDirContents()
                }
                catch {
                    //Error handling
                    print("Error in remove item at url: \(originFileUrl)")
                }
            }
            
            do {
                print("Try to copy backup from cloud to local")
                try FileManager.default.copyItem(at: cloudFileUrl, to: originFileUrl)
                print("Copy done")
                logDirContents()
                
                sdk.reopenDatabase()
            }
            catch {
                //Error handling
                print("Error in copy item:\(error.localizedDescription)")
            }
        } else {
            print("onRestoreClicked, no app container in iCloud")
        }
    }
    
    func onCreateBackupClicked() {
        if let containerUrl = FileManager.default.getAppContainerUrlInICloud() {
            print("containerUrl: \(containerUrl)")
            
            FileManager.default.createDirIfNotExists(dirUrl: containerUrl)
            
            let cloudFileUrl = containerUrl.appendingPathComponent("wishapp.backup")
            print("cloudFileUrl: \(cloudFileUrl)")
            
            let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
            
            print("originFileUrl: \(originFileUrl)")
            
            var isDir: ObjCBool = false
            
            if FileManager.default.fileExists(atPath: cloudFileUrl.path, isDirectory: &isDir) {
                print("Backup file exists in cloud, cloudFileUrl: \(cloudFileUrl)")
                
                do {
                    print("Try to remove backup from cloud cloudFileUrl: \(cloudFileUrl)")
                    try FileManager.default.removeItem(at: cloudFileUrl)
                }
                catch {
                    //Error handling
                    print("Error remove backup from cloud cloudFileUrl: \(cloudFileUrl)")
                }
            }
            
            do {
                print("Try to copy backup from local to cloud")
                try FileManager.default.copyItem(at: originFileUrl, to: cloudFileUrl)
                print("copy backup from local to cloud done")
            }
            catch {
                //Error handling
                print("Error in copy item:\(error.localizedDescription)")
            }
        } else {
            print("onCreateBackupClicked, no app container in iCloud")
        }
    }
    
    private func logDirContents() {
        do {
            
            let suppDirUrl = URL.applicationSupportDirectory
            
            let suppItems = try FileManager.default.contentsOfDirectory(atPath: suppDirUrl.path)
            
            for supItem in suppItems {
                print("Found support item: \(supItem)")
            }
            
            
            let dbDirUrl = URL.applicationSupportDirectory.appendingPathComponent("databases")
            
            let dbItems = try FileManager.default.contentsOfDirectory(atPath: dbDirUrl.path)
            
            for dbItem in dbItems {
                
                if dbItem == "ru_vitaliy_belyaev_wishapp.db" {
                    do {
                        let dbFileUrl = dbDirUrl.appendingPathComponent(dbItem)
                        
                        let map = try FileManager.default.attributesOfItem(atPath: dbFileUrl.path)
                        
                        print("dbFileUrl attrs: \(map)")
                    } catch {
                        
                    }
                }
                
                print("Found db item: \(dbItem)")
            }
        } catch {
            // failed to read directory – bad permissions, perhaps?
        }
    }
    
    func someExp() {
        let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases")
        do {
            let items = try FileManager.default.contentsOfDirectory(atPath: originFileUrl.path)
            
            for item in items {
                
                
                print("Found \(item)")
            }
        } catch {
            // failed to read directory – bad permissions, perhaps?
        }
        
        
        
        // Рабочий код
        //        if let containerUrl = FileManager.default.getAppContainerUrlInICloud() {
        //            FileManager.default.createDirIfNotExists(dirUrl: containerUrl)
        //
        //            let fileUrl = containerUrl.appendingPathComponent("wishapp.backup")
        //            let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
        //
        //            var isDir:ObjCBool = false
        //
        //            if FileManager.default.fileExists(atPath: fileUrl.path, isDirectory: &isDir) {
        //                do {
        //                    try FileManager.default.removeItem(at: fileUrl)
        //                }
        //                catch {
        //                    //Error handling
        //                    print("Error in remove item")
        //                }
        //            }
        //
        //            do {
        //                try FileManager.default.copyItem(at: originFileUrl, to: fileUrl)
        //                print("Copy done")
        //            }
        //            catch {
        //                //Error handling
        //                print("Error in copy item:\(error.localizedDescription)")
        //            }
        //        }
        
        
        //
        //        let data = Data("Test Message12131313131342fesrfsrgf".utf8)
        //        let fileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
        //        print(fileUrl.path())
        
        
        //        do {
        //            let items = try FileManager.default.contentsOfDirectory(atPath: fileUrl.path)
        //
        //            for item in items {
        //
        //
        //                print("Found \(item)")
        //            }
        //        } catch {
        //            // failed to read directory – bad permissions, perhaps?
        //        }
        
        
        
        
        //        do {
        //            try data.write(to: fileUrl, options: [.atomic, .completeFileProtection])
        //            let input = try String(contentsOf: fileUrl)
        //            print(input)
        //        } catch {
        //            print(error.localizedDescription)
        //        }
        
    }
}
