//
//  PacoJoinedExperimentsTableViewCell.swift
//  Paco
//
//  Created by northropo on 11/2/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoJoinedExperimentsTableViewCell: UITableViewCell  {
    
    
    var  experiment:PAExperimentDAO?
    var  parent:PacoExperimentProtocol?
    
   // @IBOutlet weak var subtitle: UILabel!
    @IBOutlet weak var experimentTitle: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        self.contentView.clipsToBounds = true
        self.clipsToBounds = true
        
        close.clipsToBounds = true
        // Initialization code
        
 
        
        
    
        
    }
  
    
    @IBOutlet weak var creator: UILabel!
    
    
    @IBAction func changeSchedule(sender: AnyObject)
    {
        self.parent!.editTime(self.experiment!)
        
    }
    
    @IBAction func email(sender: AnyObject)
    {
        self.parent!.email(self.experiment!)
        
    }
    
    
  
    @IBOutlet weak var close: UIButton!
 
    @IBOutlet weak var changeSchedule: UIButton!

    @IBOutlet weak var email: UIButton!
  
    @IBAction func close(sender: AnyObject) {
        
         parent?.didClose(experiment!)
    }
 
    
}
