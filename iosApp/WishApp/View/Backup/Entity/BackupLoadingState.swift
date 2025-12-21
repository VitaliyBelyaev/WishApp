//
//  BackupLoadingState.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 4/8/24.
//

import Foundation

enum BackupLoadingState {
    
    case none
    
    case createBackup
    
    case restoreBackup
    
    case standard
}

extension BackupLoadingState {
    
    func showLoader() -> Bool {
        let result: Bool
        
        switch self {
        case .none:
            result = false
        case .createBackup:
            result = true
        case .restoreBackup:
            result = true
        case .standard:
            result = true
        }
        return result
    }
    
    func loaderText() -> String {
        let result: String
        
        switch self {
        case .none:
            result = ""
        case .createBackup:
            result = "Creating backup..."
        case .restoreBackup:
            result = "Restoring data from backup..."
        case .standard:
            result = "Loading..."
        }
        return result
    }
}
