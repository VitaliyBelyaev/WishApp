//
//  NavSegment.swift
//  WishApp
//
//  Created by Vitaliy on 14.06.2023.
//

import Foundation


enum MainNavSegment: Codable, Hashable {
    case WishList(WishListMode)
    case WishDetailed(String?)
}

extension MainNavSegment {
    
    static func createFromCommonMainItem(item: CommonMainItem) -> MainNavSegment {
        switch item.type {
        case .All:
            return MainNavSegment.WishList(.All)
        case .Completed:
            return MainNavSegment.WishList(.Completed)
        }
    }
    
    static func createFromWishTagMainItem(item: WishTagMainItem) -> MainNavSegment {
        return MainNavSegment.WishList(.ByTag(item.tag.id))
    }
    
    static func createFromWishId(id: String?) -> MainNavSegment {
        return MainNavSegment.WishDetailed(id)
    }
}


