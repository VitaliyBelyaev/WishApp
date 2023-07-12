//
//  WishItemView.swift
//  WishApp
//
//  Created by Vitaliy on 19.06.2023.
//

import SwiftUI
import shared
import SwiftUIFlowLayout

struct WishItemView: View {
    
    let wish: WishEntity
    let onWishTagClicked: (String) -> ()
    let onDeleteWishConfirmed: (String) -> ()
    let onWishCompletnessChanged: (String, Bool) -> ()
    
    private let wishAdditionalInfoOpacity = 0.7
    
    @State private var isDeleteWishConfirmationPresented = false
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(wish.title)
                .font(.title)
                .strikethrough(wish.isCompleted)
                .padding(.bottom, 1)
            
            if !wish.comment.isEmpty {
                Text(wish.comment)
                    .foregroundColor(.primary.opacity(wishAdditionalInfoOpacity))
                    .padding(.bottom, 1)
            }
            
            if !wish.links.isEmpty {
                HStack {
                    Image(systemName: "link")
                        .font(.system(size: 15))
                        .foregroundColor(.primary.opacity(wishAdditionalInfoOpacity))
                    Text("\(wish.links.count)")
                        .font(.body)
                        .foregroundColor(.primary.opacity(wishAdditionalInfoOpacity))
                }
            }
            
            if !wish.tags.isEmpty {
                FlowLayout(mode: .scrollable, items: wish.tags, itemSpacing: 3) { tag in
                    let isOn: Binding<Bool> = Binding(
                        get: { return true },
                        set: { value, tr in
                            onWishTagClicked(wish.id)
                        }
                    )
                    
                    Toggle(isOn: isOn) {
                        Text(tag.title).font(.caption)
                            .padding(0)
                        
                    }
                    .toggleStyle(.button)
                    .buttonStyle(.bordered)
                    .foregroundColor(isOn.wrappedValue ? .primary.opacity(0.8) : .gray)
                }
            }
        }
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            Button {
                WishAppAnalytics.logEvent(WishListDeleteWishClickedEvent())
                isDeleteWishConfirmationPresented = true
            } label: {
                Label("delete", systemImage: "trash")
            }
            .tint(Color.init(.systemRed))
        }
        .swipeActions(edge: .leading, allowsFullSwipe: true) {
            let title: LocalizedStringKey = wish.isCompleted ? "WishDetailed.markUndoneShort" : "WishDetailed.markDoneShort"
            let image = wish.isCompleted ? "arrow.uturn.backward" : "checkmark"
            Button {
                WishAppAnalytics.logEvent(WishListChangeWishCompletnessClickedEvent())
                onWishCompletnessChanged(wish.id, !wish.isCompleted)
            } label: {
                Label(title, systemImage: image)
            }
            .tint(wish.isCompleted ? Color.init(.systemGray2) : Color.init(.systemGreen))
        }
        .confirmationDialog("delete \(wish.title)", isPresented: $isDeleteWishConfirmationPresented, titleVisibility: .visible) {
            Button("delete", role: .destructive) {
                WishAppAnalytics.logEvent(WishListDeleteWishConfirmedEvent())
                onDeleteWishConfirmed(wish.id)
            }
        }
        .padding(.vertical, 2)
    }
}

struct WishItemView_Previews: PreviewProvider {
    static var previews: some View {
        WishItemView(
            wish: WishEntityKt.createEmptyWish(),
            onWishTagClicked: {_ in},
            onDeleteWishConfirmed: {_ in },
            onWishCompletnessChanged: {_,_ in }
        )
    }
}
