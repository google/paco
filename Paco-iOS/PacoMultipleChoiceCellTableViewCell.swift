//
//  PacoMultipleChoiceCellTableViewCell.swift
//  Paco
//
//  Created by Timo on 11/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoMultipleChoiceCellTableViewCell: PacoTableViewExpandingCellBase {
    
    
 
    var listChoices:JavaUtilList?
    
    @IBOutlet weak var checkboxTable: PacoBoxControl!
  
    
    @IBOutlet weak var selectLabel: UILabel!
    
    
    
    
    override func isValid() -> Bool
    {
        
        var  retVal:Bool
        
        if(input?.getRequired().booleanValue() == true)
        {
            if( !checkboxTable.checks.keys.isEmpty )
            {
                retVal = true
            }
            else
            {
                retVal = false
            }
 
        }
        else
        {
           retVal = true
            
        }
        
        return retVal
        
    }
    
    @IBOutlet weak var showValidateStar: UILabel!
    
    
    
    
    override func displayValidationIndicator()
    {
        if(isValid() == false)
        {
            showValidateStar.isHidden = false
        }
        else
        {
             showValidateStar.isHidden = true
        }
        
    }
    
    
    
    override func getResuts() -> PacoOutput
    {
        
        var output  =  PacoOutput()
        output.input = input
        output.val = checkboxTable.checks as AnyObject?
        if checkboxTable.singleSelect
        {
            output.type = InputType.singleSelect
        }
        else
        {
            output.type = InputType.multipleSelect
            
        }
        
        output.complete = isValid()
        return output

    }
    
    
    // need this, too, or the compiler will complain that it's missing
//    required init(coder: NSCoder)
//    {
//        super.init(coder: coder)
//    }
    override func awakeFromNib() {
        super.awakeFromNib()
        checkboxTable.input = input;
        checkboxTable.listChoices = listChoices
      
        checkboxTable.reload()
 
    }
    
    
    func reloadTable(_ listChoices:JavaUtilList,singleSelect:Bool)
    {
        checkboxTable.singleSelect = singleSelect
        checkboxTable.listChoices = listChoices
        checkboxTable.reload()
        
    }
    

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
