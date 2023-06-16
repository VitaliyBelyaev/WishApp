//
//  LinkView.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import SwiftUI

struct LinkView: View {
    
    let linkInfo: LinkInfo
    let onDeleteConfirmed: (String) -> Void
    
    @State private var isDeleteLinkConfirmationPresented = false
    
    var body: some View {
        Text(.init(linkInfo.getMdText()))
            .contextMenu {
                Button(role: .destructive) {
                    isDeleteLinkConfirmationPresented = true
                } label: {
                    Label("delete", systemImage: "trash")
                }
            }
            .confirmationDialog("delete \(linkInfo.link)", isPresented: $isDeleteLinkConfirmationPresented, titleVisibility: .visible) {
                Button("delete", role: .destructive) {
                    onDeleteConfirmed(linkInfo.link)
                }
            }
    }
}

struct LinkView_Previews: PreviewProvider {
    static var previews: some View {
        LinkView(linkInfo: LinkInfo(title: "title", link: "link"), onDeleteConfirmed: {_ in})
    }
}
