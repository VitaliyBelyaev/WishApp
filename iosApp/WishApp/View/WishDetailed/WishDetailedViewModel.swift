//
//  WishDetailedViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 07.06.2023.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import UIKit

@MainActor
final class WishDetailedViewModel: ObservableObject {
    
    @Published var wish: WishEntity = WishEntityKt.createEmptyWish()
    @Published var title: String = ""
    @Published var comment: String = ""
    @Published var link: String = ""
    @Published var linksInfos: [LinkInfo] = []
    
    @Published var isAddLinkButtonEnabled: Bool = false
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.getDatabaseRepository()
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    private let linksAdapter = LinksAdapter()
    private var wishId: String
    private var tagId: String?
   
    init(wishId: String?, tagId: String?) {
        self.tagId = tagId
        
        if let wishIdNotNull = wishId {
            self.wishId = wishIdNotNull
            observeWish(wishId: wishIdNotNull)
        } else {
            let wish = WishEntityKt.createEmptyWish()
            self.wishId = wish.id
            self.wish = wish
            insertWishAndObserve(wish: wish)
        }
        
        self.observeLink()
    }
    
    func onNewLinkAddClicked(link: String) {
        let newLinkAccumulatedString = linksAdapter.addLinkAndGetAccumulatedString(link: link, currentLinks: wish.links)
        
        createFuture(for: dbRepository.updateWishLink(newValue: newLinkAccumulatedString, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onDeleteLinkConfirmed(link: String) {
        let newLinkAccumulatedString = linksAdapter.removeLinkAndGetAccumulatedString(link: link, currentLinks: wish.links)
        
        createFuture(for: dbRepository.updateWishLink(newValue: newLinkAccumulatedString, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onTitleChanged(to newTitle: String) {
        if(newTitle == wish.title) {
            return
        }
        
        createFuture(for: dbRepository.updateWishTitle(newValue: newTitle, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onCommentChanged(to newComment: String) {
        if(newComment == wish.comment) {
            return
        }
        
        createFuture(for: dbRepository.updateWishComment(newValue: newComment, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onDeleteWish() {
        subscriptions.forEach { subscription in
            subscription.cancel()
        }
    }

    private func insertWishAndObserve(wish: WishEntity) {
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                return Just(KotlinUnit())
            }
            .sink(receiveCompletion: { [weak self] completion in
                self?.observeWish(wishId: wish.id)
            }, receiveValue: { value in
                
            })
            .store(in: &subscriptions)
    }
    
    private func observeWish(wishId: String) {
        if let tagId = tagId {
            addTagToWish(wishId: wishId, tagId: tagId)
        }
        
        createPublisher(for: dbRepository.observeWishById(id: wishId))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                return Just(WishEntityKt.createEmptyWish())
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] wish in
                if let self = self {
                    self.wish = wish
                    self.title = wish.title
                    self.comment = wish.comment
                    self.linksInfos = self.getLinksInfos(links: wish.links)
                }
            }
            .store(in: &subscriptions)
    }
    
    private func addTagToWish(wishId: String, tagId: String) {
        createFuture(for: dbRepository.insertWishTagRelation(wishId: wishId, tagId: tagId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    private func getLinksInfos(links: [String]) -> [LinkInfo] {
        links.map { link in
            let title = URL(string: link)?.host ?? link
            return LinkInfo(title: title, link: link)
        }
    }
    
    private func observeLink() {
        $link
            .receive(on: DispatchQueue.main)
            .sink { [weak self] link in
                if let self = self {
                    self.isAddLinkButtonEnabled = self.isLinkValid(link: link)
                }
            }
            .store(in: &subscriptions)
    }
    
    func isLinkValid(link: String) -> Bool {
        if let url = NSURL(string: link) {
            return UIApplication.shared.canOpenURL(url as URL)
        }
        return false
    }
}
