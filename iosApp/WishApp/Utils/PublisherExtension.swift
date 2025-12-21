//
//  PublisherExtension.swift
//  WishApp
//
//  Created by Vitaliy on 20.06.2023.
//

import Foundation
import Combine

extension Publisher {
    
    public func sinkSilently() -> AnyCancellable {
        return sink(receiveCompletion: {_ in}, receiveValue: {_ in})
    }
    
    public func sinkIgnoringCompletion(receiveValue: @escaping ((Self.Output) -> Void)) -> AnyCancellable {
        return sink(receiveCompletion: {_ in}, receiveValue: receiveValue)
    }
    
    public func sinkIgnoringReceivedValue(receiveCompletion: @escaping ((Subscribers.Completion<Self.Failure>) -> Void)) -> AnyCancellable {
        return sink(receiveCompletion: receiveCompletion, receiveValue: {_ in})
    }
}
