//
//  PacoResponseTableViewController.swift
//  Paco
//
//  Created by northropo on 11/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit
import Foundation





class PacoResponseTableViewController: UITableViewController {
    
   var selectedIndexPath : NSIndexPath?
    
      let cellID         = "cellDate"
      let cellSelectId   = "cellSelect"
      let cellText       = "cellText"
      let cellMC   = "cellMultipleChoice"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        self.tableView.registerNib(UINib(nibName:"PacoPickerTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellID)
        self.tableView.registerNib(UINib(nibName:"PacoStringSelectorTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellSelectId)
        self.tableView.registerNib(UINib(nibName:"PacoTextTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellText)
        self.tableView.registerNib(UINib(nibName:"PacoMultipleChoiceCellTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellMC)

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
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 4
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
       
        if indexPath.row == 0
        {
            let cell:PacoPickerTableViewCell
            cell = tableView.dequeueReusableCellWithIdentifier(self.cellID, forIndexPath: indexPath) as! PacoPickerTableViewCell
            cell.titleLabel.text = "Date Time"
            return cell
        }
        else if indexPath.row == 1
        {
            
           
            let   c:PacoStringSelectorTableViewCell
            c  = tableView.dequeueReusableCellWithIdentifier(self.cellSelectId, forIndexPath: indexPath) as! PacoStringSelectorTableViewCell
            c.titleLabel.text = "Selector"
            return c
        }
        else if indexPath.row == 2
        {
            
            let   c:PacoTextTableViewCell
            c  = tableView.dequeueReusableCellWithIdentifier(self.cellText, forIndexPath: indexPath) as! PacoTextTableViewCell
            c.titleLabel.text = "Text"
            return c
            
        }
        else
        {
            let c:PacoMultipleChoiceCellTableViewCell
            c  = tableView.dequeueReusableCellWithIdentifier(self.cellMC, forIndexPath: indexPath) as! PacoMultipleChoiceCellTableViewCell
            return c
            
            
            
        }
        
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let previousIndexPath = selectedIndexPath
        if indexPath == selectedIndexPath {
            selectedIndexPath = nil
        } else {
            selectedIndexPath = indexPath
        }
        
        var indexPaths : Array<NSIndexPath> = []
        if let previous = previousIndexPath {
            indexPaths += [previous]
        }
        if let current = selectedIndexPath {
            indexPaths += [current]
        }
        if indexPaths.count > 0 {
            tableView.reloadRowsAtIndexPaths(indexPaths, withRowAnimation: UITableViewRowAnimation.Automatic)
        }
    }
    
    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        (cell as! PacoTableViewExpandingCellBase).watchFrameChanges()
    }
    
    override func tableView(tableView: UITableView, didEndDisplayingCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        (cell as! PacoTableViewExpandingCellBase).ignoreFrameChanges()
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        for cell  in tableView.visibleCells()
        {
            
            if cell is PacoTableViewExpandingCellBase
            {
                 cell.ignoreFrameChanges()
            }
        }
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        
        
        
        if indexPath.row == 0 || indexPath.row == 1
        {
        
                if indexPath == selectedIndexPath {
                    
                    return PacoPickerTableViewCell.expandedHeight
                } else {
                    
                    
                    return PacoPickerTableViewCell.defaultHeight
                }
        }
        else
        {
            return 200
        }
 
    
      }
   
   
    
    
}
