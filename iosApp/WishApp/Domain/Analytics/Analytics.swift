//
//  Analytics.swift
//  WishApp
//
//  Created by Vitaliy on 30.06.2023.
//

import Foundation
import FirebaseAnalytics
import Amplitude


class WishAppAnalytcis {
    
    private static let firebase = Analytics.self
    private static let amplitude = Amplitude.instance()
    
    static func logEvent(name: String, params: [String: Any]? = nil) {
        amplitude.logEvent(name, withEventProperties: params)
        firebase.logEvent(name, parameters: params)
    }
    
    static func logEvent(_ event: AnalyticsEvent) {
        amplitude.logEvent(event.name, withEventProperties: event.params)
        firebase.logEvent(event.name, parameters: event.params)
    }
}
