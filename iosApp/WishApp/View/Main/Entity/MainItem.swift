//
//  MainItem.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared

enum MainItem: Hashable, Identifiable {
    
    var id: Self {
           return self
       }
    
    case AllWishes(Int)
    case CompletedWishes(Int)
    case Settings
    case WishTag(TagEntity, Int)
}

