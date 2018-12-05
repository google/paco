//
//  PacoMyExpermementTitleCellTableViewCell.swift
//  Paco
//
//  Created by Timo on 10/22/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

@objc open class PacoMyExpermementTitleCellTableViewCell: UITableViewCell {

 
 
    
    @IBOutlet weak var isIos: UIImageView!
    open var  experiment:PAExperimentDAO?
    open var  jsonDict:NSDictionary?
    open var  parent:PacoExperimentProtocol?
    @IBOutlet weak var isIOSCompatible: UILabel!
 
    
  
    open var expanded = false
    var unexpandedHeight = CGFloat(44)
 
    @IBOutlet weak var subtitle: UILabel!
    @IBOutlet weak var experimentTitle: UILabel!
    
    override open func awakeFromNib() {
        super.awakeFromNib()
     /*   self.contentView.backgroundColor = UIColor.clearColor();
        
        if(self.backgroundView  != nil)
        {
            self.backgroundView!.backgroundColor = UIColor.clearColor();
        }
        self.backgroundColor = UIColor.clearColor()
        */
        
    }
    
    
 
    func email(_ experiment:PAExperimentDAO){}
    func editTime(_ experiment:PAExperimentDAO){}
    
    @IBAction func buttnClicked(_ sender: UIButton)
    {
        parent?.didSelect(experiment!)
        
    }
    
}
