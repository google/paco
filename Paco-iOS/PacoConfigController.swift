//
//  PacoConfigController.swift
//  Paco
//
//  Created by Timo on 10/29/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoConfigController: UITableViewController {
    
    
    
   
    
     var cells:NSArray = []
 
    
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
      
       self.tableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: "reuseIdentifier")
        cells = ["Refresh Experiments", "Settings", "User Guide", "Email Paco Team", "About Paco", "User Agreement", "Open Source Libraries"]

        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
             }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
               return 1
    
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
      
        return cells.count
    }

  
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCellWithIdentifier("reuseIdentifier", forIndexPath: indexPath) as! UITableViewCell

        cell.textLabel!.text = (cells[indexPath.row] as! String)
        cell.textLabel!.textColor = UIColor(red:0.00, green:0.60, blue:1.00, alpha:1.0)
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        
        
        
        
        
        switch indexPath.row  {
        case 0:
            
            
            
           print("first ")
            
        default:
            
            print("not first")
        }
       
    }
  

    
}
