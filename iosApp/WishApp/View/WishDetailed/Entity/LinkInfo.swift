//
//  LinkInfo.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import Foundation

struct LinkInfo : Hashable {
    let title: String
    let link: String
    
    func getMdText() -> String {
        return "[\(title)](\(link))"
    }
}
