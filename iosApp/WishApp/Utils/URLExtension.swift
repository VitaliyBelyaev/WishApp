//
//  URLExtension.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 28/7/24.
//

import Foundation


extension URL {
    
    public func getURLWithAppendingPath(_ path: String) -> URL {
        return self.appending(path: path)
        
//        self.appendingPathComponent(path)
    }
}
