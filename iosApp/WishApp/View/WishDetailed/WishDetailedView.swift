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
    @State private var title: String
    @State private var comment: String
    @State private var link = ""
    
    
    init(sdk: WishAppSdk? = nil, previewViewModel: WishDetailedViewModel? = nil, wishId: String? = nil) {
        
        let viewModel: WishDetailedViewModel
        
        if previewViewModel != nil {
            viewModel = previewViewModel!
        } else {
            viewModel = WishDetailedViewModel(sdk: sdk, wishId: wishId)
        }
        _viewModel = StateObject(wrappedValue: viewModel)
        self.sdk = sdk
        
        _title = State(initialValue: viewModel.wish.title)
        _comment = State(initialValue: viewModel.wish.comment)
    }
    
    
    var body: some View {
        VStack(alignment: .leading) {
            Form {
//                TextField("Title", text: $title, axis: .vertical)
//                    .font(.title2)
//                    .lineLimit(5)
//
                Section {
                    TextField("", text: $title, axis: .vertical)
                        .font(.title2)
                        .lineLimit(5)
                        .onChange(of: title) { viewModel.onTitleChanged(to: $0) }
                      
                } header: {
                    Text("Title")
                }
                
                Section {
                    TextField("",text: $comment, axis: .vertical)
                        .lineLimit(5)
                        .onChange(of: comment) { viewModel.onCommentChanged(to: $0) }
                } header: {
                    Text("Comment")

                }
                
                Section {
                    HStack {
                        TextField("",text: $link)
                            .lineLimit(1)
                            .keyboardType(.URL)
                            .textContentType(.URL)
                        Spacer()
                        Button {
                            viewModel.onNewLinkAddClicked(link: link)
                            link = ""
                        } label: {
                            Image(systemName: "plus.app")
                        }
                        .disabled(link.isEmpty)
                    }
                    
                    ForEach(viewModel.wish.links.reversed(), id: \.self) { link in
//                        let mdLink = "[\(link)](\(link))"
                        Text(.init(link))
                    }
                    
                } header: {
                    Text("Links")
                }
            }
            
//            Spacer()
            
        }
        .scrollDismissesKeyboard(.interactively)
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
            ToolbarItemGroup(placement: .keyboard) {
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
