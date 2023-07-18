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
}

extension Publisher {
    public func sinkIgnoringCompletion(receiveValue: @escaping ((Self.Output) -> Void)) -> AnyCancellable {
        return sink(receiveCompletion: {_ in}, receiveValue: receiveValue)
    }
}