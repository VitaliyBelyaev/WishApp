//
//  UpdateWishTagsContentView.swift
//  WishApp
//
//  Created by Vitaliy on 24.07.2023.
//

import SwiftUI
import SwiftUIFlowLayout
import shared

struct UpdateWishTagsContentView: View {
    
    let query: Binding<String>
    let state: ScreenState
    
    let onCreateTagClicked: (String) -> ()
    let onTagSelectedChanged: (TagItem) -> ()
    
    @AppStorage(wrappedValue: 0, UserDefaultsKeys.positiveActionsCount)
    private var positiveActionsCount: Int
    
    @EnvironmentObject private var navigationModel: NavigationModel
    
    @State private var becomeFirstResponder = true
    @State private var isKeyboardPresented = false
    
    var body: some View {
        
        ScrollView {
            VStack {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(Color(UIColor.placeholderText))
                    
                    TextField("UpdateWishTags.prompt", text: query, axis: .vertical)
                        .lineLimit(1)
                        .introspect(.textField(axis: .vertical), on: .iOS(.v16, .v17)) { textField in
                            if self.becomeFirstResponder {
                                textField.becomeFirstResponder()
                                self.becomeFirstResponder = false
                            }
                        }
                    
                    if !query.wrappedValue.isEmpty {
                        Button {
                            
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(Color(UIColor.systemGray))
                        }
                    }
                    
                    
                    if isKeyboardPresented {
                        Button {
                            hideKeyboard()
                        } label: {
                            Text("Cancel")
                        }.buttonStyle(.borderless)
                    }
                }
                .padding(EdgeInsets(top: 8, leading: 8, bottom: 8, trailing: 10))
                .background(Color(UIColor.systemFill))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                
                
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
                    }.padding(EdgeInsets(top: 8, leading: 0, bottom: 0, trailing: 0))
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
                    .padding(EdgeInsets(top: 8, leading: 0, bottom: 0, trailing: 0))
                }
                Spacer()
            }
            .padding(EdgeInsets(top: 12, leading: 16, bottom: 16, trailing: 16))
            .toolbar {
                ToolbarItem(placement: .primaryAction){
                    Button("done") {
                        navigationModel.popMainPath()
                    }
                }
            }
        }
    }
}

struct UpdateWishTagsContentView_Previews: PreviewProvider {
    static var previews: some View {
        
        let items = [
            TagItem(tag: TagEntity(
                id: "id", title: "Tilte"
            ), isSelected: true),
            TagItem(tag: TagEntity(
                id: "id", title: "Tilte2"
            ), isSelected: false),
            TagItem(tag: TagEntity(
                id: "id", title: "Tilte3"
            ), isSelected: true),
            TagItem(tag: TagEntity(
                id: "id", title: "Tilte4"
            ), isSelected: false)
        ]
        
//        let createItem = CreateTagItem(title: "Create tag")
                let createItem: CreateTagItem? = nil
        
        NavigationStack {
            UpdateWishTagsContentView(
                query: Binding(get: { return ""}, set: {_ in }), state: ScreenState(createItem: createItem, tagItems: items), onCreateTagClicked: {_ in}, onTagSelectedChanged: {_ in}
            )
        }
    }
}
