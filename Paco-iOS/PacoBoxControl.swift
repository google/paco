//
//  PacoBoxControl.swift
//  Paco
//
//  Created by northropo on 11/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoBoxControl: UIView, UITableViewDataSource,UITableViewDelegate
{
    

 
    var table:UITableView
    let cellId = "ExperimenCellID"
    
    
    
    override init(frame: CGRect)
    {
        table = UITableView(frame:frame)
        super.init(frame: frame)
    }
    
    required init(coder aDecoder: NSCoder)
    {
         table = UITableView(coder: aDecoder)
         super.init(coder:aDecoder)
    }

    
    
    override func drawRect(rect: CGRect)
    {
        table.frame = self.bounds
        
    }
    

    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell
    {
             let cell = tableView.dequeueReusableCellWithIdentifier(self.cellId, forIndexPath: indexPath) as! PacoMyExpermementTitleCellTableViewCell
        return cell
    }
    
  
   func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int
   {
         var retVal:Int = 1
    
        return retVal
    
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int
    {
         var retVal:Int = 5
        
          return retVal
    }
    
    
    
 

}
