//
//  UpdateWishTagsViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine


@MainActor
final class UpdateWishTagsViewModel: ObservableObject {
    
    
    @Published var query: String = ""
    @Published var state: ScreenState = ScreenState(createItem: nil, tagItems: [])
    
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    private var wishId: String
    
    private var recentlyAddedTagsIds: [String] = []
    
    init(wishId: String) {
        self.wishId = wishId
        self.observeState()
    }
    
    func onTagSelectedChanged(tagItem: TagItem) {
        let newIsSelected = !tagItem.isSelected
        if newIsSelected {
            createFuture(for: dbRepository.insertWishTagRelation(wishId: wishId, tagId: tagItem.tag.id))
                .subscribe(on: DispatchQueue.global())
                .sinkSilently()
                .store(in: &subscriptions)
        } else {
            createFuture(for: dbRepository.deleteWishTagRelation(wishId: wishId, tagId: tagItem.tag.id))
                .subscribe(on: DispatchQueue.global())
                .sinkSilently()
                .store(in: &subscriptions)
        }
    }
    
    func onCreateTagClicked(title: String) {
        query = ""
        
        createFuture(for: dbRepository.insertTag(title: title))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                Just("")
            }
            .sink { [weak self] tagId in
                guard let self = self else { return }
                guard !tagId.isEmpty else { return }
                
                recentlyAddedTagsIds.insert(tagId, at: 0)
                addWishTagRelation(tagId: tagId, wishId: wishId)
            }
            .store(in: &subscriptions)
    }
    
    private func addWishTagRelation(tagId: String, wishId: String) {
        createFuture(for: dbRepository.insertWishTagRelation(wishId: wishId, tagId: tagId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    private func observeState() {
        let allTags = createPublisher(for: dbRepository.observeAllTags())
            .catch{ error in
                Just([])
            }
        let wishTags = createPublisher(for: dbRepository.observeTagsByWishId(wishId: wishId))
            .catch{ error in
                Just([])
            }
        
        Publishers.CombineLatest3(allTags, wishTags, $query)
            .subscribe(on: DispatchQueue.global())
            .map { [weak self] allTags, wishTags, query in
                if let self = self {
                    return self.createState(allTags: allTags, wishTags: wishTags, query: query)
                } else{
                    return ScreenState(createItem: nil, tagItems: [])
                }
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.state, on: self)
            .store(in: &subscriptions)
    }
    
    
    private func createState(allTags: [TagEntity], wishTags: [TagEntity], query: String) -> ScreenState {
        let allTagItemsFilteredByQuery: [TagItem] = filterTagItemsByQuery(
            tagItems: createTagItems(allTags: allTags, wishTags: wishTags),
            query: query
        )
        
        let withoutRecentlyAdded: [TagItem] = allTagItemsFilteredByQuery.filter { tagItem in
            return !recentlyAddedTagsIds.contains { tagId in
                tagId == tagItem.tag.id
            }
        }
        
        var recentlyAdded: [TagItem] = getRecentlyAddedTagItems(tagItems: allTagItemsFilteredByQuery, recentlyAddedTagsIds: recentlyAddedTagsIds)
        
        var resultTagItems = [TagItem]()
        resultTagItems.append(contentsOf: recentlyAdded)
        resultTagItems.append(contentsOf: withoutRecentlyAdded)
        
        let createItem: CreateTagItem?
        if !query.isEmpty && allTagItemsFilteredByQuery.isEmpty {
            createItem = CreateTagItem(title: query)
        } else {
            createItem = nil
        }
        return ScreenState(createItem: createItem, tagItems: resultTagItems)
    }
    
    private func createTagItems(allTags: [TagEntity], wishTags: [TagEntity]) -> [TagItem] {
        return allTags.map { tag in
            let isSelected =  wishTags.contains { wishTag in
                return wishTag.id == tag.id
            }
            return TagItem(tag: tag, isSelected: isSelected)
        }
    }
    
    private func filterTagItemsByQuery(tagItems: [TagItem], query: String) -> [TagItem] {
        return tagItems.filter { tagItem in
            if query.isEmpty {
                return true
            } else {
                return tagItem.tag.title.localizedCaseInsensitiveContains(query)
            }
        }
    }
    
    private func getRecentlyAddedTagItems(tagItems: [TagItem], recentlyAddedTagsIds: [String]) -> [TagItem] {
        var recentlyAdded: [TagItem] = []
        
        for tagId in recentlyAddedTagsIds {
            let tagItemToAdd = tagItems.first { tagItem in
                return tagId == tagItem.tag.id
            }
            if let tagItemToAdd = tagItemToAdd {
                recentlyAdded.append(tagItemToAdd)
            }
        }
        
        return recentlyAdded
    }
}

