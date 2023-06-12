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
        print("MainView init")
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
    
    //    static var viewModel = MainViewModel()
    
    static var previews: some View {
        //        viewModel.state = getPreviewState()
        return MainView()
    }
    
    //    private static func getPreviewState() -> MainViewState {
    //        return MainViewState(
    //            commonItems: [
    //                CommonMainItem(type:.All, count: 13),
    //                CommonMainItem(type:.Completed, count: 2),
    //            ],
    //            tagItems: getTags().map { tag in
    //                WishTagMainItem(tag: tag, count: 13)
    //            }
    //        )
    //    }
    //
    //    private static func getTags() -> [TagEntity] {
    //        var tags = [TagEntity]()
    //        for i in 0...15 {
    //            tags.append(TagEntity(id: String(i), title: "Tag \(i)"))
    //        }
    //        return tags
    //    }
}
