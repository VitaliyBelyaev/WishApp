//
//  BackupErrors.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 28/7/24.
//

import Foundation


enum CreateBackupError: Error {
    
    case iCloudContainerNotExists
    
    case removeOldBackupInICloudError(Error?)
    
    case copyNewBackuptoICloudError(Error?)
}


enum ResoreBackupError: Error {
    
    case iCloudContainerNotExists
    
    case restoreCopyContentsError
    
    case restoreGeneralError(Error?)
}
