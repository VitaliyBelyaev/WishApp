//
//  AnalyticsEvent.swift
//  WishApp
//
//  Created by Vitaliy on 05.07.2023.
//

import Foundation

protocol AnalyticsEvent {
    
    var name: String { get }
    
    var params: [String: Any]? { get }
}
