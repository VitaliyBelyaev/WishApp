//
//  WishListContentView.swift
//  WishApp
//
//  Created by Vitaliy on 13.06.2023.
//

import SwiftUI
import shared

struct WishListContentView: View {
    
    let wishes: [WishEntity]
    let title: LocalizedStringKey
    let onSettingsClicked: () -> ()
    
    let onAddTestWishClicked: () -> ()
    
    var body: some View {
        List {
            ForEach(wishes, id: \.id) { wish in
                NavigationLink(value: MainNavSegment.createFromWishId(id: wish.id)) {
                    VStack(alignment: .leading) {
                        Text(wish.title).font(.title)
                        Text(wish.comment)
                        Text(wish.link)
                    }
                }
            }
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("+wish"){
                    onAddTestWishClicked()
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
                Button {
                    
                } label: {
                    Image(systemName: "square.and.arrow.up")
                }
                
                NavigationLink(value: MainNavSegment.createFromWishId(id: nil)) {
                    Image(systemName: "square.and.pencil")
                }
            }
        }
    }
}

struct WishListContentView_Previews: PreviewProvider {
    
    private static var wishes = [
        WishEntity(id: "1", title: "iPhone 15", link: "Some link", links:["Some link"], comment: "Some commmment", isCompleted: false, createdTimestamp: 0, updatedTimestamp: 0, position: 0, tags: []),
        WishEntity(id: "2", title: "Робот пылесос", link: "Some link", links:["Some link"], comment: "Some commmment", isCompleted: false, createdTimestamp: 0, updatedTimestamp: 0, position: 1, tags: []),
        WishEntity(id: "3", title: "Механическая клавиатура", link: "Some link", links:["Some link"], comment: "Some commmment", isCompleted: false, createdTimestamp: 0, updatedTimestamp: 0, position: 2, tags: []),
        
    ]
    
    static var previews: some View {
        
        NavigationStack {
            WishListContentView(
                wishes: wishes,
                title: "All",
                onSettingsClicked: {},
                onAddTestWishClicked: {}
            )
        }
    }
}
