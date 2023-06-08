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
    
    init(sdk: WishAppSdk? = nil, previewViewModel: MainViewModel? = nil) {
        
        let viewModel: MainViewModel
        
        if previewViewModel != nil{
            viewModel = previewViewModel!
        } else {
            viewModel = MainViewModel(sdk: sdk)
        }
        _viewModel = StateObject(wrappedValue: viewModel)
        self.sdk = sdk
    }
    
    var body: some View {
        NavigationStack() {
            List {
                ForEach(viewModel.state.commonItems, id: \.self) { item in
                    NavigationLink(value: item) {
                        HStack{
                            switch item.type {
                            case .All:
                                Text("Main.all")
                            case .Completed:
                                Text("Main.completed")
                            }
                            Spacer()
                            Text("\(item.count)")
                        }
                    }
                }
                
                Section {
                    ForEach(viewModel.state.tagItems, id: \.tag.id) { item in
                        NavigationLink(value: item) {
                            TagItemView(
                                item: item,
                                onRenameConfirmed: { tag, newTitle in
                                    viewModel.onRenameTagConfirmed(tag: tag, newTitle: newTitle)
                                },
                                onDeleteClicked: { tag in
                                    viewModel.onDeleteTagClicked(tag: tag)
                                }
                            )
                        }
                    }
                } header: {
                    Text("Main.tags")
                        .font(.title3)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)

                }
                .textCase(nil)
            }
            .listStyle(.sidebar)
            .navigationTitle("Main.title")
            .toolbar {
               
                ToolbarItem(placement: .primaryAction) {
                    Button("+tag"){
                        viewModel.onAddTagClicked()
                    }
                }
                
                ToolbarItem(placement: .primaryAction) {
                    NavigationLink(destination: WishListView(sdk: sdk, mode: .All)) {
                        Image(systemName: "gearshape")
                    }
                }
                
                ToolbarItemGroup(placement: .bottomBar) {
                    Spacer()
                    NavigationLink(destination: WishDetailedView(sdk: sdk)) {
                        Image(systemName: "square.and.pencil")
                    }
                }
            }
            .navigationDestination(for: CommonMainItem.self) { item in
                switch item.type {
                case .All:
                    WishListView(sdk: sdk, mode: .All)
                case .Completed:
                    WishListView(sdk: sdk, mode: .Completed)
                }
            }
            .navigationDestination(for: WishTagMainItem.self) { item in
                WishListView(sdk: sdk, mode: .ByTag(item.tag))
            }
        }
    }
    
    struct MainView_Previews: PreviewProvider {
        
        static var viewModel = MainViewModel()

        static var previews: some View {
            viewModel.state = getPreviewState()
            return MainView(previewViewModel: viewModel)
        }
        
        private static func getPreviewState() -> MainViewState {
            return MainViewState(
                commonItems: [
                    CommonMainItem(type:.All, count: 13),
                    CommonMainItem(type:.Completed, count: 2),
                ],
                tagItems: getTags().map { tag in
                    WishTagMainItem(tag: tag, count: 13)
                }
            )
        }
        
        private static func getTags() -> [TagEntity] {
            var tags = [TagEntity]()
            for i in 0...15 {
                tags.append(TagEntity(id: String(i), title: "Tag \(i)"))
            }
            return tags
        }
    }
}
