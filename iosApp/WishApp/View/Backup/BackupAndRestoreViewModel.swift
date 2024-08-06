//
//  BackupAndRestoreViewModel.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 29/3/24.
//

import Foundation
import shared
import SwiftUISnackbar

@MainActor
final class BackupAndRestoreViewModel: ObservableObject {
    
    @Published var state: BackupViewState = BackupViewState()
    @Published var loadingState: BackupLoadingState = BackupLoadingState.none
    @Published var snackbarState: SnackbarState = SnackbarState.none
    @Published var showSnackbar: Bool = false
    
    private let sdk: WishAppSdk = WishAppSdkDiHelper().wishAppSdk
    
    private let dbFilesManager = DBLocalFilesManager()
    private let dbICloudFilesManager = DBICloudFilesManager()
    
    private var createBackupTask: Task<Void, Never>? = nil
    private var restoreBackupTask: Task<Void, Never>? = nil
    private var checkBackupTask: Task<Void, Never>? = nil
    
    init() {
        checkBackupTask = Task {
            self.loadingState = BackupLoadingState.standard
            await updateState()
            self.loadingState = BackupLoadingState.none
        }
    }
    
    deinit {
        self.createBackupTask?.cancel()
        self.restoreBackupTask?.cancel()
        self.checkBackupTask?.cancel()
    }
    
    func onRestoreClicked() {
        restoreBackupTask = Task {
            do {
                self.loadingState = BackupLoadingState.restoreBackup
                
                try await dbICloudFilesManager.restoreBackup(originDbName: sdk.databaseName, sdk: sdk)
                
                self.loadingState = BackupLoadingState.none
            } catch {
                self.loadingState = BackupLoadingState.none
                print("Error restore backup: \(error)")
            }
        }
    }
    
    func onCreateBackupClicked() {
        createBackupTask = Task {
            do {
                self.loadingState = BackupLoadingState.createBackup
                
                try? await Task.sleep(nanoseconds: 20000000)
                
                throw CreateBackupError.removeOldBackupInICloudError(nil)
                
                try await dbICloudFilesManager.crateBackup(originDbName: sdk.databaseName)
                await updateState()
                
                self.loadingState = BackupLoadingState.none
                
                showSnackbar(text: "Backup created successfully", isError: false)
            } catch {
                self.loadingState = BackupLoadingState.none
                showSnackbar(text: "Error creating backup", isError: true)
                print("Error create backup: \(error)")
            }
        }
    }
    
    private func updateState() async {
        if let backupData: BackupData = await dbICloudFilesManager.getBackupData() {
            self.state = BackupViewState(haveBackup: true, backupData: backupData)
        } else {
            self.state = BackupViewState()
        }
    }
    
    private func showSnackbar(text: String, isError: Bool) {
        let snackbar = if isError {
            SnackbarState.error(text)
        } else {
            SnackbarState.info(text)
        }
        self.snackbarState = snackbar
        self.showSnackbar = true
    }
}
