//
//  WishDetailedView.swift
//  WishApp
//
//  Created by Vitaliy on 07.06.2023.
//

import SwiftUI
import shared
import Combine
import SwiftUIFlowLayout
import SwiftUIIntrospect

struct WishDetailedView: View {
    
    @EnvironmentObject private var appViewModel: AppViewModel
    @EnvironmentObject private var navigationModel: NavigationModel
    @StateObject private var viewModel: WishDetailedViewModel
    @State private var isDeleteWishConfirmationPresented = false
    @State private var isUpdateWishTagsSheetPresented = false
    @State private var becomeFirstResponder = true
    
    private let isNewWish: Bool
    
    init(wishId: String?, tagId: String?) {
        _viewModel = StateObject.init(wrappedValue: { WishDetailedViewModel(wishId: wishId, tagId: tagId) }())
        
        self.isNewWish = wishId == nil
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            Form {
                Section {
                    TextField("", text: $viewModel.title, axis: .vertical)
                        .font(.title2)
                        .lineLimit(4)
                        .strikethrough(viewModel.wish.isCompleted)
                        .onChange(of: viewModel.title) { viewModel.onTitleChanged(to: $0) }
                        .introspect(.textField(axis: .vertical), on: .iOS(.v16, .v17)) { textField in
                            if self.becomeFirstResponder && isNewWish {
                                textField.becomeFirstResponder()
                                self.becomeFirstResponder = false
                            }
                        }
                } header: {
                    Text("WishDetailed.title")
                }
                
                Section {
                    TextField("",text: $viewModel.comment, axis: .vertical)
                        .lineLimit(7)
                        .onChange(of: viewModel.comment) { viewModel.onCommentChanged(to: $0) }
                } header: {
                    Text("WishDetailed.comment")
                }
                
                Section {
                    HStack {
                        TextField("", text: $viewModel.link)
                            .lineLimit(1)
                            .keyboardType(.URL)
                            .textInputAutocapitalization(.never)
                        Spacer()
                        Button {
                            WishAppAnalytics.logEvent(WishDetailedAddLinkButtonClickedEvent())
                            viewModel.onNewLinkAddClicked(link: viewModel.link)
                            viewModel.link = ""
                        } label: {
                            Image(systemName: "plus.app")
                        }
                        .disabled(!viewModel.isAddLinkButtonEnabled)
                    }
                    
                    ForEach(viewModel.linksInfos.reversed(), id: \.self) { linkInfo in
                        LinkView(
                            linkInfo: linkInfo,
                            onDeleteConfirmed: { viewModel.onDeleteLinkConfirmed(link: $0) }
                        )
                    }
                    
                } header: {
                    Text("WishDetailed.links")
                }
                
                Section {
                    Button {
                        WishAppAnalytics.logEvent(WishDetailedAddTagTextButtonClickedEvent())
                        isUpdateWishTagsSheetPresented = true
                    } label: {
                        HStack {
                            Image(systemName: "plus")
                            Text("WishDetailed.addTags")
                        }
                    }
                    
                    if !viewModel.wish.tags.isEmpty {
                        FlowLayout(mode: .scrollable, items: viewModel.wish.tags, itemSpacing: 4) { tag in
                            
                            let isOn: Binding<Bool> = Binding(
                                get: {return true },
                                set: {value, tr in
                                    if !value {
                                        isUpdateWishTagsSheetPresented = true
                                    }
                                }
                            )
                            
                            Toggle(isOn: isOn) {
                                Text(tag.title)
                            }
                            .toggleStyle(.button)
                            .buttonStyle(.bordered)
                            .foregroundColor(isOn.wrappedValue ? .primary.opacity(0.8) : .gray)
                        }
                    }
                } header: {
                    Text("WishDetailed.tags")
                }
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .navigationBarTitleDisplayMode(.inline)
        .navigationTitle("")
        .toolbar {
            ToolbarItemGroup(placement: .secondaryAction) {
                Button {
                    WishAppAnalytics.logEvent(WishDetailedChangeWishCompletenessClickedEvent())
                    appViewModel.onWishCompletnessChangeButtonClicked(
                        wishId: viewModel.wish.id,
                        newIsCompleted: !viewModel.wish.isCompleted
                    )
                    navigationModel.popMainPath()
                } label: {
                    if viewModel.wish.isCompleted {
                        Label("WishDetailed.markUndone", systemImage: "arrow.uturn.backward")
                    } else {
                        Label("WishDetailed.markDone", systemImage: "checkmark")
                    }
                }
                
                Button(role: .destructive) {
                    WishAppAnalytics.logEvent(WishDetailedDeleteWishClickedEvent())
                    isDeleteWishConfirmationPresented = true
                } label: {
                    Label("delete", systemImage: "trash")
                }
                .confirmationDialog("delete \(viewModel.title)", isPresented: $isDeleteWishConfirmationPresented, titleVisibility: .visible) {
                    Button("delete", role: .destructive) {
                        WishAppAnalytics.logEvent(WishDetailedDeleteWishConfirmedEvent())
                        viewModel.onDeleteWish()
                        appViewModel.deleteWish(id: viewModel.wish.id)
                        navigationModel.popMainPath()
                    }
                }
            }
            ToolbarItemGroup(placement: .bottomBar) {
                Spacer()
                Button {
                    WishAppAnalytics.logEvent(WishDetailedAddTagIconButtonClickedEvent())
                    isUpdateWishTagsSheetPresented = true
                } label: {
                    Image(systemName: "tag")
                }
            }
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button {
                    WishAppAnalytics.logEvent(WishDetailedAddTagIconButtonClickedEvent())
                    isUpdateWishTagsSheetPresented = true
                } label: {
                    Image(systemName: "tag")
                }
            }
        }
        .sheet(isPresented: $isUpdateWishTagsSheetPresented) {
            UpdateWishTagsView(
                wishId: viewModel.wish.id,
                onCloseClicked: { isUpdateWishTagsSheetPresented = false }
            )
            .onAppear {
                WishAppAnalytics.logEvent(UpdateWishTagsScreenShowEvent())
            }
        }
        .onAppear {
            logScreenShow()
        }
        .onDisappear {
            if isNewWish {
                appViewModel.onNewWishIdToCheckForDeletionChanged(newWishId: viewModel.wish.id)
            }
        }
    }
    
    private func logScreenShow() {
        var fromNavSegment: MainNavSegment? = nil
        if navigationModel.mainPath.count > 1 {
            fromNavSegment = navigationModel.mainPath[navigationModel.mainPath.count - 2]
        }
        
        let fromScreen: String
        switch fromNavSegment {
        case .none:
            fromScreen = "Main"
        case .some(.WishList(let mode)):
            fromScreen = "Wish List \(mode.analyticsString)"
        case .some(.WishDetailed(_, _)):
            fromScreen = "Wish Detailed"
        }
        
        let event = WishDetailedScreenShowEvent(
            fromScreen: fromScreen,
            isNewWish: isNewWish
        )
    
        WishAppAnalytics.logEvent(event)
    }
}

struct WishDetailedView_Previews: PreviewProvider {
    
    static var previews: some View {
        WishDetailedView(wishId: nil, tagId: nil)
    }
}
