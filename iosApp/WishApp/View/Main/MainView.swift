//
//  MainView.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import SwiftUI
import shared

struct MainView: View {
    
    @EnvironmentObject private var navigationModel: NavigationModel
    @StateObject private var viewModel: MainViewModel
    @State private var isSettingsPresented: Bool = false
    
    init() {
        _viewModel = StateObject(wrappedValue: { MainViewModel() }())
    }
    
    var body: some View {
        NavigationStack(path: $navigationModel.mainPath) {
            MainContentView(
                state: viewModel.state,
                onRenameTagConfirmed: { tag, newTitle in
                    viewModel.onRenameTagConfirmed(tag: tag, newTitle: newTitle)
                    
                },
                onDeleteTagClicked: { tag in
                    viewModel.onDeleteTagClicked(tag: tag)
                },
                onSettingsClicked: { navigationModel.isSettingPresented = true },
                onAddTestTagClicked: { viewModel.onAddTagClicked() }
            )
            .onAppear {
                logMainScreenShow()
            }
        }
        .sheet(isPresented: $navigationModel.isSettingPresented) {
            SettingsView {
                navigationModel.isSettingPresented = false
            }
            .onAppear {
                WishAppAnalytcis.logEvent(SettingsSheetShowEvent())
            }
        }
    }
    
    private func logMainScreenShow() {
        if(viewModel.state.currentCount == 0){
            return
        }
        
        let event = MainScreenShowEvent(
            currentWishesCount: viewModel.state.currentCount,
            completedWishesCount: viewModel.state.completedCount,
            tagsCount: viewModel.state.tagItems.count
        )
        WishAppAnalytcis.logEvent(event)
    }
}

struct MainView_Previews: PreviewProvider {
    
    static var previews: some View {
        MainView()
    }
}
