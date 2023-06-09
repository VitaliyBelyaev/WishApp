//
//  WishListView.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import SwiftUI
import shared

struct WishListView: View {
    
    private let sdk: WishAppSdk?
    @StateObject private var viewModel: WishListViewModel
    
    init(sdk: WishAppSdk? = nil, mode: WishListMode) {
        self.sdk = sdk
        let viewModel = WishListViewModel(sdk: sdk, mode: mode)
        _viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        
        List {
            ForEach(viewModel.wishes, id: \.self) { wish in
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
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("+wish"){
                    viewModel.onAddWishClicked()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                NavigationLink(value: Destitation.Settings) {
                    Image(systemName: "gearshape")
                }
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                NavigationLink(value: Destitation.Share) {
                    Image(systemName: "square.and.arrow.up")
                }
                
                NavigationLink(value: Destitation.NewWish) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
        .navigationDestination(for: WishEntity.self) { wish in
            WishDetailedView(sdk: sdk, wishId: wish.id)
        }
        .navigationDestination(for: Destitation.self) { dest in
            switch dest {
            case .NewWish:
                WishDetailedView(sdk: sdk)
            case .Settings:
                WishListView(sdk:sdk, mode: .All)
            case .Share:
                WishListView(sdk:sdk, mode: .All)
            }
        }
    }
    
    enum Destitation {
        case NewWish
        case Settings
        case Share
    }
}

struct WishListView_Previews: PreviewProvider {
    static var previews: some View {
        WishListView(mode: .All)
    }
}
