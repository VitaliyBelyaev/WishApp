//
//  UIDeviceExtension.swift
//  WishApp
//
//  Created by Vitaliy on 26.06.2023.
//

import Foundation
import UIKit

extension UIDevice {
    
    /// Obtain the machine hardware platform from the `uname()` unix command
    ///
    /// Example of return values
    ///  - `"iPhone8,1"` = iPhone 6s
    ///  - `"iPad6,7"` = iPad Pro (12.9-inch)
    static var deviceModelCodeString: String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let modelCode = withUnsafePointer(to: &systemInfo.machine) {
            $0.withMemoryRebound(to: CChar.self, capacity: 1) {
                ptr in String.init(validatingUTF8: ptr)
            }
        }
        return modelCode ?? "N/A"
    }
}
