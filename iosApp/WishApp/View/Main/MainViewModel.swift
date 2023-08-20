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
import FirebaseAnalytics
import Amplitude

@MainActor
final class MainViewModel: ObservableObject {
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    private var testWishes: [WishEntity] = []
    private var testWishIndex: Int = 0
    
    @Published var state: MainViewState = MainViewState(
        commonItems: [],
        tagItems: [],
        currentCount: 0,
        completedCount: 0)
    
    init() {
        self.testWishes = createTestWishes(forRu: true)
        self.subscribeOnMainItems()
    }
    
    func onRenameTagConfirmed(tag: TagEntity, newTitle: String) {
        if(tag.title == newTitle) {
            return
        }
        let newTitleTrimmed = newTitle.trimmingCharacters(in: .whitespacesAndNewlines)
        
        createFuture(for: dbRepository.updateTagTitle(title: newTitleTrimmed, tagId: tag.id))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onDeleteTagClicked(tag: TagEntity) {
        createFuture(for: dbRepository.deleteTagsByIds(ids: [tag.id]))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
        
    }
    
    func onAddTestWishClicked() {
        if testWishes.isEmpty {
            return
        }
        let timestamp = Date.currentTimeStamp
        
        let testWish: WishEntity = testWishes[testWishIndex % testWishes.count]
            .createCopy(id: NSUUID().uuidString, createdTimestamp: timestamp, updatedTimestamp: timestamp)

        createFuture(for: dbRepository.insertWish(wish: testWish))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
        
        testWishIndex += 1
    }
    
    func onAddTagClicked() {
        createFuture(for: dbRepository.insertTag(title: "Tag title \(Int.random(in: 0..<1000))"))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    private func subscribeOnMainItems() {
        let all = createPublisher(for: dbRepository.observeWishesCount(isCompleted: false))
        let completed = createPublisher(for: dbRepository.observeWishesCount(isCompleted: true))
        let tags = createPublisher(for: dbRepository.observeAllTagsWithWishesCount())
        
        Publishers.CombineLatest3(all, completed, tags)
            .subscribe(on: DispatchQueue.global())
            .map { currentCount, completedCount, tags in
                
                let currentCountInt = Int(truncating: currentCount)
                let completedCountInt = Int(truncating: completedCount)
                
                let commonItems = [
                    CommonMainItem(type: .All, count: currentCountInt),
                    CommonMainItem(type: .Completed, count: Int(truncating: completedCount))
                ]
                let tagItems = tags.map { tag in
                    WishTagMainItem(tag: tag.tag, count: Int(tag.wishesCount))
                }
                return MainViewState(commonItems: commonItems, tagItems: tagItems, currentCount: currentCountInt, completedCount: completedCountInt)
            }
            .catch { error in
                Just(MainViewState(commonItems: [], tagItems: [], currentCount: 0, completedCount: 0))
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.state, on: self)
            .store(in: &subscriptions)
    }
    
    private func createTestWishes(forRu: Bool) -> [WishEntity] {
        #if DEBUG
        if forRu {
            return SampleDataGenerator().createRuWishes()
        } else {
            return SampleDataGenerator().createEnWishes()
        }
        #else
        return []
        #endif
    }
}
