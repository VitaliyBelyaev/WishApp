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
        print("WishListView init")
        _viewModel = StateObject.init(wrappedValue: { WishListViewModel(mode: mode) }())
    }
    
    var body: some View {
        List {
            ForEach(viewModel.wishes, id: \.id) { wish in
                NavigationLink(value: wish) {
                    VStack(alignment: .leading) {
                        Text(wish.title).font(.title)
                        Text(wish.comment)
                        Text(wish.link)
                    }
                }
            }
        }
        .navigationTitle(viewModel.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("+wish"){
                    viewModel.onAddWishClicked()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button {
                    isSettingsPresented = true
                } label: {
                    Image(systemName: "gearshape")
                }
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                Button {
                    
                } label: {
                    Image(systemName: "square.and.arrow.up")
                }
                
                NavigationLink(destination: WishDetailedView(wishId: nil)) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
        .navigationDestination(for: WishEntity.self) { wish in
            WishDetailedView(wishId: wish.id)
        }
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
