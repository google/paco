

import UIKit
 

@objc open class ScheduleEditor: UITableViewController {

    var cells  = [NSArray]()
    var isWizard:Bool = false
    var  experiment:PAExperimentDAO?
    
    
   open  override func viewDidLoad() {
    
        self.tableView.rowHeight = UITableViewAutomaticDimension
        self.tableView.estimatedRowHeight = 44
    
    
    

    
    
    
    
 
    

    
    
    if isWizard == true {
        
    
     //   let  leftAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Done, target: self, action:#selector(doneButton))
       let  rightAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Done", style: UIBarButtonItemStyle.plain, target: self, action:#selector(ScheduleEditor.doneButton(_:)))
        
        
        
       // navigationItem.leftBarButtonItem  = leftAddBarButtonItem;
        navigationItem.rightBarButtonItem = rightAddBarButtonItem
        
       
        //self.tabBarController!.navigationController!.navigationBar.barTintColor = UIColor.greenColor()
        
     
        
     
    }
    else {
        
        
        
      //  let  leftAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Done, target: self, action:"doneButton:")
        let  rightAddBarButtonItem:UIBarButtonItem = UIBarButtonItem(title: "Done", style: UIBarButtonItemStyle.plain, target: self, action:#selector(ScheduleEditor.doneButton(_:)))
     
        
         navigationItem.hidesBackButton = true;
         navigationItem.rightBarButtonItem = rightAddBarButtonItem
        
        
  
    }
    

 
    }
    

    
     func backTaped(_ sender:UIBarButtonItem!)
    {
        print ("back taped super")
    }
    
    
     func nextTaped(_ sender:UIBarButtonItem!)
    {
        
        let   pacoViewController:PacoJoinSummary   = PacoJoinSummary(nibName: "PacoJoinSummary", bundle: nil)
        pacoViewController.experiment = self.experiment
        
        if(isWizard==true)
        {
            self .dismiss(animated: true, completion: {
                
        })
        }
        else
        {
            
            self.navigationController!.popViewController(animated: true)
        }
        
        
    }
    
    

    
    func backThree() {
        
        let viewControllers: [UIViewController] = self.navigationController!.viewControllers
        self.navigationController!.popToViewController(viewControllers[viewControllers.count - 4], animated: true);
        
    }
    
    
    func doneButton(_ sender: UIBarButtonItem) {
        
        
        
         self.tabBarController?.navigationController?.isNavigationBarHidden = true;
        
       let  mediator =  PacoMediator.sharedInstance()
        var experimentId:String
        if  experiment?.instanceId()  != nil
        {
            experimentId =  experiment!.instanceId()
            mediator?.startRunningExperimentRegenerate(experimentId);
        }
        
        
        if(isWizard == true)
        {
            self .dismiss(animated: true, completion: {
                
            })
        }
        else
        {
            self.navigationController?.popViewController(animated: true)
            
        }
    }
    
    
    func cancelButton(_ sender: UIBarButtonItem) {
       
    }
    
    override open func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        
        // Get the correct height if the cell is a DatePickerCell.
        let cell  = self.tableView(tableView, cellForRowAt: indexPath)
        if (cell.isKind(of: DatePickerCell.self)) {
            return (cell as! DatePickerCell).datePickerHeight()
        }
        
        return super.tableView(tableView, heightForRowAt: indexPath)
    }
    
    
    
    override open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        // Deselect automatically if the cell is a DatePickerCell.
        let cell = self.tableView(tableView, cellForRowAt: indexPath)
        
        
        if (cell.isKind(of: DatePickerCell.self)) {
            
            
            let datePickerTableViewCell = cell as! DatePickerCell
            datePickerTableViewCell.selectedInTableView(tableView)
            self.tableView.deselectRow(at: indexPath, animated: true)
            
            
        }
    }
    
    
    
    open override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        
        return (cells[section][0] as AnyObject).groupName as! String;
        
         
    }
    
    open override func numberOfSections(in tableView: UITableView) -> Int {
        return cells.count
    }
    
   open  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (cells[section] as AnyObject).count
    }
    
    
    open override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        return (cells[(indexPath as NSIndexPath).section] as! NSArray )[(indexPath as NSIndexPath).row] as! DatePickerCell
    }
    
    
}
