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

@MainActor
final class WishDetailedViewModel: ObservableObject {
    
    private let sdk: WishAppSdk?
    
    private var dbRepository: DatabaseRepository? {
        get {
            return sdk?.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    private var wishId: String
    
    private let linksAdapter = LinksAdapter()
    
    @Published var wish: WishEntity = WishEntityKt.createEmptyWish()
    
    init(sdk: WishAppSdk? = nil, wishId: String? = nil) {
        self.sdk = sdk
        if let wishId = wishId {
            self.wishId = wishId
            observeWish(wishId: wishId)
        } else {
            let wish = WishEntityKt.createEmptyWish()
            self.wishId = wish.id
            insertWishAndObserve(wish: wish)
        }
    }
    
    func onNewLinkAddClicked(link: String) {
        guard let dbRepository = dbRepository else {
            return
        }
        
        let newLinkAccumulatedString = linksAdapter.addLinkAndGetAccumulatedString(link: link, currentLinks: wish.links)
        
        createFuture(for: dbRepository.updateWishLink(newValue: newLinkAccumulatedString, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onTitleChanged(to newTitle: String) {
        guard let dbRepository = dbRepository else {
            return
        }
        
//        createFuture(for: dbRepository.updateWishTitle(newValue: newTitle, wishId: wishId))
//            .subscribe(on: DispatchQueue.global())
//            .sinkSilently()
//            .store(in: &subscriptions)
    }
    
    func onCommentChanged(to newComment: String) {
        guard let dbRepository = dbRepository else {
            return
        }
        
//        createFuture(for: dbRepository.updateWishComment(newValue: newComment, wishId: wishId))
//            .subscribe(on: DispatchQueue.global())
//            .sinkSilently()
//            .store(in: &subscriptions)
    }

    private func insertWishAndObserve(wish: WishEntity) {
        guard let dbRepository = dbRepository else {
            return
        }
        
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .sink(receiveCompletion: { [weak self] completion in
                self?.observeWish(wishId: wish.id)
            }, receiveValue: {_ in})
            .store(in: &subscriptions)
    }
    
    private func observeWish(wishId: String) {
        guard let dbRepository = dbRepository else {
            return
        }
        
        createPublisher(for: dbRepository.observeWishById(id: wishId))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                Just(WishEntityKt.createEmptyWish())
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.wish, on: self)
            .store(in: &subscriptions)
    }
}
