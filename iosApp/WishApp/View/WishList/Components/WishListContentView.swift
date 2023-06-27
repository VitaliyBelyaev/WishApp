//
//  WishListContentView.swift
//  WishApp
//
//  Created by Vitaliy on 13.06.2023.
//

import SwiftUI
import shared
import SwiftUIFlowLayout

struct WishListContentView: View {
    
    let mode: WishListMode
    let wishes: [WishEntity]
    let title: LocalizedStringKey
    let shareText: String
    
    let onSettingsClicked: () -> ()
    let onWishTagClicked: (String) -> ()
    let onDeleteWishConfirmed: (String) -> ()
    let onWishCompletnessChanged: (String, Bool) -> ()
    let onMove: (IndexSet, Int) -> ()
    @State private var isDeleteWishConfirmationPresented = false
    
    let onAddTestWishClicked: () -> ()
    
    private var onMovePerform : ((IndexSet, Int) -> ())? {
        get {
            if mode != WishListMode.Completed {
                return onMove
            } else {
                return nil
            }
        }
    }
    
    private var tagId: String? {
        get {
            switch mode {
            case .All:
                return nil
            case .Completed:
                return nil
            case .ByTag(let id):
                return id
            case .Empty:
                return nil
            }
        }
    }
    
    var body: some View {
        List {
            ForEach(wishes, id: \.id) { wish in
                NavigationLink(value: MainNavSegment.createFromWishId(id: wish.id, tagId: nil)) {
                    WishItemView(
                        wish: wish,
                        onWishTagClicked: onWishTagClicked,
                        onDeleteWishConfirmed: onDeleteWishConfirmed,
                        onWishCompletnessChanged: onWishCompletnessChanged
                    )
                }
            }
            .onMove(perform: onMovePerform)
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("+wish"){
                    onAddTestWishClicked()
                }
            }
            
            if mode != WishListMode.Completed && !wishes.isEmpty {
                ToolbarItem(placement: .primaryAction) {
                    EditButton()
                }
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                if mode != WishListMode.Completed && !wishes.isEmpty {
                    ShareLink(item: shareText) {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
                
                Spacer()
                
                if mode != WishListMode.Completed {
                    
                    NavigationLink(value: MainNavSegment.createFromWishId(id: nil, tagId: self.tagId)) {
                        Image(systemName: "square.and.pencil")
                    }
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
                mode: WishListMode.All,
                wishes: wishes,
                title: "All",
                shareText: "",
                onSettingsClicked: {},
                onWishTagClicked: {_ in },
                onDeleteWishConfirmed: {_ in },
                onWishCompletnessChanged: {_,_ in },
                onMove: {_,_ in },
                onAddTestWishClicked: {}
            )
        }
    }
}
