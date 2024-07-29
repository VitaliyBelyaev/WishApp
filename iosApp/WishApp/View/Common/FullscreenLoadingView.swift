//
//  FullscreenLoadingView.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 29/7/24.
//

import SwiftUI

struct FullscreenLoadingView: View {
    
    let loadingText: String?
    let isTransparent: Bool
    let transparentOpacity: Double
    
    init(loadingText: String?, isTransparent: Bool, transparentOpacity: Double) {
        self.loadingText = loadingText
        self.isTransparent = isTransparent
        self.transparentOpacity = transparentOpacity
    }
    
    init(loadingText: String?, isTransparent: Bool) {
        self.loadingText = loadingText
        self.isTransparent = isTransparent
        self.transparentOpacity = defaultTransaprentOpacity
    }
    
    private let defaultTransaprentOpacity: Double = 0.65
    private let opaqueOpacity: Double = 1.0
    
    var body: some View {
        
        let opacity = if isTransparent {
            transparentOpacity
        } else {
            opaqueOpacity
        }
        
        VStack {
            Spacer()
            if loadingText != nil {
                Text(loadingText!)
                    .padding(.horizontal, 32)
                    .padding(.bottom, 8)
            }
           
            ProgressView()
                .progressViewStyle(.circular)
            
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .background(.background.opacity(opacity))
    }
}

#Preview {
    FullscreenLoadingView(loadingText: "Loading", isTransparent: true)
}
