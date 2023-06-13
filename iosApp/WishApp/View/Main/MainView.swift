//
//  MainView.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import SwiftUI
import shared

struct MainView: View {
    
    @StateObject private var viewModel: MainViewModel
    @State private var isSettingsPresented: Bool = false
    
    init() {
        _viewModel = StateObject(wrappedValue: { MainViewModel() }())
    }
    
    var body: some View {
        MainContentView(
            state: viewModel.state,
            onRenameTagConfirmed: { tag, newTitle in
                viewModel.onRenameTagConfirmed(tag: tag, newTitle: newTitle)
                
            },
            onDeleteTagClicked: { tag in
                viewModel.onDeleteTagClicked(tag: tag)
            },
            onSettingsClicked: { isSettingsPresented = true }
        )
        .sheet(isPresented: $isSettingsPresented) {
            SettingsView {
                isSettingsPresented = false
            }
        }
    }
}

struct MainView_Previews: PreviewProvider {
    
    static var previews: some View {
        MainView()
    }
}
