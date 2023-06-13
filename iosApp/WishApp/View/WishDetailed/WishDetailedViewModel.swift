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
    
    @Published var wish: WishEntity = WishEntityKt.createEmptyWish()
    @Published var title: String = ""
    @Published var comment: String = ""
    @Published var link: String = ""
    
    @Published var isAddLinkButtonEnabled: Bool = false
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    private let linksAdapter = LinksAdapter()
    private var wishId: String
   
    init(wishId: String? = nil) {
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

    private func insertWishAndObserve(wish: WishEntity) {
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                print("Error: \(error) in insert wish")
                return Just(KotlinUnit())
            }
            .sink(receiveCompletion: { [weak self] completion in
                print("Completeion: \(completion) in insert wish")
                self?.observeWish(wishId: wish.id)
            }, receiveValue: {value in
                print("Value: \(value) in insert wish")
            })
            .store(in: &subscriptions)
    }
    
    private func observeWish(wishId: String) {
        createPublisher(for: dbRepository.observeWishById(id: wishId))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                print("Error: \(error) in observeWish")
                return Just(WishEntityKt.createEmptyWish())
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] wish in
                self?.wish = wish
                self?.title = wish.title
                self?.comment = wish.comment
            }
            .store(in: &subscriptions)
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
