//
//  PacoMyExperiments.swift
//  Paco
//
//  Created by Timo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit
import Foundation

class PacoMyExperiments: UITableViewController,PacoExperimentProtocol {
    
      var  myExpriments:Array<PAExperimentDAO>?;
      var cells:NSArray = []
      let cellId = "ExperimenCellID"
      let simpleCellId = "ExperimenSimpleCellID"
      var  isRefreshed = false;
    
    
    
    
    override func viewDidLayoutSubviews() {
         let   lengthis:CGFloat    = self.bottomLayoutGuide.length
        
         let lengththat:CGFloat = self.topLayoutGuide.length
        
         self.tableView.contentInset = UIEdgeInsetsMake(lengththat, 0, lengthis, 0);
        
        
        
    }
    
    
    func showEditView(experiment:PAExperimentDAO,indexPath:NSIndexPath)
        {
            
            
        }
  
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
  
  
        
      
        
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector:#selector(PacoMyExperiments.experimentsRefreshed(_:)), name:"MyExperiments", object: nil)
      
        self.tableView.registerNib(UINib(nibName: "PacoTableViewCell", bundle: nil), forCellReuseIdentifier:cellId)
        
        self.tableView.registerNib(UINib(nibName: "PacoMyExpermementTitleCellTableViewCell", bundle: nil), forCellReuseIdentifier:simpleCellId)
        
        
        let swiftColor = UIColor(red:0.96, green:0.96, blue:0.96, alpha:1.0)
        self.tableView.backgroundColor = swiftColor
 
        
        let  networkHelper = PacoNetwork .sharedInstance()
        networkHelper.update()
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

 

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
 
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
 
        var retVal:Int = 0
        if isRefreshed
        {
            retVal =  myExpriments!.count;
        }
        else
        {
            retVal = 1;
            
        }
        return retVal
    }

 
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCellWithIdentifier(self.simpleCellId, forIndexPath: indexPath) as! PacoMyExpermementTitleCellTableViewCell
        
        if( !isRefreshed ) {
            
            
             cell.textLabel!.text = "Loading...";
            
            
        }
        else
        {
       
         cell.textLabel!.text = nil 
        
     

                var dao:PAExperimentDAO = myExpriments![indexPath.row]
                var title:String?
                var organization:String?
                var email:String?
                var description:String?
         
               if  dao.valueForKeyEx("title") != nil
               {
                   title = (dao.valueForKeyEx("title")  as? String)!
                }
                if  dao.valueForKeyEx("description")  != nil
                {
                  description = (dao.valueForKeyEx("description")  as? String)!
                }
                if !dao.isCompatibleWithIOS()
                {
                    
                    
                    
                }
                if  dao.valueForKeyEx("organization") != nil
                {
                    organization = (dao.valueForKeyEx("organization")  as? String)!
                }
                else
                {
                    organization = " "
                }
                if  dao.valueForKeyEx("contactEmail") != nil
                {
                    email = (dao.valueForKeyEx("contactEmail")  as? String)!
                }
                else
                {
                    email = " "
                    
                }
                
                
                
                let isCompatible =  dao.isCompatibleWithIOS()
                var compatibilityText:NSString;
                
                
                if isCompatible == true
                {
                    cell.isIos.hidden = true;
                }
            
                
            
            

                cell.parent = self;
                cell.experiment = dao
                cell.experimentTitle.text = title
                cell.subtitle.text = "\(organization!), \(email!)"
                cell.selectionStyle  = UITableViewCellSelectionStyle.None
                //cell.isIOSCompatible.text = compatibilityText as String
        
        }
        
        return cell;
        
    }
    
 
  
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let cell = tableView.cellForRowAtIndexPath(indexPath) as! PacoMyExpermementTitleCellTableViewCell
        
                didSelect(cell.experiment!)
                print("did select \(cell.experimentTitle.text) \n")
        }
    
    
 
    
    
    func email(experiment:PAExperimentDAO){}
    func editTime(experiment:PAExperimentDAO){}
    
    
    func experimentsRefreshed(notification: NSNotification){
        
        
      isRefreshed = true;
      let   mediator =  PacoMediator.sharedInstance();
      let   mArray:NSMutableArray  = mediator.experiments();
       myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
        
        
        
        PacoMediator.sharedInstance().replaceAllExperiments(myExpriments)
        
        print("print the notificatins \(myExpriments)");
       
        dispatch_async(dispatch_get_main_queue(), { self.tableView.reloadData()
                                                    self.tableView.setNeedsDisplay()})
        
        
      
    }
    
    
    
    func didSelect(experiment:PAExperimentDAO)
    {
        let  detailController =  PacoExperimentDetailController(nibName:"PacoExperimentDetailController",bundle:nil)
        
        if  experiment.valueForKeyEx("title") != nil
        {
            detailController.title   = (experiment.valueForKeyEx("title")  as? String)!
        }
        
        detailController.experiment = experiment;
        self.tabBarController?.navigationController?.pushViewController(detailController, animated:  true)
        //self.tabnavigationController?.pushViewController(detailController, animated: true)
    }
    
    func didClose(experiment: PAExperimentDAO)
    {
        
        
        
    }
  
    override func viewDidAppear(animated: Bool) {
        
          self.tabBarController?.navigationItem.title = "Invitations"
 
        
        let  mediator =  PacoMediator.sharedInstance()
        let mArray:NSMutableArray  = mediator.experiments()
        myExpriments = mArray as AnyObject as? [PAExperimentDAO]
        
        
        
        
        self.tableView.reloadData()
    }
    
    
    override func viewWillDisappear(animated: Bool) {
  
    }
    
}
