//
//  MainView.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import SwiftUI
import shared

struct MainView: View {
    
    private let sdk: WishAppSdk?
    @StateObject private var viewModel: MainViewModel
    
    init(sdk: WishAppSdk? = nil) {
        let viewModel = MainViewModel(sdk: sdk)
        _viewModel = StateObject(wrappedValue: viewModel)
        self.sdk = sdk
    }
    
    var body: some View {
        NavigationStack() {
            List(viewModel.items, id: \.id) { item in
                NavigationLink(value: item){
                    switch item {
                    case let .AllWishes(count):
                        HStack{
                            Text("All wishes")
                            Spacer()
                            Text("\(count)")
                        }
                    case let .CompletedWishes(count):
                        HStack{
                            Text("Completed wishes")
                            Spacer()
                            Text("\(count)")
                        }
                    case let .WishTag(tag, count):
                        HStack{
                            Text("\(tag.title)")
                            Spacer()
                            Text("\(count)")
                        }
                    case .Settings:
                        HStack{
                            Text("Settings")
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("Wishapp")
            .toolbar {
                Button("+ wish"){
                    viewModel.onAddWishClicked()
                }
                Button("+ tag"){
                    viewModel.onAddTagClicked()
                }
            }
            .navigationDestination(for: MainItem.self) { item in
                switch item {
                case .AllWishes(_):
                    WishListView(sdk: sdk, mode: .All)
                case .CompletedWishes(_):
                    WishListView(sdk: sdk, mode: .Completed)
                case let .WishTag(tag, _):
                    WishListView(sdk: sdk, mode: .ByTag(tag))
                case .Settings:
                    WishListView(sdk: sdk, mode: .All)
                }
            }
        }
    }
    
    struct MainView_Previews: PreviewProvider {
        static var previews: some View {
            MainView()
        }
    }
}
