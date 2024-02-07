//
//  BackupAndRestoreView.swift
//  WishApp
//
//  Created by Vitaliy on 21.06.2023.
//

import SwiftUI

struct BackupAndRestoreView: View {
    var body: some View {
        VStack(alignment: .leading) {
            
            Text("Last backup")
                .font(.headline)
                .padding(.bottom)
        
            Button {
                
            } label: {
                HStack {
                    Text("Create backup")
                    Spacer()
                }
            }
            
        
            Text("Restore")
                .font(.headline)
                .padding(.vertical)
        
            Button {
                
            } label: {
                HStack {
                    Text("Restore backup")
                    Spacer()
                }
            }
            
            Spacer()
        }
        .padding()
        .navigationTitle("Backup and restore")
        .navigationBarTitleDisplayMode(.inline)
        
    }
}

struct BackupAndRestoreView_Previews: PreviewProvider {
    static var previews: some View {
        BackupAndRestoreView()
    }
}
