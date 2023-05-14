//
//  ContentView.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import SwiftUI
import shared

struct ContentView: View {
    
    @StateObject private var viewModel: MainViewModel
    
    init(sdk: WishAppSdk? = nil) {
        let viewModel = MainViewModel(sdk: sdk)
        _viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        
        NavigationView {
            List(viewModel.wishes) { wish in
                VStack {
                    Text(wish.title)
                        .font(.title)
                    Text(wish.comment)
                    Text(wish.link)
                }
            }
            .navigationTitle("Wishapp")
            .toolbar {
                Button("Add wish"){
                    viewModel.onAddWishClicked()
                }
            }
        }

    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

extension WishEntity : Identifiable {
    
}
