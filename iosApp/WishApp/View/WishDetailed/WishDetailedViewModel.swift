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
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    
    private var dbRepository: DatabaseRepository? {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    private let linksAdapter = LinksAdapter()
    private var wishId: String
   
    init(wishId: String? = nil) {
        let d = Date()
        let df = DateFormatter()
        df.dateFormat = "y-MM-dd H:mm:ss.SSSS"
        let dateString = df.string(from: d)
        
        print("\(dateString) WishDetailedViewModel init, wishId: \(wishId ?? "nil")")
        if let wishIdNotNull = wishId {
            self.wishId = wishIdNotNull
            observeWish(wishId: wishIdNotNull)
        } else {
            let wish = WishEntityKt.createEmptyWish()
            self.wishId = wish.id
            self.wish = wish
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
        
        if(newTitle == wish.title) {
            return
        }
        
        createFuture(for: dbRepository.updateWishTitle(newValue: newTitle, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onCommentChanged(to newComment: String) {
        guard let dbRepository = dbRepository else {
            return
        }
        
        if(newComment == wish.comment) {
            return
        }
        
        createFuture(for: dbRepository.updateWishComment(newValue: newComment, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }

    private func insertWishAndObserve(wish: WishEntity) {
        guard let dbRepository = dbRepository else {
            return
        }
        
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
        guard let dbRepository = dbRepository else {
            return
        }
        
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
}
