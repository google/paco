//
//  PacoConsentFormViewController.swift
//  Paco
//
//  Created by Timo on 10/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoConsentFormViewController: UIViewController {
    
    var agreement = "By joining this experiment, you may be sharing data with the creator and administrators of this experiment. Read the data handling policy they have provided below to decide on whether you want to participate in this experiment."
    
    
    
    
    @IBOutlet weak var agreementView: UITextView!
    
    
    var myMutableString = NSMutableAttributedString()
    var  experiment:PAExperimentDAO?

    override func viewDidLoad() {
        super.viewDidLoad()
        
       
        self.navigationItem.setHidesBackButton(true, animated:true);
        
         
        let font = UIFont(name: "Roboto-light", size: 11.0) ?? UIFont.systemFontOfSize(12.0)
        let textFont = [NSFontAttributeName:font]
        
        let fontBold = UIFont(name: "Roboto-regular", size: 15.0) ?? UIFont.systemFontOfSize(12.0)
        let fontTextBold = [NSFontAttributeName:fontBold]
        
        let para = NSMutableAttributedString()
        let attrString1 = NSAttributedString(string: "Data Handling & Privacy Agreement between You and the Experiment Creator.\n ", attributes:fontTextBold)
         let attrString2 = NSAttributedString(string: agreement, attributes:textFont)
        
         para.appendAttributedString(attrString1)
         para.appendAttributedString(attrString2)
        
        // Define paragraph styling
        let paraStyle = NSMutableParagraphStyle()
        paraStyle.firstLineHeadIndent = 15.0
        paraStyle.paragraphSpacingBefore = 10.0
        
        self.agreementView.attributedText = para;
        
        

        // Do any additional setup after loading the view.
    }
    
    


    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func decline(sender: AnyObject)
    {
        print("declined")
        
        backTwo()
        
    }
    
    
    @IBAction func accept(sender: AnyObject)
    {
        print("accepted")
        
        
       var mediator =  PacoMediator.sharedInstance()
        var experimentId:String
        if  experiment?.instanceId()  != nil
        {
             experimentId =  experiment!.instanceId()
             mediator.startRunningExperimentRegenerate(experimentId);
        }
        
        
        
       
        
        
        backTwo()
        
    }
    
    
    func backTwo() {
        
        let viewControllers: [UIViewController] = self.navigationController!.viewControllers as! [UIViewController];
        self.navigationController!.popToViewController(viewControllers[viewControllers.count - 3], animated: true);
        
    }
    
    
 

    @IBOutlet weak var agreementText: UITextView!
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
