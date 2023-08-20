//
//  MainItem.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared
import SwiftUI

struct CommonMainItem : Hashable, Identifiable {
    
    var id: Int {
        get {
            return type.hashValue + count
        }
    }
    
    let type: CommonItemType
    let count: Int
}

enum CommonItemType {
    case All
    case Completed
}

struct WishTagMainItem : Hashable {
    let tag: TagEntity
    let count: Int
}

