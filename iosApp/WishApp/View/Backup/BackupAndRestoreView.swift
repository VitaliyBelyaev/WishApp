//
//  BackupAndRestoreView.swift
//  WishApp
//
//  Created by Vitaliy on 21.06.2023.
//

import SwiftUI

struct BackupAndRestoreView: View {
    
    @EnvironmentObject private var navigationModel: NavigationModel
    
    @StateObject private var viewModel: BackupAndRestoreViewModel
    
    init() {
        _viewModel = StateObject(wrappedValue: { BackupAndRestoreViewModel() }())
    }
    
    var body: some View {
        
        BackupAndRestoreContentView(
            state: viewModel.state,
            onCreateBackupClicked: { viewModel.onCreateBackupClicked() },
            onRestoreBackupClicked: { viewModel.onRestoreClicked() }
        )        
    }
}

struct BackupAndRestoreView_Previews: PreviewProvider {
    static var previews: some View {
        BackupAndRestoreView()
    }
}
