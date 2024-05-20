//
//  WishAppApp.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import SwiftUI
import shared
import Combine
//import FirebaseCore
import Amplitude
//import FirebaseCrashlytics

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        KoinKt.doInitKoin()
//        FirebaseApp.configure()
        
        NapierProxyKt().debugBuild()
        
        if let amplitudeApiKey = valueForAPIKey(named: "AMPLITUDE_API_KEY") {
            Amplitude.instance().trackingSessionEvents = true
            Amplitude.instance().initializeApiKey(amplitudeApiKey)
        } else {
            let error = AmplitudeNotInitializedError()
//            Crashlytics.crashlytics().record(error: error)
            print(error)
        }
        
        return true
    }
}

@main
struct WishApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    @AppStorage("navigationData") private var navigationData: Data?
    @AppStorage(wrappedValue: 0, UserDefaultsKeys.positiveActionsCount) private var positiveActionsCount: Int
    @Environment(\.scenePhase) private var scenePhase
    
    @StateObject private var navigationModel = NavigationModel()
    @StateObject private var appViewModel: AppViewModel
    
    private var subscriptions: [AnyCancellable] = []
    
    init() {
        _appViewModel = StateObject(wrappedValue: { AppViewModel() }())
    }
    
    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(appViewModel)
                .environmentObject(navigationModel)
                .onChange(of: navigationModel.mainPath) { [oldMainPath = navigationModel.mainPath] newMainPath in
                    let isOldPathContainsNewWishDetailed = oldMainPath.contains(where: { segment in
                        isWishDetailedForNewWishSegment(segment: segment)
                    })
                    
                    let isNewPathContainsNewWishDetailed = newMainPath.contains(where: { segment in
                        isWishDetailedForNewWishSegment(segment: segment)
                    })
                    
                    if isOldPathContainsNewWishDetailed && !isNewPathContainsNewWishDetailed {
                        appViewModel.onNewWishDetailedScreenExit()
                        positiveActionsCount += 1
                    }
                }
                .task {
                    if let jsonData = navigationData {
                        navigationModel.jsonData = jsonData
                    }
                    for await _ in navigationModel.objectWillChangeSequence {
                        if !isWishDetailedForNewWishSegment(segment: navigationModel.mainPath.last) {
                            navigationData = navigationModel.jsonData
                        }
                    }
                }
        }
    }
    
    private func isWishDetailedForNewWishSegment(segment: MainNavSegment?) -> Bool {
        switch segment {
        case .WishDetailed(.none, .some(_)):
            return true
        case .WishDetailed(.some(_), .some(_)):
            return false
        case .WishDetailed(.some(_), .none):
            return false
        case .WishDetailed(.none, .none):
            return true
        case .WishList(_):
            return false
        case .none:
            return false
        }
    }
}


