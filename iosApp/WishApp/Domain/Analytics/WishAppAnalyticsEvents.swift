//
//  WishAppAnalyticsEvents.swift
//  WishApp
//
//  Created by Vitaliy on 05.07.2023.
//

import Foundation


class MainScreenShowEvent: AnalyticsEvent {
    
    init(
        currentWishesCount: Int,
        completedWishesCount: Int,
        tagsCount: Int
    ) {
        self.params = [
            "current_wishes_count" : currentWishesCount,
            "completed_wishes_count" : completedWishesCount,
            "tags_count" : tagsCount,
        ]
    }
    
    var name: String = "Main Screen - Show"
    
    var params: [String : Any]?
}

class SettingsSheetShowEvent: AnalyticsEvent {
    
    var name: String = "Settings Screen - Show"
    
    var params: [String : Any]?
}

class WishListScreenShowEvent: AnalyticsEvent {
    
    var name: String = "Wish List Screen - Show"
    
    var params: [String : Any]?
}

class WishDetailedScreenShowEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Show"
    
    var params: [String : Any]?
}

class UpdateWishTagsScreenShowEvent: AnalyticsEvent {
    
    var name: String = "Update Wish Tags Screen - Show"
    
    var params: [String : Any]?
}

class RenameTagScreenShowEvent: AnalyticsEvent {
    
    var name: String = "Rename Tag Screen - Show"
    
    var params: [String : Any]?
}
