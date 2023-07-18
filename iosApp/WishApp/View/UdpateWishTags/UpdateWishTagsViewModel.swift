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
    
    @Published private var recentlyAddedTagsIds: [String] = []
    
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
        
        let dbRepository = dbRepository
        let wishId = wishId
        
        createFuture(for: dbRepository.insertTag(title: title))
            .flatMap { tagId -> Publishers.Map<AnyPublisher<KotlinUnit, Error>, String> in
                return createFuture(for: dbRepository.insertWishTagRelation(wishId: wishId, tagId: tagId))
                    .map { _ in
                        return tagId
                    }
            }
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                Just("")
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] tagId in
                guard let self = self else { return }
                guard !tagId.isEmpty else { return }
    
                recentlyAddedTagsIds.insert(tagId, at: 0)
            }
            .store(in: &subscriptions)
    }
    
    
    private func observeState() {
        let allTagsPublisher = createPublisher(for: dbRepository.observeAllTags())
            .catch { error in
                Just([])
            }
        
        let wishTagsPublisher = createPublisher(for: dbRepository.observeTagsByWishId(wishId: wishId))
            .catch { error in
                Just([])
            }
        
        let wishTagsFuture = createFuture(for: dbRepository.getTagsByWishId(wishId: wishId))
            .catch { error in
                Just([])
            }

        let queryPublisher = $query
        
        Publishers.CombineLatest4(allTagsPublisher, wishTagsPublisher, queryPublisher, $recentlyAddedTagsIds)
            .subscribe(on: DispatchQueue.global())
            .flatMap { [weak self] allTags, wishTags, query, recentlyAddedTagsIds ->  Publishers.Map<Publishers.Catch<AnyPublisher<[TagEntity], Error>, Just<[TagEntity]>>, ScreenState> in
                
                return wishTagsFuture
                    .map { wishTags in
                        if let self = self {
                            return self.createState(allTags: allTags, wishTags: wishTags, query: query, recentlyAddedTagsIds: recentlyAddedTagsIds)
                        } else{
                            return ScreenState(createItem: nil, tagItems: [])
                        }
                    }
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.state, on: self)
            .store(in: &subscriptions)
    }
    
    private func createState(
        allTags: [TagEntity],
        wishTags: [TagEntity],
        query: String,
        recentlyAddedTagsIds: [String]
    ) -> ScreenState {
        let allTagItemsFilteredByQuery: [TagItem] = filterTagItemsByQuery(
            tagItems: createTagItems(allTags: allTags, wishTags: wishTags),
            query: query
        )
        
        let withoutRecentlyAdded: [TagItem] = allTagItemsFilteredByQuery.filter { tagItem in
            return !recentlyAddedTagsIds.contains { tagId in
                tagId == tagItem.tag.id
            }
        }
        
        let recentlyAdded: [TagItem] = getRecentlyAddedTagItems(tagItems: allTagItemsFilteredByQuery, recentlyAddedTagsIds: recentlyAddedTagsIds)
        
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
                return tagItem.tag.title.starts(with: query)
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

