//
//  SettingsView.swift
//  WishApp
//
//  Created by Vitaliy on 12.06.2023.
//

import SwiftUI

struct SettingsView: View {
        
    var onCloseClicked: () -> () = {}
    
    var body: some View {
        NavigationStack {
            Form {
                Text("Hello")
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction){
                    Button("Close") {
                        onCloseClicked()
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    
    static var previews: some View {
        SettingsView()
    }
}
