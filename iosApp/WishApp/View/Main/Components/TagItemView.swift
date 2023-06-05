//
//  TagItemView.swift
//  WishApp
//
//  Created by Vitaliy on 02.06.2023.
//

import SwiftUI
import shared

struct TagItemView: View {
    
    let item: WishTagMainItem
    let onRenameClicked: (TagEntity) -> Void
    let onDeleteClicked: (TagEntity) -> Void
    let confirmTitle: String
    
    @State private var isDeleteTagConfirmationVisible = false
    
    init(item: WishTagMainItem, onRenameClicked: @escaping (TagEntity) -> Void = {_ in}, onDeleteClicked: @escaping (TagEntity) -> Void = {_ in}) {
        self.item = item
        self.onRenameClicked = onRenameClicked
        self.onDeleteClicked = onDeleteClicked
        
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
                onRenameClicked(item.tag)
            } label: {
                Label("Main.renameTag", systemImage: "pencil")
            }
            Button(role: .destructive) {
               isDeleteTagConfirmationVisible = true
            } label: {
                Label("Main.deleteTag", systemImage: "trash")
            }
        }
        .confirmationDialog(confirmTitle, isPresented: $isDeleteTagConfirmationVisible, titleVisibility: .visible) {
            Button("Main.deleteTag", role: .destructive) {
                onDeleteClicked(item.tag)
            }
        }
    }
}

struct TagItemView_Previews: PreviewProvider {
    static var previews: some View {
        TagItemView(item: WishTagMainItem(tag: TagEntity(id: "1", title: "Tag 1"), count: 13))
    }
}
