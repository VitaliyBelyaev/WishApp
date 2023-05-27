//
//  MainViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine

@MainActor
final class MainViewModel: ObservableObject {
    
    private let sdk: WishAppSdk?
    
    private var dbRepository: DatabaseRepository {
        get {
            guard let sdk else { fatalError() }
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    @Published var items: [MainItem] = []
    
    init(sdk: WishAppSdk?) {
        self.sdk = sdk
        self.subscribeOnMainItems()
    }
    
    func onAddWishClicked(){
        let timestamp = Date.currentTimeStamp
        
        let randNumber = Int.random(in: 0..<1000)
        let wish = WishEntity(id: NSUUID().uuidString, title: "Test wish \(randNumber)", link: "Some link", comment: "Some commmment", isCompleted: false, createdTimestamp: timestamp, updatedTimestamp: timestamp, position: 0, tags: [])
        
    
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onAddTagClicked(){
        createFuture(for: dbRepository.insertTag(title: "Tag title \(Int.random(in: 0..<1000))"))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    private func subscribeOnMainItems(){
        let all = createPublisher(for: dbRepository.observeWishesCount(isCompleted: false))
        let completed = createPublisher(for: dbRepository.observeWishesCount(isCompleted: true))
        let tags = createPublisher(for: dbRepository.observeAllTagsWithWishesCount())
        
        Publishers.CombineLatest3(all, completed, tags)
            .subscribe(on: DispatchQueue.global())
            .map { allCount, completedCount, tags in
                let tagMainItems = tags.map{ tag in
                    MainItem.WishTag(tag.tag, Int(tag.wishesCount))
                }
                let items = [
                    MainItem.AllWishes(Int(truncating: allCount)),
                    MainItem.CompletedWishes(Int(truncating: completedCount)),
                    MainItem.Settings
                ]
                
                print("items:\(items)")
                return items + tagMainItems
            }
            .catch { error in
                Just(Array())
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.items, on: self)
            .store(in: &subscriptions)
    }
}
