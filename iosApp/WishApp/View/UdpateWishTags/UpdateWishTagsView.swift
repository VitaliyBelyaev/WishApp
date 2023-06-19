//
//  UpdateWishTagsView.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import SwiftUI
import SwiftUIFlowLayout

struct UpdateWishTagsView: View {
    
    @StateObject private var viewModel: UpdateWishTagsViewModel
    let onCloseClicked: () -> ()
    
    
    init(wishId: String, onCloseClicked: @escaping () -> ()) {
        self.onCloseClicked = onCloseClicked
        _viewModel = StateObject.init(wrappedValue: { UpdateWishTagsViewModel(wishId: wishId) }())
    }
    
    var body: some View {
        NavigationStack {
            List {
                if let createItem = viewModel.state.createItem {
                    Button {
                        viewModel.onCreateTagClicked(title: createItem.title)
                    } label: {
                        HStack {
                            Image(systemName: "plus")
                                .foregroundColor(.accentColor)
                            Text("Create \"\(createItem.title)\"")
                            Spacer()
                        }
                    }
                }
                
                let tagItems = viewModel.state.tagItems
                
                if !tagItems.isEmpty {
                    FlowLayout(mode: .scrollable, items: tagItems, itemSpacing: 4) { tagItem in
                        let isOn: Binding<Bool> = Binding(
                            get: {return tagItem.isSelected },
                            set: {value, tr in viewModel.onTagSelectedChanged(tagItem: tagItem) }
                        )
                        
                        Toggle(isOn: isOn) {
                            Text(tagItem.tag.title)
                        }
                        .toggleStyle(.button)
                        .buttonStyle(.bordered)
                        .foregroundColor(isOn.wrappedValue ? .primary.opacity(0.8) : .gray)
                    }
                }
            }
            .listStyle(.grouped)
            .navigationTitle("")
            .toolbar {
                ToolbarItem(placement: .primaryAction){
                    Button("done") {
                        onCloseClicked()
                    }
                }
            }
            .searchable(text: $viewModel.query, placement: .toolbar, prompt: "UpdateWishTags.prompt")
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
