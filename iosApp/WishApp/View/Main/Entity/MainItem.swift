//
//  MainItem.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared

struct CommonMainItem : Hashable {
    let type: CommonItemType
    let title: String
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

