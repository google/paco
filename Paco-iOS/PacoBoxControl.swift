//
//  PacoBoxControl.swift
//  Paco
//
//  Created by northropo on 11/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoBoxControl: UIView, UITableViewDataSource,UITableViewDelegate,UIGestureRecognizerDelegate
{
    

 
    var table:UITableView
    let cellId = "ExperimenCellID"
    let CHECKED = "\u{2705}"
    let  UNCHECKED = "\u{2B1C}"
    
    
    override init(frame: CGRect)
    {
        table = UITableView(frame:frame)
        super.init(frame: frame)
    }
    
    required init(coder aDecoder: NSCoder)
    {
        table = UITableView(coder: aDecoder)
        super.init(coder:aDecoder)
        
        self.backgroundColor = UIColor.whiteColor()
  
        table.dataSource = self
        table.delegate = self
        table.allowsSelection = false
        
        table.registerNib(UINib(nibName:"PacoCheckCell", bundle: nil), forCellReuseIdentifier:self.cellId)
        self.addSubview(table)
        table.reloadData()
       
    }

    
    
    override func drawRect(rect: CGRect)
    {
        table.frame = self.bounds
        
    }
    
   func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
    
    return 40
    
    }
    
    
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell
    {
             let cell = tableView.dequeueReusableCellWithIdentifier(self.cellId, forIndexPath: indexPath) as! PacoCheckCell
        cell.checkboxLabel.text  = "\(CHECKED) and checked"
        cell.checkboxLabel.userInteractionEnabled = true
        
        cell.checkboxLabel!.tag = 1
        let   recognizer  =  UITapGestureRecognizer(target: self,action: Selector("checkOrUncheck:"))
        recognizer.delegate = self
        recognizer.numberOfTapsRequired = 1
        cell.checkboxLabel.addGestureRecognizer(recognizer)
    
        return cell
    }
    
  
   func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int
   {
         var retVal:Int = 1
    
        return retVal
    
    }
    
    func checkOrUncheck(recognizer:UITapGestureRecognizer )
    {
        
        let label = recognizer.view as! UILabel
         if label.tag == 1
         {
            
            label.text = "\(UNCHECKED) and unchecked"
            label.tag  = 0
          }
        else
         {
            
            label.text = "\(CHECKED) and checked"
            label.tag  = 1
            
        }
        
        
        
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int
    {
         var retVal:Int = 5
        
          return retVal
    }
    
    
    
 

}
