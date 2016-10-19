//
//  PacoJoinedExperimentsTableViewCell.swift
//  Paco
//
//  Created by Timo on 11/2/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoJoinedExperimentsTableViewCell: UITableViewCell  {
    
    
    @IBOutlet weak var editBtn: UILabel!
    var  experiment:PAExperimentDAO?
    var  parent:PacoExperimentProtocol?
    var  indexPath:IndexPath?
    
    
   // @IBOutlet weak var subtitle: UILabel!
    @IBOutlet weak var experimentTitle: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        self.contentView.clipsToBounds = true
        self.clipsToBounds = true
        
        close.clipsToBounds = true
        
        
        
        
    }
    @IBOutlet weak var edit: UIButton!
    
    
    
    @IBAction func edit(_ sender: AnyObject)
    {
        parent!.showEditView(self.experiment!,indexPath:indexPath!)
       
    }
    
    

    
    @IBOutlet weak var creator: UILabel!
    
    
    @IBAction func changeSchedule(_ sender: AnyObject)
    {
        self.parent!.editTime(self.experiment!)
        
    }
    
    @IBAction func email(_ sender: AnyObject)
    {
        self.parent!.email(self.experiment!)
        
    }
    
    
  
    @IBOutlet weak var close: UIButton!
 
    @IBOutlet weak var changeSchedule: UIButton!

    @IBOutlet weak var email: UIButton!
  
    @IBAction func close(_ sender: AnyObject) {
        
         parent?.didClose(experiment!)
    }
 
    
}
