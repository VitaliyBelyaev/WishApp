//
//  ScreenState.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import Foundation
import shared

struct ScreenState {
    let createItem: CreateTagItem?
    let tagItems: [TagItem]
}

struct CreateTagItem {
    let title: String
}

struct TagItem: Hashable {
    let tag: TagEntity
    let isSelected: Bool
}
