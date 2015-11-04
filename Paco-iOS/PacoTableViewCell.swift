//
//  PacoTableViewCell.swift
//  Paco
//
//  Created by northropo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit                      


class PacoTableViewCell: UITableViewCell {

 
    @IBOutlet weak var experimentDescription: UITextView!
    @IBOutlet weak var experimentTitle: UILabel!
    
    var  experiment:PAExperimentDAO?
    var  parent:PacoExperimentProtocol?
    
 
    @IBOutlet weak var pacoCard: PacoCardView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.contentView.backgroundColor = UIColor.clearColor();
        
         if(self.backgroundView  != nil)
        {
             self.backgroundView!.backgroundColor = UIColor.clearColor();
        }
        self.backgroundColor = UIColor.clearColor()

    }

    @IBAction func buttnClicked(sender: UIButton)
    {
        parent?.didSelect(experiment!)
       
    }
    
}
