//
//  PacoCardView.swift
//  Paco
//
//  Created by northropo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoCardView: UIView {

  var radius: CGFloat = 2
    
    override func layoutSubviews() {
        layer.cornerRadius = radius
        let shadowPath = UIBezierPath(roundedRect: bounds, cornerRadius: radius)
        
        layer.masksToBounds = false
        layer.shadowColor = UIColor.blackColor().CGColor
        layer.shadowOffset = CGSize(width: 3, height:5);
        layer.shadowOpacity = 0.4
        layer.shadowPath = shadowPath.CGPath
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
 

}
