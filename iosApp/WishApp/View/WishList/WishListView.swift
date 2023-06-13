//
//  WishListView.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import SwiftUI
import shared

struct WishListView: View {
    
    @StateObject private var viewModel: WishListViewModel
    @State private var isSettingsPresented: Bool = false
    
    init(mode: WishListMode) {
        _viewModel = StateObject.init(wrappedValue: { WishListViewModel(mode: mode) }())
    }
    
    var body: some View {
        WishListContentView(
            wishes: viewModel.wishes,
            title: viewModel.title,
            onSettingsClicked: { isSettingsPresented = true },
            onAddTestWishClicked: { viewModel.onAddWishClicked() }
        )
        .sheet(isPresented: $isSettingsPresented) {
            SettingsView {
                isSettingsPresented = false
            }
        }
    }
}

struct WishListView_Previews: PreviewProvider {
    
    static var previews: some View {
        WishListView(mode: .All)
    }
}
