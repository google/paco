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
    
    
    func showEditView(_ experiment:PAExperimentDAO,indexPath:IndexPath)
        {
            
            
        }
  
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
  
  
        
      
        
        
        NotificationCenter.default.addObserver(self, selector:#selector(PacoMyExperiments.experimentsRefreshed(_:)), name:NSNotification.Name(rawValue: "MyExperiments"), object: nil)
      
        self.tableView.register(UINib(nibName: "PacoTableViewCell", bundle: nil), forCellReuseIdentifier:cellId)
        
        self.tableView.register(UINib(nibName: "PacoMyExpermementTitleCellTableViewCell", bundle: nil), forCellReuseIdentifier:simpleCellId)
        
        
        let swiftColor = UIColor(red:0.96, green:0.96, blue:0.96, alpha:1.0)
        self.tableView.backgroundColor = swiftColor
 
        
        let  networkHelper = PacoNetwork .sharedInstance()
        networkHelper?.update()
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

 

    override func numberOfSections(in tableView: UITableView) -> Int {
 
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
 
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

 
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: self.simpleCellId, for: indexPath) as! PacoMyExpermementTitleCellTableViewCell
        
        if( !isRefreshed ) {
            
            
             cell.textLabel!.text = "Loading...";
            
            
        }
        else
        {
       
         cell.textLabel!.text = nil 
        
     

                let dao:PAExperimentDAO = myExpriments![(indexPath as NSIndexPath).row]
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
                if !dao.isCompatibleWithIOS()
                {
                    
                    
                    
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
                
                
                
                let isCompatible =  dao.isCompatibleWithIOS()
                var compatibilityText:NSString;
                
                
                if isCompatible == true
                {
                    cell.isIos.isHidden = true;
                }
            
                
            
            

                cell.parent = self;
                cell.experiment = dao
                cell.experimentTitle.text = title
                cell.subtitle.text = "\(organization!), \(email!)"
                cell.selectionStyle  = UITableViewCellSelectionStyle.none
                //cell.isIOSCompatible.text = compatibilityText as String
        
        }
        
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
        
        
      isRefreshed = true;
      let   mediator =  PacoMediator.sharedInstance();
      let   mArray:NSMutableArray  = mediator!.experiments();
       myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
        
        
        
        PacoMediator.sharedInstance().replaceAllExperiments(myExpriments)
        
        print("print the notificatins \(myExpriments)");
       
        DispatchQueue.main.async(execute: { self.tableView.reloadData()
                                                    self.tableView.setNeedsDisplay()})
        
        
      
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
        //self.tabnavigationController?.pushViewController(detailController, animated: true)
    }
    
    func didClose(_ experiment: PAExperimentDAO)
    {
        
        
        
    }
  
    override func viewDidAppear(_ animated: Bool) {
        
          self.tabBarController?.navigationItem.title = "Invitations"
 
        
        let  mediator =  PacoMediator.sharedInstance()
        let mArray:NSMutableArray  = mediator!.experiments()
        myExpriments = mArray as AnyObject as? [PAExperimentDAO]
        
        
        
        
        self.tableView.reloadData()
    }
    
    
    override func viewWillDisappear(_ animated: Bool) {
  
    }
    
}
