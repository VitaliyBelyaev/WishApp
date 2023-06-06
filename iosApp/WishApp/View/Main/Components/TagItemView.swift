//
//  TagItemView.swift
//  WishApp
//
//  Created by Vitaliy on 02.06.2023.
//

import SwiftUI
import shared
import Introspect

struct TagItemView: View {
    
    let item: WishTagMainItem
    let onRenameConfirmed: (TagEntity, String) -> Void
    let onDeleteClicked: (TagEntity) -> Void
    let confirmTitle: String
    
    @State private var isDeleteTagConfirmationPresented = false
    @State private var isRenameTagPopoverPresented = false
    @State private var tagTitle: String = ""
    @State private var becomeFirstResponder = true
    
    init(item: WishTagMainItem, onRenameConfirmed: @escaping (TagEntity, String) -> Void = {_,_ in}, onDeleteClicked: @escaping (TagEntity) -> Void = {_ in}) {
        self.item = item
        self.onRenameConfirmed = onRenameConfirmed
        self.onDeleteClicked = onDeleteClicked
        self.tagTitle = item.tag.title
        
        
        let stringFormat = NSLocalizedString("Tag %@ and %d wishes", comment: "")
        self.confirmTitle = String.localizedStringWithFormat(stringFormat, item.tag.title, item.count)
    }
    
    var body: some View {
        HStack{
            Text("\(item.tag.title)")
            Spacer()
            Text("\(item.count)")
        }
        .contextMenu {
            Button {
                isRenameTagPopoverPresented = true
            } label: {
                Label("Main.renameTag", systemImage: "pencil")
            }
            Button(role: .destructive) {
               isDeleteTagConfirmationPresented = true
            } label: {
                Label("Main.deleteTag", systemImage: "trash")
            }
        }
        .confirmationDialog(confirmTitle, isPresented: $isDeleteTagConfirmationPresented, titleVisibility: .visible) {
            Button("Main.deleteTag", role: .destructive) {
                onDeleteClicked(item.tag)
            }
        }
        .popover(isPresented: $isRenameTagPopoverPresented) {
            ZStack {
                Color(.systemGray6)
                VStack(alignment: .leading) {
                    HStack {
                        Button("Main.cancel") {
                            isRenameTagPopoverPresented = false
                        }
                        Spacer()
                        Text("Main.renameTag")
                            .font(.headline)
                            .lineLimit(1)
                        Spacer()
                        Button("Main.done") {
                            isRenameTagPopoverPresented = false
                            onRenameConfirmed(item.tag, tagTitle)
                        }
                        .font(.headline)
                        .disabled(tagTitle.isEmpty)
                    }.padding()
                    
                    Form {
                        TextField("", text: $tagTitle)
                            .introspectTextField { textField in
                                if self.becomeFirstResponder {
                                    textField.becomeFirstResponder()
                                    self.becomeFirstResponder = false
                                }
                            }
                            .onAppear {
                                UITextField.appearance().clearButtonMode = .whileEditing
                            }
                    }
                }
            }
        }
    }
}

struct TagItemView_Previews: PreviewProvider {
    static var previews: some View {
        TagItemView(item: WishTagMainItem(tag: TagEntity(id: "1", title: "Tag 1"), count: 13))
    }
}
