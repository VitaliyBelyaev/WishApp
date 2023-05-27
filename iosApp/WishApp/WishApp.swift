//
//  WishAppApp.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import SwiftUI
import shared

@main
struct WishApp: App {
    
    let sdk = WishAppSdk(databaseDriveFactory: DatabaseDriverFactory())
    
    var body: some Scene {
        WindowGroup {
            MainView(sdk: sdk)
        }
    }
}
