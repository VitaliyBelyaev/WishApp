//
//  CommonItemView.swift
//  WishApp
//
//  Created by Vitaliy on 15.06.2023.
//

import SwiftUI

struct CommonItemView: View {
    
    let item: CommonMainItem
    
    var body: some View {
        HStack {
            switch item.type {
            case .All:
                Text("Main.all")
            case .Completed:
                Text("Main.completed")
            }
            Spacer()
            Text("\(item.count)")
        }
    }
}

struct CommonItemView_Previews: PreviewProvider {
    static var previews: some View {
        CommonItemView(item: CommonMainItem(type: .All, count: 1))
    }
}
