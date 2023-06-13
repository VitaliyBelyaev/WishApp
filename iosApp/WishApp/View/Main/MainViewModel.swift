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
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
        
    @Published var state: MainViewState = MainViewState(commonItems: [], tagItems: [])
     
    init() {
        let d = Date()
        let df = DateFormatter()
        df.dateFormat = "y-MM-dd H:mm:ss.SSSS"
        let dateString = df.string(from: d)
        
        print("\(dateString) MainViewModel init")
        self.subscribeOnMainItems()
    }
    
    func onRenameTagConfirmed(tag: TagEntity, newTitle: String) {
        if(tag.title == newTitle) {
            return
        }
        
        createFuture(for: dbRepository.updateTagTitle(title: newTitle, tagId: tag.id))
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
    
    func onAddWishClicked() {
        let timestamp = Date.currentTimeStamp
        
        let randNumber = Int.random(in: 0..<1000)
        let wish = WishEntity(id: NSUUID().uuidString, title: "Test wish \(randNumber)", link: "Some link", links: [], comment: "Some commmment", isCompleted: false, createdTimestamp: timestamp, updatedTimestamp: timestamp, position: 0, tags: [])
        
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
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
            .map { allCount, completedCount, tags in
                let commonItems = [
                    CommonMainItem(type: .All, count: Int(truncating: allCount)),
                    CommonMainItem(type: .Completed, count: Int(truncating: completedCount))
                ]
                let tagItems = tags.map { tag in
                    WishTagMainItem(tag: tag.tag, count: Int(tag.wishesCount))
                }
                return MainViewState(commonItems: commonItems, tagItems: tagItems)
            }
            .catch { error in
                Just(MainViewState(commonItems: [], tagItems: []))
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.state, on: self)
            .store(in: &subscriptions)
    }
}
