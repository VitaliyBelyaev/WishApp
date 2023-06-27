//
//  SettingsView.swift
//  WishApp
//
//  Created by Vitaliy on 12.06.2023.
//

import SwiftUI
import MessageUI

struct SettingsView: View {
    
    var onCloseClicked: () -> () = {}
    @State var emailResult: Result<MFMailComposeResult, Error>? = nil
    @State var isWriteEmailPresented = false
    
    var body: some View {
        NavigationStack {
            List {
                Section {
                    SettingsItemView(title: "Settings.writeToSupport") {
                        isWriteEmailPresented = true
                    }
                    .disabled(!MFMailComposeViewController.canSendMail())
                    .sheet(isPresented: $isWriteEmailPresented) {
                        MailView(content: getDebugInfoForFeedbackEmail(),
                                 to: "vitaliy.belyaev.wishapp@gmail.com",
                                 subject: "Feedback")
                    }
                    
                    SettingsItemView(title: "Settings.rateUs") {
                        if let url = URL(string: "https://google.com"),
                           UIApplication.shared.canOpenURL(url){
                            UIApplication.shared.open(url)
                        }
                    }
                }
                
                Section{
                    Text("Settings.description")
                        .font(.body)
                } footer: {
                    HStack {
                        Spacer()
                        Text("Settings.appVersion \(Bundle.main.appVersionPretty)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Spacer()
                    }.padding(.top)
                    
                }.textCase(nil)
                
            }
            .environment(\.defaultMinListRowHeight, 48)
            .toolbar {
                ToolbarItem(placement: .cancellationAction){
                    Button("close") {
                        onCloseClicked()
                    }
                }
            }
            .navigationTitle("Settings.title")
            .navigationBarTitleDisplayMode(.large)
        }
    }
    
    private func getDebugInfoForFeedbackEmail() -> String {
        let device = UIDevice()
        let appVersionPart = "App version: \(Bundle.main.appVersionPretty)"
        let osPart = "OS: \(device.systemName) \(device.systemVersion)"
        let devicePart = "Device: \(UIDevice.deviceModelCodeString)"
        return "\n\n\(appVersionPart)\n\(osPart)\n\(devicePart)"
    }
}

struct SettingsView_Previews: PreviewProvider {
    
    static var previews: some View {
        SettingsView()
    }
}
