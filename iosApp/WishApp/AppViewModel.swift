//
//  AppViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 13.06.2023.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine

@MainActor
final class AppViewModel: ObservableObject {
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    private var dbRepository: DatabaseRepository {
        get {
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
   
    func deleteWish(id: String) {
        createFuture(for: dbRepository.deleteWishesByIds(ids: [id]))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onNewWishDetailedScreenExit() {
        createFuture(for: dbRepository.getAllWishes(isCompleted: false))
            .subscribe(on: DispatchQueue.global())
            .catch { error in
                Just([])
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] wishes in
                self?.deleteEmptyWishes(wishes: wishes)
            }
            .store(in: &subscriptions)
    }
    
    private func deleteEmptyWishes(wishes: [WishEntity]) {
        let emptyWishesIds = wishes.filter{ wish in WishEntityKt.isWishEmpty(wish: wish) }.map { $0.id }
        if !emptyWishesIds.isEmpty {
            createFuture(for: dbRepository.deleteWishesByIds(ids: emptyWishesIds))
                .subscribe(on: DispatchQueue.global())
                .sinkSilently()
                .store(in: &subscriptions)
        }
    }
}
