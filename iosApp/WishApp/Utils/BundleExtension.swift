//
//  BundleExtension.swift
//  WishApp
//
//  Created by Vitaliy on 22.06.2023.
//

import Foundation

extension Bundle {
    
    var releaseVersionNumber: String? {
        return infoDictionary?["CFBundleShortVersionString"] as? String
    }
    var buildVersionNumber: String? {
        return infoDictionary?["CFBundleVersion"] as? String
    }
    
    var appName: String {
        return infoDictionary?["CBBundleDisplayName"] as? String ?? "Wishapp"
    }
    
    var appVersionPretty: String {
        let releaseNumer = releaseVersionNumber ?? "1.0.0"
        let buildNumber = buildVersionNumber ?? "1"
        return "\(releaseNumer) (\(buildNumber))"
    }
}
