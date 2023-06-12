//
//  WishListMode.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared

enum WishListMode {
    case All
    case Completed
    case ByTag(TagEntity)
    case Empty
}
