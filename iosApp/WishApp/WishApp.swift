//
//  WishAppApp.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import SwiftUI
import shared
import Combine

@main
struct WishApp: App {
    
    @AppStorage("navigationData") var navigationData: Data?
    @StateObject private var navigationModel = NavigationModel()
    @StateObject private var appViewModel: AppViewModel
    @Environment(\.scenePhase) private var scenePhase
    
    private var subscriptions: [AnyCancellable] = []
    
    init() {
        KoinKt.doInitKoin()
        
        _appViewModel = StateObject(wrappedValue: { AppViewModel() }())
    }
    
    
    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(appViewModel)
                .environmentObject(navigationModel)
                .task {
                    if let jsonData = navigationData {
                        navigationModel.jsonData = jsonData
                    }
                }
                .onChange(of: scenePhase) { phase in
                    if phase == .background {
                        navigationData = navigationModel.jsonData
                    }
                }
                .onChange(of: navigationModel.mainPath) { [oldMainPath = navigationModel.mainPath] newMainPath in
                    let isOldPathContainsNewWishDetailed = oldMainPath.contains(where: { segment in
                        switch segment {
                        case .WishDetailed(nil):
                            return true
                        case .WishList(_):
                            return false
                        case .WishDetailed(.some(_)):
                            return false
                        }
                    })
                    
                    let isNewPathContainsNewWishDetailed = newMainPath.contains(where: { segment in
                        switch segment {
                        case .WishDetailed(nil):
                            return true
                        case .WishList(_):
                            return false
                        case .WishDetailed(.some(_)):
                            return false
                        }
                    })
                    
                    if isOldPathContainsNewWishDetailed && !isNewPathContainsNewWishDetailed {
                        appViewModel.onNewWishDetailedScreenExit()
                    }
                }
        }
    }
}
