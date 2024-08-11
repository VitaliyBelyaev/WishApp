//
//  File.swift
//  WishApp
//
//  Created by Vitaliy Belyaev on 5/8/24.
//

import Foundation
import SwiftUISnackbar
import SwiftUI

enum SnackbarState {
    
    case none
    
    case info(String)
    
    case success(String)
    
    case error(String)
}

extension SnackbarState {
    
    func show() -> Bool {
        let result: Bool
        
        switch self {
        case .none:
            result = false
        case .info(_):
            result = true
        case .success(_):
            result = true
        case .error(_):
            result = true
        }
        return result
    }
    
    func isError() -> Bool {
        let result: Bool
        
        switch self {
        case .none:
            result = false
        case .info(_):
            result = false
        case .success(_):
            result = false
        case .error(_):
            result = true
        }
        return result
    }
    
    func backgroundColor(_ colorScheme: ColorScheme) -> Color {
        let result: Color
        
        switch self {
        case .none:
            result = Color.clear
        case .info(_):
            result = if colorScheme == .dark {
                Color.accentColor.opacity(0.95)
            } else {
                Color.accentColor.opacity(1.0)
            }
        case .success(_):
            result = if colorScheme == .dark {
                Color(red: 0.58, green: 0.84, blue: 0.65)
            } else {
                Color(red: 0.18, green: 0.42, blue: 0.27)
            }
        case .error(_):
            result = if colorScheme == .dark {
                Color(red: 1.00, green: 0.71, blue: 0.67)
            } else {
                Color(red: 0.73, green: 0.10, blue: 0.10)
            }
        }
        return result
    }
    
    func textColor(_ colorScheme: ColorScheme) -> Color {
        let result: Color
        
        switch self {
        case .none:
            result = Color.clear
        case .info(_):
            result = Color.white
        case .success(_):
            result = if colorScheme == .dark {
                Color(red: 0.00, green: 0.22, blue: 0.11)
            } else {
                Color.white
            }
        case .error(_):
            result = if colorScheme == .dark {
                Color(red: 0.41, green: 0.00, blue: 0.02)
            } else {
                Color.white
            }
        }
        return result
    }
        
    func text() -> String {
        let result: String
        
        switch self {
        case .none:
            result = ""
        case .info(let text):
            result = text
        case .success(let text):
            result = text
        case .error(let text):
            result = text
        }
        return result
    }
}
