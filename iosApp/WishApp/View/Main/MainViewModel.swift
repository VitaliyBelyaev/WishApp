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
        
        
        if let containerUrl = FileManager.default.url(forUbiquityContainerIdentifier: nil)?.appendingPathComponent("Documents") {
            if !FileManager.default.fileExists(atPath: containerUrl.path, isDirectory: nil) {
                do {
                    try FileManager.default.createDirectory(at: containerUrl, withIntermediateDirectories: true, attributes: nil)
                }
                catch {
                    print(error.localizedDescription)
                }
            }
            
            let fileUrl = containerUrl.appendingPathComponent("TEST546464TTTTTT.txt")
            
            do {
                try "Hello iCloud 333333!".write(to: fileUrl, atomically: true, encoding: .utf8)
                let input = try String(contentsOf: fileUrl)
                            print(input)
            }
            catch {
                print(error.localizedDescription)
            }
        }
        
        
        
//        guard let driveURL = FileManager.default.url(forUbiquityContainerIdentifier: nil)?.appendingPathComponent("Documents") else {
//            print("Error get driveUrl")
//            return
//        }
//        
//        
//        let data = Data("Test Message".utf8)
//     let fileUrl = URL.documentsDirectory.appending(path: "message.txt")
//        let fileUrl = driveURL.appending(path: "message.txt")
//    
//        do {
//            try data.write(to: fileUrl, options: [.atomic, .completeFileProtection])
//            let input = try String(contentsOf: fileUrl)
//            print(input)
//        } catch {
//            print(error.localizedDescription)
//        }
        
//        if testWishes.isEmpty {
        //            return
//        }
//        let timestamp = Date.currentTimeStamp
//        
//        let testWish: WishEntity = testWishes[testWishIndex % testWishes.count]
//            .createCopy(id: NSUUID().uuidString, createdTimestamp: timestamp, updatedTimestamp: timestamp)
//
//        createFuture(for: dbRepository.insertWish(wish: testWish))
//            .subscribe(on: DispatchQueue.global())
//            .sinkSilently()
//            .store(in: &subscriptions)
//        
//        testWishIndex += 1
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
