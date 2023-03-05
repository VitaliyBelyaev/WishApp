//
//  MainViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import Foundation
import shared

@MainActor
final class MainViewModel: ObservableObject {
    
    private let sdk: WishAppSdk?
    
    private var wishesRepo: WishesRepository {
        get {
            guard let sdk else { fatalError() }
            return sdk.wishesRepository
        }
    }
    
    @Published var wishes: [WishEntity] = []
    
    init(sdk: WishAppSdk?) {
        self.sdk = sdk
    }
    
    func onAddWishClicked() {
        let timestamp = Date.currentTimeStamp
        
        let wish = WishEntity(id: NSUUID().uuidString, title: "Test wish", link: "Some link", comment: "Some commmment", isCompleted: false, createdTimestamp: timestamp, updatedTimestamp: timestamp, position: 0, tags: [])
        
        wishesRepo.insertWish(wish: wish) { error in
            if let error {
                print(error.localizedDescription)
                return
            }
            
            self.updateWishes()
        }
    }
    
    
    private func updateWishes() {
        wishesRepo.getAllWishes(isCompleted: false) { wishes, error in
            if let wishes {
                self.wishes = wishes
            }
        }
    }
}
