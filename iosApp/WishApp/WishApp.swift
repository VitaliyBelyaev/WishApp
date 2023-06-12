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
    
    //    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    init() {
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}

//class AppDelegate: NSObject, UIApplicationDelegate {
//    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
//        KoinKt.doInitKoin()
//        return true
//    }
//}
