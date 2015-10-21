//
//  PacoTableViewCell.swift
//  Paco
//
//  Created by northropo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoTableViewCell: UITableViewCell {

    override func awakeFromNib() {
        super.awakeFromNib()
        self.contentView.backgroundColor = UIColor.clearColor();
        
         if(self.backgroundView  != nil)
        {
          
      
             self.backgroundView!.backgroundColor = UIColor.clearColor();
        }
       self.backgroundColor = UIColor.clearColor()
        
        //[[cell contentView] setBackgroundColor:[UIColor clearColor]];
        //[[cell backgroundView] setBackgroundColor:[UIColor clearColor]];
        //[cell setBackgroundColor:[UIColor clearColor]];
        // Do any additional setup after loading the view.
        
        
    }

 
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
