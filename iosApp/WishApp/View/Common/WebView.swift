//
//  WebView.swift
//  WishApp
//
//  Created by Vitaliy on 28.06.2023.
//

import SwiftUI
import WebKit

struct WebView: UIViewRepresentable {
    
    let url: URL?

    func makeUIView(context: Context) -> WKWebView {

        return WKWebView()
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        if let url = url {
            let request = URLRequest(url: url)
            webView.load(request)
        }
    }
}
