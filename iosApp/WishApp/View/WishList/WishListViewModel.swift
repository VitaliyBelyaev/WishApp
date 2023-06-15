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
import SwiftUI

@MainActor
final class WishListViewModel: ObservableObject {
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    @Published private var mode: WishListMode = WishListMode.Empty
    
    @Published private var tag: TagEntity = TagEntity(id: "", title: "")
    
    @Published var wishes: [WishEntity] = []
    
    @Published var title: LocalizedStringKey = ""
    
    init(mode: WishListMode) {
        self.mode = mode
        self.updateTitle(mode: mode)
        self.collectTagIfNeeded(mode: mode)
        self.collectWishes()
    }
    
    func onAddWishClicked() {
        let timestamp = Date.currentTimeStamp
        
        let randNumber = Int.random(in: 0..<1000)
        let wish = WishEntity(id: NSUUID().uuidString, title: "Test wish \(randNumber)", link: "Some link", links:["Some link"], comment: "Some commmment", isCompleted: false, createdTimestamp: timestamp, updatedTimestamp: timestamp, position: 0, tags: [])
        
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    
    private func collectWishes() {
        let dbRepository = dbRepository
        
        $mode
            .flatMap { mode in
                switch mode {
                case .All:
                    return createPublisher(for: dbRepository.observeAllWishes(isCompleted: false))
                case .Completed:
                    return createPublisher(for: dbRepository.observeAllWishes(isCompleted: true))
                case let .ByTag(tagId):
                    return createPublisher(for: dbRepository.observeWishesByTag(tagId: tagId))
                case .Empty:
                    return createPublisher(for: dbRepository.observeAllWishes(isCompleted: true))
                }
            }
            .subscribe(on: DispatchQueue.global())
            .retry(3)
            .catch { error in
                Just(Array<WishEntity>())
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.wishes, on: self)
            .store(in: &subscriptions)
    }
    
    private func updateTitle(mode: WishListMode) {
        switch mode {
        case .All:
            self.title = LocalizedStringKey("WishList.all")
        case .Completed:
            self.title = LocalizedStringKey("WishList.completed")
        case .ByTag(_):
            self.title =  ""
        case .Empty:
            self.title = ""
        }
    }
    
    private func collectTagIfNeeded(mode: WishListMode) {
        let id: String?

        switch mode {
        case let .ByTag(tagId):
            id = tagId
        default:
            id = nil
        }
        if let id = id {
            collectTag(id: id)
        }
    }
    
    private func collectTag(id: String) {
        createFuture(for: dbRepository.getTagById(id: id))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                Just(TagEntity(id: "", title: ""))
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] tag in
                self?.tag = tag
                self?.title = LocalizedStringKey(tag.title)
            }
            .store(in: &subscriptions)
    }
}

