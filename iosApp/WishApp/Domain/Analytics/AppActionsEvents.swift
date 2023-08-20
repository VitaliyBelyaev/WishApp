//
//  AppActionsEvents.swift
//  WishApp
//
//  Created by Vitaliy on 20.07.2023.
//

import Foundation

class InAppReviewRequestedEvent: AnalyticsEvent {
    
    var name: String = "InApp Review Requested"
    
    var params: [String : Any]?
}
