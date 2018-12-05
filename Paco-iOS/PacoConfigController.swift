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
        
      
       self.tableView.register(UITableViewCell.self, forCellReuseIdentifier: "reuseIdentifier")
        cells = ["Refresh Experiments", "Settings", "User Guide", "Email Paco Team", "About Paco", "User Agreement", "Open Source Libraries"]

        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
             }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
               return 1
    
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
      
        return cells.count
    }

  
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier", for: indexPath)

        cell.textLabel!.text = (cells[(indexPath as NSIndexPath).row] as! String)
        cell.textLabel!.textColor = UIColor(red:0.00, green:0.60, blue:1.00, alpha:1.0)
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        
        
        
        
        
        switch (indexPath as NSIndexPath).row  {
        case 0:
            
            
            
           print("first ")
            
        default:
            
            print("not first")
        }
       
    }
    
    
    
    override func viewWillAppear(_ animated: Bool) {
        self.tabBarController?.navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        
        self.tabBarController?.navigationController?.setNavigationBarHidden(false, animated: false)
        
    }
  

    
}
