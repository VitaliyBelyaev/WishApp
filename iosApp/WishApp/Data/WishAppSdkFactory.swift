//
//  WishAppSdkFactory.swift
//  WishApp
//
//  Created by Vitaliy on 11.06.2023.
//

import Foundation
import shared

class WishAppSdkFactory {
    
    static let sdk: WishAppSdk = WishAppSdk(databaseDriveFactory: DatabaseDriverFactory())
}
