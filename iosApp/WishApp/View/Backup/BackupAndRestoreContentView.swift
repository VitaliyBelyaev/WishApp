//
//  BackupAndRestoreContentView.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 28/7/24.
//

import SwiftUI
import DeviceKit

struct BackupAndRestoreContentView: View {
    
    let state: BackupViewState
    
    let onCreateBackupClicked: () -> ()
    let onRestoreBackupClicked: () -> ()
    
    func getFormattedDate(date: Date) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .short
        
        return dateFormatter.string(from: date)
    }
    
    var body: some View {
        
        
        ZStack {
            Form  {
                Section {
                    if let data = state.backupData {
                        LabeledContent("Creation date", value: getFormattedDate(date: data.updateDate))
                        
                        LabeledContent("Size", value: data.sizeFormattedString)
                        LabeledContent("Device", value: data.deviceName)
                    } else {
                        Text("No backup found")
                    }
                } header: {
                    Text("Last backup in iCloud")
                }
                
                Section {
                    Button {
                        print("onCreateBackupClicked")
                        onCreateBackupClicked()
                    } label: {
                        Text("Create backup")
                    }
                } header: {
                    Text("Create")
                } footer: {
                    Text("Creating backup will rewrite existing backup in iCloud with data from the app.")
                }
                
                if state.haveBackup {
                    Section {
                        Button {
                            onRestoreBackupClicked()
                        } label: {
                            Text("Restore backup")
                        }
                    } header: {
                        Text("Restore")
                    } footer: {
                        Text("Restore backup will rewrite current app data with data from iCloud.")
                    }
                }
                
            }
            
            FullscreenLoadingView(loadingText: "Loading", isTransparent: true, transparentOpacity: 0.7)
        }
        .navigationTitle("Backup and restore")
        .navigationBarTitleDisplayMode(.inline)

        
    }
}


struct BackupAndRestoreContentView_Previews: PreviewProvider {
    
    static var previews: some View {
        
        let noBackupState = BackupViewState()
        let backupState = BackupViewState(
            haveBackup: true,
            backupData: BackupData(updateDate: Date(timeIntervalSince1970: TimeInterval(424434344)), sizeFormattedString: "1223", deviceName: "fefefe"))
        
        NavigationStack {
            BackupAndRestoreContentView(
                state: backupState,
                onCreateBackupClicked: { },
                onRestoreBackupClicked: {}
            )
        }
    }
}
