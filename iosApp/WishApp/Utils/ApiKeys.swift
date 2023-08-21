//
//  ApiKeys.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 20/8/23.
//

import Foundation

func valueForAPIKey(named key: String)-> String? {
    guard let path = Bundle.main.url(forResource: "AppKeys", withExtension: "plist") else { return  nil }
    guard let data = try? Data(contentsOf: path) else { return  nil }
    guard let plist = try? PropertyListSerialization.propertyList(from: data, options: [], format: nil) as? [String: Any] else { return  nil }

    
    return plist[key] as? String
}

