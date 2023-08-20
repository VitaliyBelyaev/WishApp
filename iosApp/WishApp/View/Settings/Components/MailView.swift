//
//  MailView.swift
//  WishApp
//
//  Created by Vitaliy on 24.06.2023.
//

import SwiftUI
import UIKit
import MessageUI

struct MailView : UIViewControllerRepresentable {
    
    var content: String
    var to: String
    var subject: String
    
    typealias UIViewControllerType = MFMailComposeViewController
    
    func updateUIViewController(_ uiViewController: MFMailComposeViewController, context: Context) {
        
    }

    func makeUIViewController(context: Context) -> MFMailComposeViewController {
        if MFMailComposeViewController.canSendMail(){
            let view = MFMailComposeViewController()
            view.mailComposeDelegate = context.coordinator
            view.setToRecipients([to])
            view.setSubject(subject)
            view.setMessageBody(content, isHTML: false)
            return view
        } else {
            return MFMailComposeViewController()
        }
    }
    
    func makeCoordinator() -> Coordinator {
        return Coordinator(self)
    }
    
    
    class Coordinator : NSObject, MFMailComposeViewControllerDelegate{
        
        var parent : MailView
        
        init(_ parent: MailView){
            self.parent = parent
        }
        
        func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
            controller.dismiss(animated: true)
        }
    }
}
