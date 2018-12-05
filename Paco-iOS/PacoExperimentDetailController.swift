//
//  PacoExperimentDetailController.swift
//  Paco
//
//  Created by Timo on 10/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoExperimentDetailController: UIViewController {
 
    var  experiment:PAExperimentDAO?
    
 
    @IBOutlet weak var organization: UITextView!
    @IBOutlet weak var creator: UILabel!
    @IBOutlet weak var experimentDescription: UITextView!
    @IBOutlet weak var experimentTitle: UITextView!
    
    
    
    /* remove outlets when there is no start time and end time.*/
    
    @IBOutlet weak var divider3: UIView!
    
    @IBOutlet weak var divider2: UIView!
    @IBOutlet weak var endDate: UILabel!
    @IBOutlet weak var endDateValue: UILabel!
    @IBOutlet weak var endDateActual: UILabel!
    
    @IBOutlet weak var endDateActualValue: UILabel!
 
    
    @IBOutlet weak var OrganizationConstraint: NSLayoutConstraint!
    
    override func viewDidLoad()
    {
        super.viewDidLoad()
        
        self.tabBarController?.navigationController?.isNavigationBarHidden = true;
        
        
        let backBtn = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.plain, target: self, action: #selector(PacoExperimentDetailController.btnBack(_:)))
        
     
        
        navigationItem.leftBarButtonItem  = backBtn
   
        
   
    
        
       if   experiment?.earliestStartDate() != nil &&   experiment?.lastEndDate() != nil
       {
        
            endDateValue.text = (experiment?.earliestStartDate() )
            endDateActualValue.text = ( experiment?.lastEndDate()  )
        }
        else
       {
        
           removeStartAndEndDate()
        
        }
      
        
        
        
        if  experiment!.value(forKeyEx: "title") != nil
        {
             experimentTitle.text   = (experiment!.value(forKeyEx: "title")  as? String)!
        }
        
        if  experiment!.value(forKeyEx: "description")  != nil
        {
              experimentDescription.text  = (experiment!.value(forKeyEx: "description")  as? String)!
        }
        
        if  experiment!.value(forKeyEx: "creator")  != nil
        {
             creator.text   = (experiment!.value(forKeyEx: "creator")  as? String)!
        }
        
        if  experiment!.value(forKeyEx: "organization")  != nil
        {
            organization.text  = (experiment!.value(forKeyEx: "organization")  as? String)!
        }
 
        // Do any additional setup after loading the view.
    }

    @IBOutlet weak var joinExperiment: UIButton!
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        
   
        self.experiment = nil
        
        
    }
    
    
    
    @IBAction func btnBack(_ btn:AnyObject)
    {
        
        navigationController?.popToRootViewController(animated: true)
        
    }
    
  
    @IBAction func joinExperiment(_ sender: AnyObject)
    {
        let  consentForm = PacoConsentFormViewController(nibName:"PacoConsentFormViewController", bundle:nil)
        consentForm.experiment = self.experiment
       
      
        
        if  experiment!.value(forKeyEx: "title") != nil
        {
            consentForm.title   = (experiment!.value(forKeyEx: "title")  as? String)!
        }
        self.navigationController?.pushViewController(consentForm, animated: true)
        
        
    }
    
   
    
    
    
    func removeStartAndEndDate()
    {
        endDate.removeFromSuperview()
        endDateValue.removeFromSuperview()
        endDateActual.removeFromSuperview()
        endDateActualValue.removeFromSuperview()
        OrganizationConstraint.constant = 3
        
        
    }

 

}
