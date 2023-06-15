//
//  WishDetailedView.swift
//  WishApp
//
//  Created by Vitaliy on 07.06.2023.
//

import SwiftUI
import shared
import Combine

struct WishDetailedView: View {
    
    @EnvironmentObject private var appViewModel: AppViewModel
    @EnvironmentObject private var navigationModel: NavigationModel
    @StateObject private var viewModel: WishDetailedViewModel
    @State private var isDeleteWishConfirmationPresented = false
    
    init(wishId: String?) {
        _viewModel = StateObject.init(wrappedValue: { WishDetailedViewModel(wishId: wishId) }())
    }
    
    
    var body: some View {
        VStack(alignment: .leading) {
            Form {
                Section {
                    TextField("", text: $viewModel.title, axis: .vertical)
                        .font(.title2)
                        .lineLimit(5)
                        .onChange(of: viewModel.title) { viewModel.onTitleChanged(to: $0) }
                      
                } header: {
                    Text("Title")
                }
                
                Section {
                    TextField("",text: $viewModel.comment, axis: .vertical)
                        .lineLimit(5)
                        .onChange(of: viewModel.comment) { viewModel.onCommentChanged(to: $0) }
                } header: {
                    Text("Comment")

                }
                
                Section {
                    HStack {
                        TextField("",text: $viewModel.link)
                            .lineLimit(1)
                            .keyboardType(.URL)
                            .textContentType(.URL)
                        Spacer()
                        Button {
                            viewModel.onNewLinkAddClicked(link: viewModel.link)
                            viewModel.link = ""
                        } label: {
                            Image(systemName: "plus.app")
                        }
                        .disabled(!viewModel.isAddLinkButtonEnabled)
                    }
                    
                    ForEach(viewModel.wish.links.reversed(), id: \.self) { link in
//                        let mdLink = "[\(link)](\(link))"
                        Text(.init(link))
                    }
                    
                } header: {
                    Text("Links")
                }
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .navigationBarTitleDisplayMode(.inline)
        .navigationTitle("")
        .toolbar {
            ToolbarItemGroup(placement: .secondaryAction) {
                Button(role: .destructive) {
                    isDeleteWishConfirmationPresented = true
                } label: {
                    Label("delete", systemImage: "trash")
                }
                .confirmationDialog("delete \(viewModel.title)", isPresented: $isDeleteWishConfirmationPresented, titleVisibility: .visible) {
                    Button("delete", role: .destructive) {
                        viewModel.onDeleteWish()
                        appViewModel.deleteWish(id: viewModel.wish.id)
                        navigationModel.popMainPath()
                    }
                }
            }
            ToolbarItemGroup(placement: .bottomBar) {
                Spacer()
//                NavigationLink(destination: { WishListView(mode: .All) }) {
                    Image(systemName: "tag")
//                }
            }
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
//                NavigationLink(destination: WishListView(mode: .Completed)) {
                    Image(systemName: "tag")
//                }
            }
        }
    }
}

struct WishDetailedView_Previews: PreviewProvider {
        
    static var previews: some View {
        WishDetailedView(wishId: nil)
    }
}