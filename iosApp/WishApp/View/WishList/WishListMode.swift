//
//  WishListMode.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import shared

enum WishListMode: Codable, Hashable {
    case All
    case Completed
    case ByTag(String)
    case Empty
}

extension WishListMode {
    
    var analyticsString: String {
        get {
            switch self {
            case .All:
                return "All"
            case .Completed:
                return "Completed"
            case .ByTag(_):
                return "By Tag"
            case .Empty:
                return "Empty"
            }
        }
    }
}
