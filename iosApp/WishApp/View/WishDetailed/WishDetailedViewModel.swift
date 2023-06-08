//
//  WishDetailedViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 07.06.2023.
//

import Foundation
import shared
import Combine

@MainActor
final class WishDetailedViewModel: ObservableObject {
    
    private let sdk: WishAppSdk?
    
    private var dbRepository: DatabaseRepository? {
        get {
            return sdk?.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    init(sdk: WishAppSdk? = nil) {
        self.sdk = sdk
    }
}
