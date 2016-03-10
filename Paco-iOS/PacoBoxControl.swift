//
//  PacoBoxControl.swift
//  Paco
//
//  Created by Timo on 11/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit



class PacoBoxControl: UIView, UITableViewDataSource,UITableViewDelegate,UIGestureRecognizerDelegate
{
    
    var   input:PAInput2?
    var singleSelect:Bool = false
    var listChoices:JavaUtilList?
 
    var table:UITableView
    let cellId = "ExperimenCellID"
    
    let CHECKED = "\u{2611}"
    let UNCHECKED = "\u{2B1C}"
    
    
    let CHECKED2 = "\u{2705}"
    let  UNCHECKED2 = "\u{2B1C}"
    var     checkLabels = [Int:PacoCheckCell]()
     var    checks  = [Int:Bool]()
    var     labels = [String]()
    
    override init(frame: CGRect)
    {
        table = UITableView(frame:frame)
        super.init(frame: frame)
        table.scrollEnabled = false
       
    }
    
    required init(coder aDecoder: NSCoder)
    {
        table = UITableView(coder: aDecoder)!
        super.init(coder:aDecoder)!
        
        table.scrollEnabled = false
        self.backgroundColor = UIColor.whiteColor()
        table.dataSource = self
        table.delegate = self
        table.allowsSelection = false
        
        table.registerNib(UINib(nibName:"PacoCheckCell", bundle: nil), forCellReuseIdentifier:self.cellId)
        self.addSubview(table)
        table.reloadData()
       
    }

    func  reload() {
        
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
        
        var index:Int32 =  Int32(indexPath.row)
        var  itemName:String =  (listChoices?.getWithInt(index) as? String)!
        
   
        print(" \(checks) ")
        
        
        if(   checks[indexPath.row] == true)
        {
           cell.checkboxLabel.text  = "\(CHECKED) \(itemName)"
        }
        else
        {
              cell.checkboxLabel.text  = "\(UNCHECKED) \(itemName)"
            
        }
        cell.checkboxLabel.userInteractionEnabled = true
        cell.checkboxLabel!.tag = 0
        cell.checkboxLabel!.index = indexPath.row;
        
        
        let   recognizer  =  UITapGestureRecognizer(target: self,action: Selector("checkOrUncheck:"))
        recognizer.delegate = self
        recognizer.numberOfTapsRequired = 1
        cell.checkboxLabel.addGestureRecognizer(recognizer)
        
        checkLabels.updateValue(cell, forKey:indexPath.row)
        
        return cell
    }
    
  
   func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int
   {
    
    
        if(listChoices != nil)
        {
            var count:Int32 = listChoices!.size()
            var retVal:Int = Int(count)
            return retVal
         }
    else
        {
            
            return 0
         }
   
    }
    
    
    
    func checkOrUncheck(recognizer:UITapGestureRecognizer )
    {
        print( "\(checkLabels) ")
         print( "\(checks) ")
        let label = recognizer.view as! PacoLabel
         if checks[label.index] == true
         {
            
            var  itemName =  listChoices?.getWithInt(Int32(label.index)) as? String
            label.text = "\(UNCHECKED) \(itemName)"
             checks[label.index] = false
            
           
          }
        else
         {
            if singleSelect == true
            {
                for (key,value) in checks {
                    checks[key] = false
                    print("\(key) \t \(value)")
                }
            }
            
           
            checks[label.index] = true
            
             var  i:Int32 = 0
             var  itemName =  listChoices?.getWithInt(Int32(label.index)) as? String
             label.text = "\(CHECKED) \(itemName)"
          
            
            
            
        }
           print( "\(checks) ")
        table.reloadData();
        
        
        
    }
    
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int
    {
        var retVal:Int
        
        if(listChoices != nil)
        {
           var size:Int32 = listChoices!.size()
            retVal = Int(size)
        }
        else
        {
            retVal = 0
            
        }
           return  1
    }
    
    
   
    
    
    
 

}
