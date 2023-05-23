//
//  MainViewModel.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine

@MainActor
final class MainViewModel: ObservableObject {
    
    private let sdk: WishAppSdk?
    
    private var dbRepository: DatabaseRepository {
        get {
            guard let sdk else { fatalError() }
            return sdk.databaseRepository
        }
    }
    
    private var subscriptions: [AnyCancellable] = []
    
    @Published var wishes: [WishEntity] = []
    
    init(sdk: WishAppSdk?) {
        self.sdk = sdk
        self.collectWishes()
    }
    
    
    func onAddWishClicked() {
        let timestamp = Date.currentTimeStamp
        
        let wish = WishEntity(id: NSUUID().uuidString, title: "Test wish", link: "Some link", comment: "Some commmment", isCompleted: false, createdTimestamp: timestamp, updatedTimestamp: timestamp, position: 0, tags: [])
        
    
        createFuture(for: dbRepository.insertWish(wish: wish))
            .subscribe(on: DispatchQueue.global())
            .sinkSilently()
            .store(in: &subscriptions)
    }
    
    
    private func collectWishes() {
        createPublisher(for: dbRepository.observeAllWishes(isCompleted: false))
            .subscribe(on: DispatchQueue.global())
            .retry(3)
            .catch { error in
                Just([])
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.wishes, on: self)
            .store(in: &subscriptions)
    }
}

/**
 * Copy of kotlinx.coroutines.flow.internal.NopCollector
 */
class NopCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    func emit(value: Any?) async throws {
        // does nothing
    }
}

/**
 * Copy of kotlinx.coroutines.flow.collect extension function
 */
extension Kotlinx_coroutines_coreFlow {
    func collect() async {
        try? await collect(collector: NopCollector<Any?>())
    }
}

class Collector<T>: Kotlinx_coroutines_coreFlowCollector {
    
    
    let callback:(T) -> Void

    init(callback: @escaping (T) -> Void) {
        self.callback = callback
    }
    
    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        // do whatever you what with the emitted value
        callback(value as! T)

        // after you finished your work you need to call completionHandler to
        // tell that you consumed the value and the next value can be consumed,
        // otherwise you will not receive the next value
        //
        // i think first parameter can be always nil or KotlinUnit()
        // second parameter is for an error which occurred while consuming the value
        // passing an error object will throw a NSGenericException in kotlin code, which can be handled or your app will crash
        completionHandler(nil)
    }
}
