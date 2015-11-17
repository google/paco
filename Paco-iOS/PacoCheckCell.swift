//
//  PacoCheckCell.swift
//  Paco
//
//  Created by northropo on 11/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoCheckCell: UITableViewCell {
    
    
   

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    @IBOutlet weak var checkboxLabel: PacoLabel!
 
    @IBOutlet weak var checkboxTextLabel: UILabel!
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
