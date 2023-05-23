//
//  Utils.swift
//  WishApp
//
//  Created by Vitaliy on 05.03.2023.
//

import Foundation
import Combine

extension Date {
    static var currentTimeStamp: Int64 {
        return Int64(Date().timeIntervalSince1970 * 1000)
    }
}

extension Publisher{
    public func sinkSilently() -> AnyCancellable {
        return sink(receiveCompletion: {_ in}, receiveValue: {_ in})
    }
}