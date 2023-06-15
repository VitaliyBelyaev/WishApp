//
//  SectionHeaderView.swift
//  WishApp
//
//  Created by Vitaliy on 15.06.2023.
//

import SwiftUI

struct SectionHeaderView: View {
    
    let title: LocalizedStringKey
    @Binding var isOn: Bool
    @State private var rotaitionDegrees = 0.0
    
    var body: some View {
        HStack {
            Text(title)
                .font(.title3)
                .fontWeight(.medium)
                .foregroundColor(.primary)
                .textCase(nil)
            
            Spacer()
            
            Button {
                withAnimation {
                    isOn.toggle()
                }
            } label: {
                Image(systemName: "chevron.down")
                
                    .rotationEffect(.degrees(rotaitionDegrees))
            }
            .onChange(of: isOn) { isOn in
                if(isOn){
                    rotaitionDegrees = 0.0
                } else{
                    rotaitionDegrees = -90.0
                }
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            withAnimation {
                isOn.toggle()
            }
        }
    }
}
struct SectionHeaderView_Previews: PreviewProvider {
    
    @State static var isOn: Bool = true
    
    static var previews: some View {
        SectionHeaderView(
            title: "", isOn: $isOn
        )
    }
}
