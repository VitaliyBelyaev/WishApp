//
//  WishDetailedView.swift
//  WishApp
//
//  Created by Vitaliy on 07.06.2023.
//

import SwiftUI
import shared

struct WishDetailedView: View {
    
    private let sdk: WishAppSdk?
    @StateObject private var viewModel: WishDetailedViewModel
    
    @State private var isDeleteWishConfirmationPresented = false
    @State private var title = ""
    @State private var comment = ""
    
    
    init(sdk: WishAppSdk? = nil, previewViewModel: WishDetailedViewModel? = nil) {
        
        let viewModel: WishDetailedViewModel
        
        if previewViewModel != nil{
            viewModel = previewViewModel!
        } else {
            viewModel = WishDetailedViewModel(sdk: sdk)
        }
        _viewModel = StateObject(wrappedValue: viewModel)
        self.sdk = sdk
    }
    
    
    var body: some View {
        VStack(alignment: .leading) {
            Form {
//                TextField("Title", text: $title, axis: .vertical)
//                    .font(.title2)
//                    .lineLimit(5)
//
                Section {
                    TextField("Title", text: $title, axis: .vertical)
                        .font(.title2)
                        .lineLimit(5)
                      
                } header: {
                    Text("Comment")

                }
                
                Section {
                    TextField("",text: $comment, axis: .vertical)
                        .lineLimit(5)
                } header: {
                    Text("Comment")

                }
            }
            
//            Spacer()
            
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItemGroup(placement: .secondaryAction) {
                Button(role: .destructive) {
                    isDeleteWishConfirmationPresented = true
                } label: {
                    Label("delete", systemImage: "trash")
                }
                .confirmationDialog("delete Ipad", isPresented: $isDeleteWishConfirmationPresented, titleVisibility: .visible) {
                    
                    Button("delete", role: .destructive) {
                        
                    }
                }
            }
            ToolbarItemGroup(placement: .bottomBar) {
                Spacer()
                NavigationLink(destination: WishListView(sdk: nil, mode: .Completed)) {
                    Image(systemName: "tag")
                }
            }
        }
    }
}

struct WishDetailedView_Previews: PreviewProvider {
    static var previews: some View {
        WishDetailedView()
    }
}
