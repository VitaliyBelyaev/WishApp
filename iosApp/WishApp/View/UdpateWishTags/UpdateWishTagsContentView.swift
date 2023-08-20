//
//  UpdateWishTagsContentView.swift
//  WishApp
//
//  Created by Vitaliy on 24.07.2023.
//

import SwiftUI
import SwiftUIFlowLayout

struct UpdateWishTagsContentView: View {
    
    @AppStorage(wrappedValue: 0, UserDefaultsKeys.positiveActionsCount)
    private var positiveActionsCount: Int
    
    let query: Binding<String>
    let state: ScreenState
    
    let onCreateTagClicked: (String) -> ()
    let onTagSelectedChanged: (TagItem) -> ()
    let onCloseClicked: () -> ()
    
    var body: some View {
        List {
            if let createItem = state.createItem {
                Button {
                    onCreateTagClicked(createItem.title)
                    positiveActionsCount += 1
                } label: {
                    HStack {
                        Image(systemName: "plus")
                            .foregroundColor(.accentColor)
                        Text("Create \"\(createItem.title)\"")
                        Spacer()
                    }
                }
            }
            
            let tagItems = state.tagItems
            
            if !tagItems.isEmpty {
                FlowLayout(mode: .scrollable, items: tagItems, itemSpacing: 4) { tagItem in
                    let isOn: Binding<Bool> = Binding(
                        get: { return tagItem.isSelected },
                        set: {value, tr in onTagSelectedChanged(tagItem) }
                    )
                    
                    Toggle(isOn: isOn) {
                        Text(tagItem.tag.title)
                    }
                    .toggleStyle(.button)
                    .buttonStyle(.bordered)
                    .foregroundColor(isOn.wrappedValue ? .primary.opacity(0.8) : .gray)
                }
            }
        }
        .listStyle(.grouped)
        .navigationTitle("")
        .toolbar {
            ToolbarItem(placement: .primaryAction){
                Button("done") {
                    onCloseClicked()
                }
            }
        }
        .searchable(text: query, placement: .sidebar, prompt: "UpdateWishTags.prompt")
    }
}

struct UpdateWishTagsContentView_Previews: PreviewProvider {
    static var previews: some View {
        UpdateWishTagsContentView(
            query: Binding(get: { return ""}, set: {_ in }), state: ScreenState(createItem: nil, tagItems: []), onCreateTagClicked: {_ in}, onTagSelectedChanged: {_ in}, onCloseClicked: {}
        )
    }
}
