//
//  UpdateWishTagsView.swift
//  WishApp
//
//  Created by Vitaliy on 16.06.2023.
//

import SwiftUI

struct UpdateWishTagsView: View {
    
    @StateObject private var viewModel: UpdateWishTagsViewModel
    let onCloseClicked: () -> ()
    
    
    init(wishId: String, onCloseClicked: @escaping () -> ()) {
        self.onCloseClicked = onCloseClicked
        _viewModel = StateObject.init(wrappedValue: { UpdateWishTagsViewModel(wishId: wishId) }())
    }
    
    var body: some View {
        NavigationStack {
            
            //            VStack {
            //                TextField("", text: $viewModel.query)
            //                    .textFieldStyle(.roundedBorder)
            //                    .padding()
            List {
                
                
                
                if let createItem = viewModel.state.createItem {
                    HStack {
                        Image(systemName: "plus")
                        Text("Create \(createItem.title)")
                    }
                    .onTapGesture {
                        viewModel.onCreateTagClicked(title: createItem.title)
                    }
                }
                
                ForEach(viewModel.state.tagItems, id: \.self) { tagItem in
                    var isOn: Binding<Bool> = Binding(
                        get: {return tagItem.isSelected },
                        set: {value, tr in viewModel.onTagSelectedChanged(tagItem: tagItem) }
                    )
                    
                    Toggle(isOn: isOn) {
                        Text(tagItem.tag.title)
                    }
                    .toggleStyle(iOSCheckboxToggleStyle())
                    //                        .onChange(of: $isOn.wrappedValue) {
                    //
                    //                        }
                    
                }
            }
            .listStyle(.inset)
            //            }
            .navigationTitle("")
            .toolbar {
                ToolbarItem(placement: .cancellationAction){
                    Button("Close") {
                        onCloseClicked()
                    }
                }
                
            }
            .searchable(text: $viewModel.query)
            
            
        }
        
    }
}

struct iOSCheckboxToggleStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        // 1
        Button(action: {

            // 2
            configuration.isOn.toggle()

        }, label: {
            HStack {
                // 3
                configuration.label
                Spacer()
                
                let color = configuration.isOn ? Color.accentColor : Color.secondary
                Image(systemName: configuration.isOn ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(color)
            }
        })
    }
}

struct UpdateWishTagsView_Previews: PreviewProvider {
    static var previews: some View {
        UpdateWishTagsView(wishId: "", onCloseClicked: {})
    }
}
