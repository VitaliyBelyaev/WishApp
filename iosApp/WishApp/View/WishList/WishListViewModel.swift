//
//  WishListViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine

@MainActor
final class WishListViewModel: ObservableObject {
    
    private let sdk: WishAppSdk?
    
    private let mode: WishListMode
    
    private var dbRepository: DatabaseRepository? {
        get {
            return sdk?.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    @Published var wishes: [WishEntity] = []
    
    @Published var title: String = ""
    
    init(sdk: WishAppSdk?, mode: WishListMode) {
        self.sdk = sdk
        self.mode = mode
        self.collectWishes()
        
        switch mode {
        case .All:
            self.title = "All"
        case .Completed:
            self.title = "Completed"
        case let .ByTag(tag):
            self.title =  tag.title
        }
    }
    
    
    func onAddWishClicked() {
        let timestamp = Date.currentTimeStamp
        
        let randNumber = Int.random(in: 0..<1000)
        let wish = WishEntity(id: NSUUID().uuidString, title: "Test wish \(randNumber)", link: "Some link", comment: "Some commmment", isCompleted: false, createdTimestamp: timestamp, updatedTimestamp: timestamp, position: 0, tags: [])
        
    
        
        guard let dbRepository = dbRepository else {
            return
        }
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    
    private func collectWishes() {
        guard let dbRepository = dbRepository else {
            return
        }
        createPublisher(for: dbRepository.observeAllWishes(isCompleted: false))
            .subscribe(on: DispatchQueue.global())
            .retry(3)
            .catch { error in
                Just([])
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.wishes, on: self)
            .store(in: &subscriptions)
    }
}


