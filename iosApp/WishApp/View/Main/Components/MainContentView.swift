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
    @State var isTagsSectionExpanded = true
    
    
    let onAddTestTagClicked: () -> ()
    
    var body: some View {
        List {
            Section {
                ForEach(state.commonItems, id: \.self) { item in
                    NavigationLink(value: MainNavSegment.createFromCommonMainItem(item: item)) {
                        CommonItemView(item: item)
                    }
                }
            }
            
            Section {
                if(isTagsSectionExpanded) {
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
                }
            } header: {
                SectionHeaderView(title: LocalizedStringKey("Main.tags"), isOn: $isTagsSectionExpanded)
            }
        }
        .environment(\.defaultMinListRowHeight, 48)
        .listStyle(.automatic)
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
                NavigationLink(value: MainNavSegment.createFromWishId(id: nil, tagId: nil)) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
        .navigationDestination(for: MainNavSegment.self) { segment in
            switch segment {
            case let .WishList(mode):
                WishListView(mode: mode)
            case let .WishDetailed(wishId, tagId):
                WishDetailedView(wishId: wishId, tagId: tagId)
            }
        }
    }
}

struct MainContentView_Previews: PreviewProvider {
    
    @State private static var state = MainViewState(
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
