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
    
    @StateObject private var appViewModel: AppViewModel
    
    init() {
        KoinKt.doInitKoin()
        
        _appViewModel = StateObject(wrappedValue: { AppViewModel() }())
    }
    
    
    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(appViewModel)
        }
    }
}

//class AppDelegate: NSObject, UIApplicationDelegate {
//    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
//        KoinKt.doInitKoin()
//        return true
//    }
//}
