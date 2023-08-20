//
//  PrivacyPolicyView.swift
//  WishApp
//
//  Created by Vitaliy on 28.06.2023.
//

import SwiftUI
import shared

struct PrivacyPolicyView: View {
    
    let privacyPolicyUrl = PrivacyPolicy.shared.url
    
    var body: some View {
        WebView(url: URL(string: privacyPolicyUrl))
            .navigationTitle("Settings.policy")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                WishAppAnalytics.logEvent(PrivacyPolicyScreenShowEvent())
            }
    }
}

struct PrivacyPolicyView_Previews: PreviewProvider {
    static var previews: some View {
        PrivacyPolicyView()
    }
}
