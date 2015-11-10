//
//  PacoMainSwiftViewController.swift
//  Paco
//
//  Created by northropo on 10/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoMainSwiftViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
       /*
        var helper:PacoSwiftHelper = PacoSwiftHelper();
        var  dao:PAExperimentDAO  = helper.getDao();
        var title:String  =   (dao.valueForKeyEx("title") as? String)!
        message.text = title // helper.testString();
        */
        
        
        
        //[[cell contentView] setBackgroundColor:[UIColor clearColor]];
        //[[cell backgroundView] setBackgroundColor:[UIColor clearColor]];
        //[cell setBackgroundColor:[UIColor clearColor]];
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    @IBOutlet weak var message: UILabel!
    
    
    
    
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
