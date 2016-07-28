//
//  PacoMyExperiments.swift
//  Paco
//
//  Created by Timo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoMyExperiments: UITableViewController,PacoExperimentProtocol {
    
      var  myExpriments:Array<PAExperimentDAO>?;
      var cells:NSArray = []
      let cellId = "ExperimenCellID"
      let simpleCellId = "ExperimenSimpleCellID"
    
    
     
    
    func showEditView(experiment:PAExperimentDAO,indexPath:NSIndexPath)
        {
            
            
        }
  
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
  
      tableView.tableFooterView = UIView()
        
        
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
        if myExpriments  != nil
        {
            retVal =  myExpriments!.count
        }
        return retVal
    }

 
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCellWithIdentifier(self.simpleCellId, forIndexPath: indexPath) as! PacoMyExpermementTitleCellTableViewCell

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
          compatibilityText  = "Y"
        }
        else
        {
           compatibilityText = "N"
        }
        
        
        

        cell.parent = self;
        cell.experiment = dao
        cell.experimentTitle.text = title
        cell.subtitle.text = "\(organization!), \(email!)"
        cell.selectionStyle  = UITableViewCellSelectionStyle.None
        cell.isIOSCompatible.text = compatibilityText as String
        
        
        
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
        
      let   mediator =  PacoMediator.sharedInstance();
      let   mArray:NSMutableArray  = mediator.experiments();
       myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
        
        
        
        PacoMediator.sharedInstance().replaceAllExperiments(myExpriments)
        
        print("print the notificatins \(myExpriments)");
        self.tableView.reloadData()
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
  
    override func viewWillAppear(animated: Bool) {
        
        let  mediator =  PacoMediator.sharedInstance()
        let mArray:NSMutableArray  = mediator.experiments()
        myExpriments = mArray as AnyObject as? [PAExperimentDAO]
        
        self.tableView.reloadData()
    }
    
}
