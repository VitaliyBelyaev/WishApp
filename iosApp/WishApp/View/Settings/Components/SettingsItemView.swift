//
//  SettingsItemView.swift
//  WishApp
//
//  Created by Vitaliy on 24.06.2023.
//

import SwiftUI

struct SettingsItemView: View {
    
    let title: LocalizedStringKey
    let onClick: () -> ()
    
    var body: some View {
        Button {
            onClick()
        } label: {
            HStack {
                Text(title)
                Spacer()
                NavigationLink.empty.frame(width: 16)
            }
            
        }
        .buttonStyle(.borderless)
        .foregroundColor(.primary)
    }
}

struct SettingsItemView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsItemView(
            title: "Title", onClick: {})
    }
}
