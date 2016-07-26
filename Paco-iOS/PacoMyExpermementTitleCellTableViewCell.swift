//
//  PacoMyExpermementTitleCellTableViewCell.swift
//  Paco
//
//  Created by Timo on 10/22/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

@objc public class PacoMyExpermementTitleCellTableViewCell: UITableViewCell {

 

    public var  experiment:PAExperimentDAO?
    public var  jsonDict:NSDictionary?
    public var  parent:PacoExperimentProtocol?
    @IBOutlet weak var isIOSCompatible: UILabel!
 
    
  
    public var expanded = false
    var unexpandedHeight = CGFloat(44)
 
    @IBOutlet weak var subtitle: UILabel!
    @IBOutlet weak var experimentTitle: UILabel!
    
    override public func awakeFromNib() {
        super.awakeFromNib()
     /*   self.contentView.backgroundColor = UIColor.clearColor();
        
        if(self.backgroundView  != nil)
        {
            self.backgroundView!.backgroundColor = UIColor.clearColor();
        }
        self.backgroundColor = UIColor.clearColor()
        */
        
    }
    
    
 
    func email(experiment:PAExperimentDAO){}
    func editTime(experiment:PAExperimentDAO){}
    
    @IBAction func buttnClicked(sender: UIButton)
    {
        parent?.didSelect(experiment!)
        
    }
    
}
