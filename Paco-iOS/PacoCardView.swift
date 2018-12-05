//
//  PacoCardView.swift
//  Paco
//
//  Created by Timo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoCardView: UIView {

  var radius: CGFloat = 2
    
    override func layoutSubviews() {
        layer.cornerRadius = radius
        let shadowPath = UIBezierPath(roundedRect: bounds, cornerRadius: radius)
        
        layer.masksToBounds = false
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOffset = CGSize(width: 3, height:3);
        layer.shadowOpacity = 0.1
        layer.shadowPath = shadowPath.cgPath
    }
    
    required init(coder aDecoder: NSCoder) {
         super.init(coder: aDecoder)!
    }
 

}
