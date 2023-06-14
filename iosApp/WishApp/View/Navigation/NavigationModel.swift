//
//  NavigationModel.swift
//  WishApp
//
//  Created by Vitaliy on 27.05.2023.
//

import Foundation
import SwiftUI
import Combine

final class NavigationModel: ObservableObject, Codable {
    
    @Published var mainPath: [MainNavSegment]
    
    @Published var isSettingPresented: Bool
    @Published var settingsPath: NavigationPath
    
    private lazy var decoder = JSONDecoder()
    private lazy var encoder = JSONEncoder()

    init(mainPath: [MainNavSegment] = [],
         isSettingPresented: Bool = false,
         settingsPath: NavigationPath = NavigationPath()
    ) {
        self.mainPath = mainPath
        self.isSettingPresented = isSettingPresented
        self.settingsPath = settingsPath
    }

    var jsonData: Data? {
        get { try? encoder.encode(self) }
        set {
            guard let data = newValue,
                  let model = try? decoder.decode(Self.self, from: data)
            else { return }
            mainPath = model.mainPath
            isSettingPresented = model.isSettingPresented
            settingsPath = model.settingsPath
        }
    }
    
    var objectWillChangeSequence: AsyncPublisher<Publishers.Buffer<ObservableObjectPublisher>> {
        objectWillChange
            .buffer(size: 1, prefetch: .byRequest, whenFull: .dropOldest)
            .values
    }

    required init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        self.mainPath = try container.decode([MainNavSegment].self, forKey: .mainPath)
        
        let settingsPathRepresentation = try container.decode(
            NavigationPath.CodableRepresentation.self, forKey: .settingPath)
        self.settingsPath = NavigationPath(settingsPathRepresentation)

        self.isSettingPresented = try container.decode(
            Bool.self, forKey: .isSettingsPresented)
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(mainPath, forKey: .mainPath)
        try container.encode(isSettingPresented, forKey: .isSettingsPresented)
        try container.encode(settingsPath.codable, forKey: .settingPath)
    }

    enum CodingKeys: String, CodingKey {
        case isSettingsPresented
        case mainPath
        case settingPath
    }
}
