//
//  MainContentView.swift
//  WishApp
//
//  Created by Vitaliy on 12.06.2023.
//

import SwiftUI
import shared

struct MainContentView: View {
    
    let state: MainViewState
    let onRenameTagConfirmed: (TagEntity, String) -> ()
    let onDeleteTagClicked: (TagEntity) -> ()
    let onSettingsClicked: () -> ()
    
    let onAddTestTagClicked: () -> ()
    
    
    var body: some View {
        List {
            ForEach(state.commonItems, id: \.self) { item in
                NavigationLink(value: MainNavSegment.createFromCommonMainItem(item: item)) {
                    HStack {
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
                ForEach(state.tagItems, id: \.tag.id) { item in
                    NavigationLink(value: MainNavSegment.createFromWishTagMainItem(item: item)) {
                        TagItemView(
                            item: item,
                            onRenameConfirmed: { tag, newTitle in
                                onRenameTagConfirmed(tag, newTitle)
                            },
                            onDeleteClicked: { tag in
                                onDeleteTagClicked(tag)
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
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            
            ToolbarItem(placement: .primaryAction) {
                Button("+tag"){
                    onAddTestTagClicked()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button {
                    onSettingsClicked()
                } label: {
                    Image(systemName: "gearshape")
                }
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                Spacer()
                NavigationLink(value: MainNavSegment.createFromWishId(id: nil)) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
        .navigationDestination(for: MainNavSegment.self) { segment in
            switch segment {
            case let .WishList(mode):
                WishListView(mode: mode)
            case let .WishDetailed(wishId):
                WishDetailedView(wishId: wishId)
            }
        }
    }
}

struct MainContentView_Previews: PreviewProvider {
    
    private static var state = MainViewState(
        commonItems: [CommonMainItem(type: .All, count: 13),
                      CommonMainItem(type: .Completed, count: 3)
                     ],
        tagItems: [ WishTagMainItem(tag: TagEntity(id: "1", title: "Birthday"), count: 3),
                    WishTagMainItem(tag: TagEntity(id: "2", title: "Техника"), count: 5),
                  ]
    )
    
    static var previews: some View {
        NavigationStack {
            MainContentView(
                state: state,
                onRenameTagConfirmed: {_,_ in},
                onDeleteTagClicked: {_ in},
                onSettingsClicked: {},
                onAddTestTagClicked: {}
            )
        }
    }
}
