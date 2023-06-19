//
//  WishListView.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import SwiftUI
import shared

struct WishListView: View {
    
    @EnvironmentObject private var navigationModel: NavigationModel
    @Environment(\.editMode) private var editMode: Binding<EditMode>?
    @EnvironmentObject private var appViewModel: AppViewModel
    @StateObject private var viewModel: WishListViewModel
    @State private var isSettingsPresented: Bool = false
    private let mode: WishListMode
    
    init(mode: WishListMode) {
        self.mode = mode
        _viewModel = StateObject.init(wrappedValue: { WishListViewModel(mode: mode) }())
    }
    
    var body: some View {
        WishListContentView(
            mode: mode,
            wishes: viewModel.wishes,
            title: viewModel.title,
            shareText: viewModel.shareText,
            onSettingsClicked: { navigationModel.isSettingPresented = true },
            onWishTagClicked: { wishId in navigationModel.navigateToWishDetailed(wishId: wishId) },
            onDeleteWishConfirmed: { wishId in viewModel.onDeleteWishConfirmed(wishId: wishId)},
            onWishCompletnessChanged: { wishId, newIsCompleted in
                appViewModel.onWishCompletnessChangeButtonClicked(
                    wishId: wishId,
                    newIsCompleted: newIsCompleted
                )
            },
            onMove: { indexSet, beforeIndex in viewModel.onMove(indexSet, beforeIndex) },
            onAddTestWishClicked: { viewModel.onAddWishClicked() }
        )
        .sheet(isPresented: $navigationModel.isSettingPresented) {
            SettingsView {
                navigationModel.isSettingPresented = false
            }
        }
    }
}

struct WishListView_Previews: PreviewProvider {
    
    static var previews: some View {
        WishListView(mode: .All)
    }
}
