//
//  PacoMyExperiments.swift
//  Paco
//
//  Created by Timo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

@objc class PacoHubExperiments: UITableViewController,PacoExperimentProtocol,UINavigationControllerDelegate {
    
      var  allExperiments:Array<PAExperimentDAO>?;
    
      var cells:NSArray = []
      let cellId = "ExperimenCellID"
      let simpleCellId = "ExperimenSimpleCellID"
      var publicIterator = PacoPublicDefinitionLoader.publicExperimentsEnumerator();
    
    
    func showEditView(experiment:PAExperimentDAO,indexPath:NSIndexPath)
        {
            
            
        }
  
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
       
            publicIterator.loadNextPage { ( nsArray  , err  ) -> Void in
            let  counter:Int   =  nsArray.count;
            print(counter);
          
                
                
                var dao:PAExperimentDAO = self.allExperiments![1]
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
            
            
        }
  
      tableView.tableFooterView = UIView()
        
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"experimentsRefreshed:", name:"MyExperiments", object: nil)
      
        self.tableView.registerNib(UINib(nibName: "PacoTableViewCell", bundle: nil), forCellReuseIdentifier:cellId)
        
        self.tableView.registerNib(UINib(nibName: "PacoMyExpermementTitleCellTableViewCell", bundle: nil), forCellReuseIdentifier:simpleCellId)
        
        
        let swiftColor = UIColor(red:0.96, green:0.96, blue:0.96, alpha:1.0)
        self.tableView.backgroundColor = swiftColor

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
        var retVal:Int = 0
        if allExperiments  != nil
        {
            retVal =  allExperiments!.count
        }
        return retVal
    }

 
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCellWithIdentifier(self.simpleCellId, forIndexPath: indexPath) as! PacoMyExpermementTitleCellTableViewCell

        
        var dao:PAExperimentDAO = allExperiments![indexPath.row]
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

        cell.parent = self;
        cell.experiment = dao
        cell.experimentTitle.text = title
        cell.subtitle.text = "\(organization!), \(email!)"
        cell.selectionStyle  = UITableViewCellSelectionStyle.None
        
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
        
      var mediator =  PacoMediator.sharedInstance();
      var  mArray:NSMutableArray  = mediator.experiments();
       allExperiments = mArray as AnyObject  as? [PAExperimentDAO]
        
        PacoMediator.sharedInstance().replaceAllExperiments(allExperiments)
        
        print("print the notificatins \(allExperiments)");
        self.tableView.reloadData()
    }
    
    
    
    func didSelect(experiment:PAExperimentDAO)
    {
        var detailController =  PacoExperimentDetailController(nibName:"PacoExperimentDetailController",bundle:nil)
        
        if  experiment.valueForKeyEx("title") != nil
        {
            detailController.title   = (experiment.valueForKeyEx("title")  as? String)!
        }
        
        detailController.experiment = experiment;
          self.tabBarController?.navigationController?.pushViewController(detailController, animated:  true)
       // self.navigationController?.pushViewController(detailController, animated: true)
    }
    
    func didClose(experiment: PAExperimentDAO)
    {
        
        
        
    }
  
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        var mediator =  PacoMediator.sharedInstance()
        var  mArray:NSMutableArray  = mediator.experiments()
        allExperiments = mArray as AnyObject as? [PAExperimentDAO]
        
        
        
        self.tableView.reloadData()
    }
    
}
