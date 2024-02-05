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
    
    func onRestoreClicked() {
        if let containerUrl = FileManager.default.getAppContainerUrlInICloud() {
            FileManager.default.createDirIfNotExists(dirUrl: containerUrl)
            
            let fileUrl = containerUrl.appendingPathComponent("wishapp.backup")
            
            var isDir:ObjCBool = false
            if !FileManager.default.fileExists(atPath: fileUrl.path, isDirectory: &isDir) {
                print("backup not exists in iCloud")
                return
            }
            
            let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
            
            
            
            if FileManager.default.fileExists(atPath: originFileUrl.path, isDirectory: &isDir) {
                do {
                    try FileManager.default.removeItem(at: originFileUrl)
                }
                catch {
                    //Error handling
                    print("Error in remove item")
                }
            }
            
            do {
                try FileManager.default.copyItem(at: fileUrl, to: originFileUrl)
                print("Copy done")
            }
            catch {
                //Error handling
                print("Error in copy item:\(error.localizedDescription)")
            }
        }
    }
    
    func onAddTestWishClicked() {
        
        let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases")
        do {
            let items = try FileManager.default.contentsOfDirectory(atPath: originFileUrl.path)
            
            for item in items {
                
                
                print("Found \(item)")
            }
        } catch {
            // failed to read directory – bad permissions, perhaps?
        }
        
        
        
        // Рабочий код
//        if let containerUrl = FileManager.default.getAppContainerUrlInICloud() {
//            FileManager.default.createDirIfNotExists(dirUrl: containerUrl)
//            
//            let fileUrl = containerUrl.appendingPathComponent("wishapp.backup")
//            let originFileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
//            
//            var isDir:ObjCBool = false
//            
//            if FileManager.default.fileExists(atPath: fileUrl.path, isDirectory: &isDir) {
//                do {
//                    try FileManager.default.removeItem(at: fileUrl)
//                }
//                catch {
//                    //Error handling
//                    print("Error in remove item")
//                }
//            }
//            
//            do {
//                try FileManager.default.copyItem(at: originFileUrl, to: fileUrl)
//                print("Copy done")
//            }
//            catch {
//                //Error handling
//                print("Error in copy item:\(error.localizedDescription)")
//            }
//        }
        
        
//        
//        let data = Data("Test Message12131313131342fesrfsrgf".utf8)
//        let fileUrl = URL.applicationSupportDirectory.appendingPathComponent("databases/ru_vitaliy_belyaev_wishapp.db")
//        print(fileUrl.path())
        
        
//        do {
//            let items = try FileManager.default.contentsOfDirectory(atPath: fileUrl.path)
//
//            for item in items {
//                
//            
//                print("Found \(item)")
//            }
//        } catch {
//            // failed to read directory – bad permissions, perhaps?
//        }
        
    
    
        
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
