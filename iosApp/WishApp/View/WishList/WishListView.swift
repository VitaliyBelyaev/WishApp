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
    
    init(sdk: WishAppSdk? = nil, mode: WishListMode) {
        let viewModel = WishListViewModel(sdk: sdk, mode: mode)
        _viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        
        List(viewModel.wishes) { wish in
            VStack {
                Text(wish.title)
                    .font(.title)
                Text(wish.comment)
                Text(wish.link)
            }
        }
        .navigationTitle(viewModel.title)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("+wish"){
                    viewModel.onAddWishClicked()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                NavigationLink(destination: WishListView(sdk: nil, mode: .All)) {
                    Image(systemName: "gearshape")
                }
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                Button{
                    // share
                } label: {
                    Image(systemName: "square.and.arrow.up")
                }
                
                NavigationLink(destination: WishListView(sdk: nil, mode: .Completed)) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
    }
}

struct WishListView_Previews: PreviewProvider {
    static var previews: some View {
        WishListView(mode: .All)
    }
}
