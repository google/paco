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
    
    
    override func viewDidLayoutSubviews() {
        let   lengthis:CGFloat    = self.bottomLayoutGuide.length
        
        let lengththat:CGFloat = self.topLayoutGuide.length
        
        self.tableView.contentInset = UIEdgeInsetsMake(lengththat, 0, lengthis, 0);
        
        
        
    }
    
    
    

    
    func showEditView(_ experiment:PAExperimentDAO,indexPath:IndexPath)
        {
            
            
        }
  
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
       
            publicIterator?.loadNextPage { ( nsArray  , err  ) -> Void in
            let  counter:Int   =  nsArray!.count;
            print(counter);
          
                
                
                let dao:PAExperimentDAO = self.allExperiments![1]
                let  title:String?
                let  organization:String?
                let  email:String?
                let  description:String?
                
                if  dao.value(forKeyEx: "title") != nil
                {
                    title = (dao.value(forKeyEx: "title")  as? String)!
                }
                if  dao.value(forKeyEx: "description")  != nil
                {
                    description = (dao.value(forKeyEx: "description")  as? String)!
                }
                if  dao.value(forKeyEx: "organization") != nil
                {
                    organization = (dao.value(forKeyEx: "organization")  as? String)!
                }
                else
                {
                    organization = " "
                }
                if  dao.value(forKeyEx: "contactEmail") != nil
                {
                    email = (dao.value(forKeyEx: "contactEmail")  as? String)!
                }
                else
                {
                    email = " "
                    
                }
            
            
        }
  
      tableView.tableFooterView = UIView()
        
        
        NotificationCenter.default.addObserver(self, selector:#selector(PacoHubExperiments.experimentsRefreshed(_:)), name:NSNotification.Name(rawValue: "MyExperiments"), object: nil)
      
        self.tableView.register(UINib(nibName: "PacoTableViewCell", bundle: nil), forCellReuseIdentifier:cellId)
        
        self.tableView.register(UINib(nibName: "PacoMyExpermementTitleCellTableViewCell", bundle: nil), forCellReuseIdentifier:simpleCellId)
        
        
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

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete method implementation.
        // Return the number of rows in the section.
        var retVal:Int = 0
        if allExperiments  != nil
        {
            retVal =  allExperiments!.count
        }
        return retVal
    }

 
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCell(withIdentifier: self.simpleCellId, for: indexPath) as! PacoMyExpermementTitleCellTableViewCell

        
        let dao:PAExperimentDAO = allExperiments![(indexPath as NSIndexPath).row]
        var title:String?
        var organization:String?
         var email:String?
        var description:String?
 
       if  dao.value(forKeyEx: "title") != nil
       {
           title = (dao.value(forKeyEx: "title")  as? String)!
        }
        if  dao.value(forKeyEx: "description")  != nil
        {
          description = (dao.value(forKeyEx: "description")  as? String)!
        }
        if  dao.value(forKeyEx: "organization") != nil
        {
            organization = (dao.value(forKeyEx: "organization")  as? String)!
        }
        else
        {
            organization = " "
        }
        if  dao.value(forKeyEx: "contactEmail") != nil
        {
            email = (dao.value(forKeyEx: "contactEmail")  as? String)!
        }
        else
        {
            email = " "
            
        }

        cell.parent = self;
        cell.experiment = dao
        cell.experimentTitle.text = title
        cell.subtitle.text = "\(organization!), \(email!)"
        cell.selectionStyle  = UITableViewCellSelectionStyle.none
        
        return cell;
        
    }
    
 
  
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cell = tableView.cellForRow(at: indexPath) as! PacoMyExpermementTitleCellTableViewCell
        
                didSelect(cell.experiment!)
                print("did select \(cell.experimentTitle.text) \n")
        }
    
    
 
    
    
    func email(_ experiment:PAExperimentDAO){}
    func editTime(_ experiment:PAExperimentDAO){}
    
    
    func experimentsRefreshed(_ notification: Notification){
        
      let mediator =  PacoMediator.sharedInstance();
      let   mArray:NSMutableArray  = mediator!.experiments();
       allExperiments = mArray as AnyObject  as? [PAExperimentDAO]
        
        PacoMediator.sharedInstance().replaceAllExperiments(allExperiments)
        
        print("print the notificatins \(allExperiments)");
        self.tableView.reloadData()
    }
    
    
    
    func didSelect(_ experiment:PAExperimentDAO)
    {
        let  detailController =  PacoExperimentDetailController(nibName:"PacoExperimentDetailController",bundle:nil)
        
        if  experiment.value(forKeyEx: "title") != nil
        {
            detailController.title   = (experiment.value(forKeyEx: "title")  as? String)!
        }
        
        detailController.experiment = experiment;
          self.tabBarController?.navigationController?.pushViewController(detailController, animated:  true)
       // self.navigationController?.pushViewController(detailController, animated: true)
    }
    
    func didClose(_ experiment: PAExperimentDAO)
    {
        
        
        
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        
       // self.tabBarController?.navigationController?.setNavigationBarHidden(false, animated: false)
    }
  
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        let mediator =  PacoMediator.sharedInstance()
        let   mArray:NSMutableArray  = mediator!.experiments()
        allExperiments = mArray as AnyObject as? [PAExperimentDAO]
        
      //  self.tabBarController?.navigationController!.setNavigationBarHidden(true, animated: false)
        
        self.tableView.reloadData()
    }
    
}
