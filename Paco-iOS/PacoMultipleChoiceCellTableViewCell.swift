//
//  PacoMultipleChoiceCellTableViewCell.swift
//  Paco
//
//  Created by northropo on 11/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoMultipleChoiceCellTableViewCell: PacoTableViewExpandingCellBase {
    
    
 
    var listChoices:JavaUtilList?
    
    @IBOutlet weak var checkboxTable: PacoBoxControl!
  
    
    @IBOutlet weak var selectLabel: UILabel!
    
    
    override func getResuts() -> PacoOutput
    {
        
        var output  =  PacoOutput()
        output.input = input
        output.val = listChoices
        if checkboxTable.singleSelect
        {
            output.type = InputType.SingleSelect
        }
        else
        {
            output.type = InputType.MultipleSelect
            
        }
        
        
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
    
    
    func reloadTable(listChoices:JavaUtilList,singleSelect:Bool)
    {
        checkboxTable.singleSelect = singleSelect
        checkboxTable.listChoices = listChoices
        checkboxTable.reload()
        
    }
    

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
