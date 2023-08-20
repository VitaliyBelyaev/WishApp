//
//  WishEntityExt.swift
//  WishApp
//
//  Created by Vitaliy on 25.05.2023.
//

import Foundation
import shared

extension WishEntity : Identifiable {
    
}

extension WishEntity {
    
    func createCopy(
        id: String,
        createdTimestamp: Int64,
        updatedTimestamp: Int64
    ) -> WishEntity {
        return WishEntity(
            id: id,
            title: self.title,
            link: self.link,
            links: self.links,
            comment: self.comment,
            isCompleted: self.isCompleted,
            createdTimestamp: createdTimestamp,
            updatedTimestamp: updatedTimestamp,
            position: self.position,
            tags: self.tags
        )
    }
}
