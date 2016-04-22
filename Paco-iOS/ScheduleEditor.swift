

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
        
        
        let  leftAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Plain, target: self, action:"doneButton:")
        
        
        let  rightAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Plain, target: self, action:"nextTaped:")
        
        //IBarButtonItem(image:UIImage(named:"key.png"), style:.Plain, target:self, action:#selector(navigate))
        
        self.navigationItem.setRightBarButtonItems([rightAddBarButtonItem], animated: true)
        self.navigationItem.setLeftBarButtonItems([leftAddBarButtonItem], animated: true)
        
        
     isWizard = true
        
     
    }
    else {
        
        
        
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
        
        
        self.navigationController!.pushViewController(pacoViewController, animated: false)
        
        
    }
    
    
    
    
    
    
    func backThree() {
        
        let viewControllers: [UIViewController] = self.navigationController!.viewControllers
        self.navigationController!.popToViewController(viewControllers[viewControllers.count - 4], animated: true);
        
    }
    func doneButton(sender: UIBarButtonItem) {
        
        let  mediator =  PacoMediator.sharedInstance()
        var experimentId:String
        if  experiment?.instanceId()  != nil
        {
            experimentId =  experiment!.instanceId()
            mediator.startRunningExperimentRegenerate(experimentId);
        }
        
        
        
          backThree()
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