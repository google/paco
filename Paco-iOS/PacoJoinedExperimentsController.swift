//
//  PacoJoinedExperimentsController.swift
//  Paco
//
//  Created by Timo on 10/29/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit
import MessageUI


class PacoJoinedExperimentsController: UITableViewController,PacoExperimentProtocol,MFMailComposeViewControllerDelegate {
    
    
    
    var  myExpriments:Array<PAExperimentDAO>?
    let cellId = "ExperimenJoinedCellID"
    var selectedIndex = -1;
    var controller:PacoResponseTableViewController?
    var picker:MFMailComposeViewController?
    
    
    
    
    
    
    override func viewDidLayoutSubviews() {
        let   lengthis:CGFloat    = self.bottomLayoutGuide.length
        
        let lengththat:CGFloat = self.topLayoutGuide.length
        self.tableView.contentInset = UIEdgeInsetsMake(lengththat, 0, lengthis, 0);
        
        
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

     let mediator = PacoMediator.sharedInstance();
     let  mArray:NSMutableArray  = mediator!.startedExperiments();
     myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
        
        
      NotificationCenter.default.addObserver(self, selector:#selector(PacoJoinedExperimentsController.eventJoined(_:)), name:NSNotification.Name(rawValue: "JoinEvent"), object: nil)
      tableView.tableFooterView = UIView()
    
        self.tableView.register(UINib(nibName:"PacoJoinedExperimentsTableViewCell", bundle: nil), forCellReuseIdentifier:cellId)
            let swiftColor = UIColor(red:0.96, green:0.96, blue:0.96, alpha:1.0)
            self.tableView.backgroundColor = swiftColor
    
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }



    override func numberOfSections(in tableView: UITableView) -> Int {

        return 1
    }
    
     func eventJoined(_ notification: Notification)
     {
        

        refreshTable()
        
    }
    
    func didClose(_ experiment: PAExperimentDAO)
    {
        
         let  m:PacoMediator =   PacoMediator.sharedInstance()
         let  experimentId  =   experiment.instanceId()
         myExpriments  = myExpriments!.filter() { $0 != experiment }
         m.stopRunningExperimentRegenerate(experimentId)
         let event:PacoEventExtended  = PacoEventExtended.stopEventForActionSpecificaton(withServerExperimentId: experiment, serverExperimentId: "not applicable")
        
         m.eventManager.saveEvent(event);
         m.eventManager.startUploadingEvents()
        
        
  
        
        self.tableView.reloadData()
   
        
    }
    
    
    
 func refreshTable()
{
    let  mediator =  PacoMediator.sharedInstance();
    let  mArray:NSMutableArray  = mediator!.startedExperiments();
    
    myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
    
    print("print the notificatins \(myExpriments)");
    self.tableView.reloadData()
    
    
}
    
    
    
    override func viewWillAppear(_ animated: Bool) {
        
        self.tabBarController?.navigationItem.title = "Joined"
        
        refreshTable()
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
     
        var retVal:Int = 0
        if myExpriments  != nil
        {
            retVal =  myExpriments!.count
            
        }
        return retVal
    }
    
 
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        var  returnValue:CGFloat = 30
        
        if( (indexPath as NSIndexPath).row == selectedIndex)
        {
            returnValue = 70
        }
        else
        {
            returnValue = 45
        }
        
        return  returnValue;
    }
 
  
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
            let dao:PAExperimentDAO!  =  myExpriments![(indexPath as NSIndexPath).row]
        
            let experiment:PacoExperiment =    PacoExperiment.init(experimentDao:dao!)
        
            let numberOfGroups:Int32  =   dao.numberOfGroups()
        
           if(numberOfGroups == 1)
           {
               let group  = dao.soloGroup();
               let ctrler   = PacoQuestionScreenViewController.controller(with: experiment,group:group)
               self.tabBarController!.navigationController?.pushViewController(  ctrler as! UIViewController  , animated: true)
            }
           else
           {
            
            
               let allGroups = dao.fetchExperimentGroupDictionary()
               let ctrler = PacoGroupSelectionController.init(nibNameAndGroups:allGroups , experiment: dao , nibName:"PacoGroupSelectionController")
               self.tabBarController!.navigationController?.pushViewController(  ctrler as! UIViewController  , animated: true)
            
           }
        
        
           // let ctrler   = PacoQuestionScreenViewController.controllerWithExperiment(experiment)
           // self.tabBarController!.navigationController?.pushViewController(  ctrler as! UIViewController  , animated: true)
        
    }
 
    
    func showEditView(_ experiment:PAExperimentDAO,indexPath:IndexPath)
    {
            
           print("the index is \((indexPath as NSIndexPath).row)")
        
        let cell = tableView.dequeueReusableCell(withIdentifier: self.cellId, for: indexPath) as! PacoJoinedExperimentsTableViewCell
        
        
        if(selectedIndex == (indexPath as NSIndexPath).row)
        {
            selectedIndex = -1;
            
           cell.edit.setTitle("edit", for: UIControlState())
         
        }
        else
        {
            selectedIndex = (indexPath as NSIndexPath).row;
            cell.edit.setTitle("close", for: UIControlState())
            
        }
        
        self.tableView.beginUpdates()
        self.tableView.endUpdates()
        
        
        self.tableView.deselectRow(at: indexPath, animated: false)
        self.tableView.reloadRows(at: [indexPath],  with:UITableViewRowAnimation.none)
            
            
            
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCell(withIdentifier: self.cellId, for: indexPath) as! PacoJoinedExperimentsTableViewCell
        
        
        let dao:PAExperimentDAO = myExpriments![(indexPath as NSIndexPath).row]
        var title:String?
        var description:String?
        var creator:String?
        
        if  dao.value(forKeyEx: "title") != nil
        {
            title = (dao.value(forKeyEx: "title")  as? String)!
        }
        
        if  dao.value(forKeyEx: "description")  != nil
        {
            description = (dao.value(forKeyEx: "description")  as? String)!
        }
        
        
        if  dao.value(forKeyEx: "creator")  != nil
        {
            creator = (dao.value(forKeyEx: "creator")  as? String)!
            cell.creator.text  = "by \(creator!)"
        }
        
        cell.indexPath = indexPath
        cell.parent = self;
        cell.experiment = dao
        cell.experimentTitle.text = title
        cell.selectionStyle  = UITableViewCellSelectionStyle.none
       
        return cell;
        
    }
    

    func didSelect(_ experiment:PAExperimentDAO)
    {
    
    }
    
    
    func email(_ experiment:PAExperimentDAO){

         sendMail(experiment)
    }
    
    
    func sendMail(_ experiment:PAExperimentDAO) {
        
        
        self.picker  = MFMailComposeViewController()
        self.picker!.mailComposeDelegate = self
       
        if  experiment.value(forKeyEx: "contactEmail")  != nil
        {
           let  email  = (experiment.value(forKeyEx: "contactEmail")  as? String)!
            let toRecipents = [email]
            self.picker!.setToRecipients(toRecipents)
            
        }
        
    
     
        picker!.setSubject("subject")
        picker!.setMessageBody("body", isHTML: true)
        present(picker!, animated: true, completion: nil)
        
        
    }
    
    
    func mailComposeController(_ controller: MFMailComposeViewController!, didFinishWith result: MFMailComposeResult, error: Error!) {
        
        dismiss(animated: true, completion: nil)
    }
    
    
  func editTime(_ experiment:PAExperimentDAO){
        
        
        
        
        let  arrayOfCells  = experiment.getTableCellModelObjects()
        
        if   arrayOfCells != nil && arrayOfCells?.isEmpty == false   {
            
            let  editor =  ScheduleEditor(nibName:"ScheduleEditor",bundle:nil)
            
            editor.cells = arrayOfCells! as! [NSArray]
            editor.experiment = experiment
            
            
            
                self.tabBarController?.navigationController?.pushViewController(editor, animated: true)
                
            
            
             }
    
    
    
    }

 
    
}
