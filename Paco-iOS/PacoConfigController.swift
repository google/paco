//
//  PacoConfigController.swift
//  Paco
//
//  Created by northropo on 10/29/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoConfigController: UITableViewController {
    
    
    
   
    
     var cells:NSArray = []
    
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
      
      self.tableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: "reuseIdentifier")
        
        
        
        cells = ["Refresh Experiments", "Settings", "User Guide", "Email Paco Team", "About Paco", "User Agreement", "Open Source Libraries"]

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        return 1
    
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete method implementation.
        // Return the number of rows in the section.
        return cells.count
    }

  
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("reuseIdentifier", forIndexPath: indexPath) as! UITableViewCell

        cell.textLabel!.text = (cells[indexPath.row] as! String)
        cell.textLabel!.textColor = UIColor(red:0.00, green:0.60, blue:1.00, alpha:1.0)
        return cell
    }
  

    
}
