//
//  PacoPickerTableViewCell.swift
//  Paco
//
//  Created by Timo on 11/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoPickerTableViewCell: PacoTableViewExpandingCellBase {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var expandedView: UIDatePicker!
    class var expandedHeight: CGFloat { get { return 230 } }
    class var defaultHeight: CGFloat  { get { return 44  } }
    
    
    override func getView() -> UIView {
        
        return expandedView
        
    }
    
    
    override func getHeight() -> CGFloat
    {
        return PacoPickerTableViewCell.expandedHeight
    }
    
    override func getNoneExpandedHeight() -> CGFloat
    {
        return PacoPickerTableViewCell.defaultHeight
    }
    
  
   
}
