//
//  ScreenShowEvents.swift
//  WishApp
//
//  Created by Vitaliy on 07.07.2023.
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
    
    init(mode: String) {
        self.params = [
            "mode" : mode
        ]
    }
    
    var name: String = "Wish List Screen - Show"
    
    var params: [String : Any]?
}

class WishDetailedScreenShowEvent: AnalyticsEvent {
    
    init(fromScreen: String, isNewWish: Bool) {
        self.params = [
            "from_screen" : fromScreen,
            "is_new_wish" : isNewWish
        ]
    }
    
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

class PrivacyPolicyScreenShowEvent: AnalyticsEvent {
    
    var name: String = "Privacy Policy Screen - Show"
    
    var params: [String : Any]?
}
