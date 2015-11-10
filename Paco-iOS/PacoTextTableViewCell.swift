//
//  PacoTextTableViewCell.swift
//  Paco
//
//  Created by northropo on 11/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoTextTableViewCell:PacoTableViewExpandingCellBase {
    
    
    class var expandedHeight: CGFloat { get { return 230 } }
    class var defaultHeight: CGFloat  { get { return 44  } }
    
    
    override func getHeight() -> CGFloat
    {
        return PacoPickerTableViewCell.expandedHeight
    }
    
    override func getNoneExpandedHeight() -> CGFloat
    {
        return PacoPickerTableViewCell.defaultHeight
    }
    
    
    

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var questionText: UITextView!
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

           }
    
}
