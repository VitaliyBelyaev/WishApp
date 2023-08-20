//
//  DateExtension.swift
//  WishApp
//
//  Created by Vitaliy on 20.06.2023.
//

import Foundation

extension Date {
    static var currentTimeStamp: Int64 {
        return Int64(Date().timeIntervalSince1970 * 1000)
    }
}
