//
//  NeedToRequestReviewUseCase.swift
//  WishApp
//
//  Created by Vitaliy on 20.07.2023.
//

import Foundation


struct NeedToRequestReviewUseCase {
    
    static func invoke(positiveActionsCount: Int) -> Bool {
        return positiveActionsCount != 0 && positiveActionsCount % 10 == 0
    }
}
