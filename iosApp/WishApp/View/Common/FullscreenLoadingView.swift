//
//  FullscreenLoadingView.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 29/7/24.
//

import SwiftUI

struct FullscreenLoadingView: View {
    
    @Environment(\.colorScheme) var colorScheme
    
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
        
        ZStack {
            VStack {
                if loadingText != nil {
                    Text(loadingText!)
                        .lineLimit(2)
                        .padding(.bottom, 4)
                }
                
                ProgressView()
                    .progressViewStyle(.circular)
                
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 14)
            .background {
                let color: Color = if colorScheme == .dark {
                    Color(uiColor: UIColor.systemGray5)
                } else {
                    Color(uiColor: UIColor.systemBackground)
                }
                
                if isTransparent {
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(color.shadow(.drop(radius: 20)))
                } else {
                    Color.clear
                }
            }
            .frame(maxWidth: 350)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.background.opacity(opacity))
    }
}

#Preview {
    FullscreenLoadingView(loadingText: "Restoring data from backup...", isTransparent: true)
}
