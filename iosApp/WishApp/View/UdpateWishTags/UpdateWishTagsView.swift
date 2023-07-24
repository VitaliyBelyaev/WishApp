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
    
    let onCloseClicked: () -> ()
    
    init(wishId: String, onCloseClicked: @escaping () -> ()) {
        self.onCloseClicked = onCloseClicked
        _viewModel = StateObject.init(wrappedValue: { UpdateWishTagsViewModel(wishId: wishId) }())
    }
    
    var body: some View {
        
        NavigationStack {
            UpdateWishTagsContentView(
                query: $viewModel.query,
                state: viewModel.state,
                onCreateTagClicked: {viewModel.onCreateTagClicked(title: $0)},
                onTagSelectedChanged: { viewModel.onTagSelectedChanged(tagItem: $0) },
                onCloseClicked: onCloseClicked
            )
        }
    }
}

struct iOSCheckboxToggleStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        Button {
            configuration.isOn.toggle()
        } label: {
            HStack {
                configuration.label
                Spacer()
                
                let color = configuration.isOn ? Color.accentColor : Color.secondary
                Image(systemName: configuration.isOn ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(color)
                    .font(.system(size: 20, weight: .regular))
            }
        }
    }
}

struct UpdateWishTagsView_Previews: PreviewProvider {
    static var previews: some View {
        UpdateWishTagsView(wishId: "", onCloseClicked: {})
    }
}
