

import UIKit
 

@objc public class ScheduleEditor: UITableViewController {

    var cells:NSArray = []
    var isWizard:Bool?
    var  experiment:PAExperimentDAO?
    
    
   public  override func viewDidLoad() {
    
        self.tableView.rowHeight = UITableViewAutomaticDimension
        self.tableView.estimatedRowHeight = 44
    
    
    

    
    
    
    
 
    

     let n: Int! = self.navigationController?.viewControllers.count
    
    if  n == 4 {
        
    
        let  leftAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Done, target: self, action:"doneButton:")
        let  rightAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Plain, target: self, action:"nextTaped:")
        
        
        
        navigationItem.leftBarButtonItem  = leftAddBarButtonItem;
        navigationItem.rightBarButtonItem = rightAddBarButtonItem
        
       
        
         //self.tabBarController!.navigationController!.navigationBar.barTintColor = UIColor.greenColor()
        
       isWizard = true
        
     
    }
    else {
        
        
        
      //  let  leftAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Done, target: self, action:"doneButton:")
        let  rightAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Done", style: UIBarButtonItemStyle.Plain, target: self, action:"nextTaped:")
     
        
         navigationItem.hidesBackButton = true;
        navigationItem.rightBarButtonItem = rightAddBarButtonItem
        
        
        
     isWizard = false
    }
    

 
    }
    

    
     func backTaped(sender:UIBarButtonItem!)
    {
        print ("back taped super")
    }
    
    
     func nextTaped(sender:UIBarButtonItem!)
    {
        
        let   pacoViewController:PacoJoinSummary   = PacoJoinSummary(nibName: "PacoJoinSummary", bundle: nil)
        pacoViewController.experiment = self.experiment
        
        if(isWizard==true)
        {
            self.navigationController!.pushViewController(pacoViewController, animated: false)
        }
        else
        {
            
            self.navigationController!.popViewControllerAnimated(true)
        }
        
        
    }
    
    

    
    func backThree() {
        
        let viewControllers: [UIViewController] = self.navigationController!.viewControllers
        self.navigationController!.popToViewController(viewControllers[viewControllers.count - 4], animated: true);
        
    }
    
    
    func doneButton(sender: UIBarButtonItem) {
        
         self.tabBarController?.navigationController?.navigationBarHidden = true;
        
       /* let  mediator =  PacoMediator.sharedInstance()
        var experimentId:String
        if  experiment?.instanceId()  != nil
        {
            experimentId =  experiment!.instanceId()
            mediator.startRunningExperimentRegenerate(experimentId);
        }*/
        
        
        if(isWizard == true)
        {
            backThree()
        }
        else
        {
            self.navigationController?.popViewControllerAnimated(true)
            
        }
    }
    
    
    func cancelButton(sender: UIBarButtonItem) {
       
    }
    
    override public func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        
        
        // Get the correct height if the cell is a DatePickerCell.
        let cell  = self.tableView(tableView, cellForRowAtIndexPath: indexPath)
        if (cell.isKindOfClass(DatePickerCell)) {
            return (cell as! DatePickerCell).datePickerHeight()
        }
        
        return super.tableView(tableView, heightForRowAtIndexPath: indexPath)
    }
    
    
    
    override public func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        // Deselect automatically if the cell is a DatePickerCell.
        let cell = self.tableView(tableView, cellForRowAtIndexPath: indexPath)
        if (cell.isKindOfClass(DatePickerCell)) {
            let datePickerTableViewCell = cell as! DatePickerCell
            datePickerTableViewCell.selectedInTableView(tableView)
            self.tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    public override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        
        return cells[section][0].groupName;
        
         
    }
    
    public override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return cells.count
    }
    
   public  override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cells[section].count
    }
    
    
    public override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        return cells[indexPath.section][indexPath.row] as! DatePickerCell
    }
    
    
}