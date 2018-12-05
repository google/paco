//
//  PacoTimeSchedulerController.swift
//  Paco
//
//  Created by Northrop O'brien on 3/10/16.
//  Copyright © 2016 Paco. All rights reserved.
//

import UIKit

class PacoTimeSchedulerController: UITableViewController {
    
    
    
    
    
      var cells   = [[]];
    
    override func viewDidLoad() {
        
        self.tableView.rowHeight = UITableViewAutomaticDimension
        self.tableView.estimatedRowHeight = 44
        
        // The DatePickerCell.
        let datePickerCell = DatePickerCell(style: UITableViewCellStyle.default, reuseIdentifier: nil)
        // Cells is a 2D array containing sections and rows.
        cells = [[datePickerCell]]
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        // Get the correct height if the cell is a DatePickerCell.
        let cell = self.tableView(tableView, cellForRowAt: indexPath)
        if (cell.isKind(of: DatePickerCell.self)) {
            return (cell as! DatePickerCell).datePickerHeight()
        }
        
        return super.tableView(tableView, heightForRowAt: indexPath)
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        // Deselect automatically if the cell is a DatePickerCell.
        let cell = self.tableView(tableView, cellForRowAt: indexPath)
        if (cell.isKind(of: DatePickerCell.self)) {
            let datePickerTableViewCell = cell as! DatePickerCell
            datePickerTableViewCell.selectedInTableView(tableView)
            self.tableView.deselectRow(at: indexPath, animated: true)
        }
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return cells.count
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cells[section].count
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        return cells[(indexPath as NSIndexPath).section][(indexPath as NSIndexPath).row] as! UITableViewCell
    }


    

}
