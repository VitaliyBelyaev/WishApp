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
            return sdk.getDatabaseRepository()
        }
    }
    
    private var newWishIdToCheckForDeletion: String? = nil
    
    private var subscriptions: [AnyCancellable] = []
    
    func onWishCompletnessChangeButtonClicked(wishId: String, newIsCompleted: Bool) {
        createFuture(for: dbRepository.updateWishIsCompleted(newValue: newIsCompleted, wishId: wishId))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
   
    func deleteWish(id: String) {
        
        sdk.closeDatabase()
        
        createFuture(for: dbRepository.deleteWishesByIds(ids: [id]))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    func onNewWishIdToCheckForDeletionChanged(newWishId: String?) {
        self.newWishIdToCheckForDeletion = newWishId
    }
    
    func onNewWishDetailedScreenExit() {
        guard let newWishIdToCheckForDeletion = newWishIdToCheckForDeletion else { return }
        self.newWishIdToCheckForDeletion = nil
        
        createFuture(for: dbRepository.getWishById(id: newWishIdToCheckForDeletion))
            .subscribe(on: DispatchQueue.global())
            .receive(on: DispatchQueue.main)
            .sinkIgnoringCompletion { [weak self] newWish in
                self?.deleteWishIfEmpty(wish: newWish)
            }
            .store(in: &subscriptions)
    }
    
    private func deleteWishIfEmpty(wish: WishEntity) {
        if !WishEntityKt.isWishEmpty(wish: wish) {
            return
        }
        
        createFuture(for: dbRepository.deleteWishesByIds(ids: [wish.id]))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
}
