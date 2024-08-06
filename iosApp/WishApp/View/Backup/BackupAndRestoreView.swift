//
//  BackupAndRestoreView.swift
//  WishApp
//
//  Created by Vitaliy on 21.06.2023.
//

import SwiftUI
import SwiftUISnackbar

struct BackupAndRestoreView: View {
    
    @EnvironmentObject private var navigationModel: NavigationModel
    
    @StateObject private var viewModel: BackupAndRestoreViewModel
    
    init() {
        _viewModel = StateObject(wrappedValue: { BackupAndRestoreViewModel() }())
    }
    
    var body: some View {

        BackupAndRestoreContentView(
            state: viewModel.state,
            loadingState: viewModel.loadingState,
            snackbarState: viewModel.snackbarState,
            showSnackbarBinding: $viewModel.showSnackbar,
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
