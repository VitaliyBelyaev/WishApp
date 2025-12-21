//
//  UpdateWishTagsView.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import SwiftUI
import SwiftUIFlowLayout

struct UpdateWishTagsView: View {
    
    @AppStorage(wrappedValue: 0, UserDefaultsKeys.positiveActionsCount)
    private var positiveActionsCount: Int
    
    @StateObject private var viewModel: UpdateWishTagsViewModel
    
    init(wishId: String) {
        _viewModel = StateObject.init(wrappedValue: { UpdateWishTagsViewModel(wishId: wishId) }())
    }
    
    var body: some View {
        UpdateWishTagsContentView(
            query: $viewModel.query,
            state: viewModel.state,
            onCreateTagClicked: {viewModel.onCreateTagClicked(title: $0)},
            onTagSelectedChanged: { viewModel.onTagSelectedChanged(tagItem: $0) }
        )
    }
}

struct UpdateWishTagsView_Previews: PreviewProvider {
    static var previews: some View {
        UpdateWishTagsView(wishId: "")
    }
}
