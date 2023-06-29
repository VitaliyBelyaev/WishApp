//
//  PrivacyPolicyView.swift
//  WishApp
//
//  Created by Vitaliy on 28.06.2023.
//

import SwiftUI

struct PrivacyPolicyView: View {
    
    var body: some View {
        WebView(url: URL(string: "https://vitaliybelyaev.github.io/"))
            .navigationTitle("Settings.policy")
            .navigationBarTitleDisplayMode(.inline)
    }
}

struct PrivacyPolicyView_Previews: PreviewProvider {
    static var previews: some View {
        PrivacyPolicyView()
    }
}
