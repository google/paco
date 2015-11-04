//
//  PacoJoinedExperimentsController.swift
//  Paco
//
//  Created by northropo on 10/29/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit
import MessageUI


class PacoJoinedExperimentsController: UITableViewController,PacoExperimentProtocol,MFMailComposeViewControllerDelegate {
    
    
    
    var  myExpriments:Array<PAExperimentDAO>?
    let cellId = "ExperimenJoinedCellID"
    var selectedIndex = -1;
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
  
 
        
        
     let mediator = PacoMediator.sharedInstance();
     var  mArray:NSMutableArray  = mediator.startedExperiments();
     myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
      NSNotificationCenter.defaultCenter().addObserver(self, selector:"eventJoined:", name:"JoinEvent", object: nil)
     
        
        tableView.tableFooterView = UIView()
    
        self.tableView.registerNib(UINib(nibName:"PacoJoinedExperimentsTableViewCell", bundle: nil), forCellReuseIdentifier:cellId)
            let swiftColor = UIColor(red:0.96, green:0.96, blue:0.96, alpha:1.0)
            self.tableView.backgroundColor = swiftColor
    
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
    
     func eventJoined(notification: NSNotification)
     {
        

        refreshTable()
        
    }
    
    func didClose(experiment: PAExperimentDAO)
    {
        
        var  mediator =   PacoMediator.sharedInstance()
        var  experimentId  =   experiment.instanceId()
        
        myExpriments  = myExpriments!.filter() { $0 != experiment }
       // self.tabBarController?.selectedIndex=0
        
        mediator.stopRunningExperiment(experimentId)
        self.tableView.reloadData()
      ///  refreshTable()
        
    }
    
    
    
 func refreshTable()
{
    var mediator =  PacoMediator.sharedInstance();
    var  mArray:NSMutableArray  = mediator.startedExperiments();
    myExpriments = mArray as AnyObject  as? [PAExperimentDAO]
    println("print the notificatins \(myExpriments)");
    self.tableView.reloadData()
    
    
}
    
    
    
    override func viewWillAppear(animated: Bool) {
        
        refreshTable()
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
     
        var retVal:Int = 0
        if myExpriments  != nil
        {
            retVal =  myExpriments!.count
            
        }
        return retVal
    }
    
 
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        
        var  returnValue:CGFloat = 30
        
        if( indexPath.row == selectedIndex)
        {
            returnValue = 70
        }
        else
        {
            returnValue = 45
        }
        
        return  returnValue;
    }
 
    
    
    
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let cell = tableView.cellForRowAtIndexPath(indexPath) as! PacoJoinedExperimentsTableViewCell
        
        
        if(selectedIndex == indexPath.row)
        {
            selectedIndex = -1;
        }
        else
        {
            selectedIndex = indexPath.row;
          
            
        }
        
        self.tableView.beginUpdates()
        self.tableView.endUpdates()
        
        
        self.tableView.deselectRowAtIndexPath(indexPath, animated: false)
        self.tableView.reloadRowsAtIndexPaths([indexPath],  withRowAnimation:UITableViewRowAnimation.None)
        
        
    }
    
    
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        
        let cell = tableView.dequeueReusableCellWithIdentifier(self.cellId, forIndexPath: indexPath) as! PacoJoinedExperimentsTableViewCell
        
        
        var dao:PAExperimentDAO = myExpriments![indexPath.row]
        var title:String?
        var description:String?
        var creator:String?
        
        if  dao.valueForKeyEx("title") != nil
        {
            title = (dao.valueForKeyEx("title")  as? String)!
        }
        
        if  dao.valueForKeyEx("description")  != nil
        {
            description = (dao.valueForKeyEx("description")  as? String)!
        }
        
        
        if  dao.valueForKeyEx("creator")  != nil
        {
            creator = (dao.valueForKeyEx("creator")  as? String)!
            cell.creator.text  = "by \(creator!)"
        }
        
        
        cell.parent = self;
        cell.experiment = dao
        cell.experimentTitle.text = title
        cell.selectionStyle  = UITableViewCellSelectionStyle.None
       
        return cell;
        
    }
    

    func didSelect(experiment:PAExperimentDAO)
    {
    
    }
    
    
    func email(experiment:PAExperimentDAO){
        
        
        
        
         sendMail(experiment)
    }
    
    
    func sendMail(experiment:PAExperimentDAO) {
        
        
        var picker = MFMailComposeViewController()
        picker.mailComposeDelegate = self
       
        if  experiment.valueForKeyEx("contactEmail")  != nil
        {
           var  email  = (experiment.valueForKeyEx("contactEmail")  as? String)!
            var toRecipents = [email]
            picker.setToRecipients(toRecipents)
            
        }
        
    
     
        picker.setSubject("subject")
        picker.setMessageBody("body", isHTML: true)
        presentViewController(picker, animated: true, completion: nil)
    }
    
    
    func mailComposeController(controller: MFMailComposeViewController!, didFinishWithResult result: MFMailComposeResult, error: NSError!) {
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    func editTime(experiment:PAExperimentDAO){}

    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return NO if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(tableView: UITableView, moveRowAtIndexPath fromIndexPath: NSIndexPath, toIndexPath: NSIndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return NO if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using [segue destinationViewController].
        // Pass the selected object to the new view controller.
    }
    */
    
}
