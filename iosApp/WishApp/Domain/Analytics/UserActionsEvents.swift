//
//  UserActionsEvents.swift
//  WishApp
//
//  Created by Vitaliy on 07.07.2023.
//

import Foundation

// Settings
class SettingsProjectUrlClickedEvent: AnalyticsEvent {
    
    var name: String = "Settings Screen - Project Url Clicked"
    
    var params: [String : Any]?
}

class SettingsRateAppClickedEvent: AnalyticsEvent {
    
    var name: String = "Settings Screen - Rate App Clicked"
    
    var params: [String : Any]?
}


// Main
class MainRenameTagClickedEvent: AnalyticsEvent {
    
    var name: String = "Main Screen - Rename Tag Clicked"
    
    var params: [String : Any]?
}

class MainDeleteTagClickedEvent: AnalyticsEvent {
    
    var name: String = "Main Screen - Delete Tag Clicked"
    
    var params: [String : Any]?
}

class MainDeleteTagConfirmedEvent: AnalyticsEvent {
    
    var name: String = "Main Screen - Delete Tag Confirmed"
    
    var params: [String : Any]?
}


// Wish List
class WishListShareClickedEvent: AnalyticsEvent {
    
    init(mode: String) {
        self.params = [
            "mode" : mode
        ]
    }
    
    var name: String = "Wish List Screen - Share Clicked"
    
    var params: [String : Any]?
}

class WishListWishMovedEvent: AnalyticsEvent {
    
    var name: String = "Wish List Screen - Wish Moved"
    
    var params: [String : Any]?
}

class WishListChangeWishCompletenessClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish List Screen - Change Wish Completeness Clicked"
    
    var params: [String : Any]?
}

class WishListDeleteWishClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish List Screen - Delete Wish Clicked"
    
    var params: [String : Any]?
}

class WishListDeleteWishConfirmedEvent: AnalyticsEvent {
    
    var name: String = "Wish List Screen - Delete Wish Confirmed"
    
    var params: [String : Any]?
}

// Wish Detailed
class WishDetailedChangeWishCompletenessClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Change Wish Completeness Clicked"
    
    var params: [String : Any]?
}

class WishDetailedDeleteWishClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Delete Wish Clicked"
    
    var params: [String : Any]?
}

class WishDetailedDeleteWishConfirmedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Delete Wish Confirmed"
    
    var params: [String : Any]?
}

class WishDetailedAddTagTextButtonClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Add Tag Text Button Clicked"
    
    var params: [String : Any]?
}

class WishDetailedAddTagIconButtonClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Add Tag Icon Button Clicked"
    
    var params: [String : Any]?
}

class WishDetailedLinkClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Link Clicked"
    
    var params: [String : Any]?
}

class WishDetailedAddLinkButtonClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Add Link Button Clicked"
    
    var params: [String : Any]?
}

class WishDetailedDeleteLinkClickedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Delete Link Clicked"
    
    var params: [String : Any]?
}

class WishDetailedDeleteLinkConfirmedEvent: AnalyticsEvent {
    
    var name: String = "Wish Detailed Screen - Delete Link Confirmed"
    
    var params: [String : Any]?
}

