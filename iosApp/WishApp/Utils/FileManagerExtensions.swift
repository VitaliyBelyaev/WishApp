//
//  FileManagerExtensions.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 4/2/24.
//

import Foundation


extension FileManager {
    
    public func getAppContainerUrlInICloud() -> URL? {
        return FileManager.default.url(forUbiquityContainerIdentifier: nil)?.appendingPathComponent("Documents")
    }
    
    public func createDirIfNotExists(dirUrl: URL) {
        if !FileManager.default.fileExists(atPath: dirUrl.path, isDirectory: nil) {
            do {
                try FileManager.default.createDirectory(at: dirUrl, withIntermediateDirectories: true, attributes: nil)
            } catch {
                print(error.localizedDescription)
            }
        }
    }
}
