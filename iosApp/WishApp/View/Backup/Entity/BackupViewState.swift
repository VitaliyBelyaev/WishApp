//
//  BackupViewState.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 28/7/24.
//

import Foundation

struct BackupViewState {
    
    let haveBackup: Bool
    
    let backupData: BackupData?
    

    init() {
        self.haveBackup = false
        self.backupData = nil
    }
    
    init(haveBackup: Bool, backupData: BackupData?) {
        self.haveBackup = haveBackup
        self.backupData = backupData
    }
}
