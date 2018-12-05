//
//  PacoTextTableViewCell.swift
//  Paco
//
//  Created by Timo on 11/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit
import QuartzCore

class PacoTextTableViewCell:PacoTableViewExpandingCellBase, UITextViewDelegate  {
    

    var parent:PacoInputTable?
    class var expandedHeight: CGFloat { get { return 230 } }
    class var defaultHeight: CGFloat  { get { return 44  } }
    
    @IBOutlet weak var inputLabel: UILabel!
    @IBOutlet weak var inputTexInput: UITextView!
    
    
    
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
 
    
    override func isValid() -> Bool
    {
        var retVal:Bool
        
        if(input?.getRequired().booleanValue() == true)
        {
            if( inputTexInput.text.characters.count > 1 )
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
    
    override func getResuts() -> PacoOutput
    {
      
        var output  =  PacoOutput()
        output.complete = isValid()
        output.input = input
        output.val = inputTexInput.text as AnyObject?
        output.type = InputType.openText
        return output
    
    }
    
    
    
    
    override func getHeight() -> CGFloat
    {
        return PacoPickerTableViewCell.expandedHeight
    }
    
    override func getNoneExpandedHeight() -> CGFloat
    {
        return PacoPickerTableViewCell.defaultHeight
    }
    
    func textViewDidChange(_ textView: UITextView) {
        
         parent?.textChanged((indexPath! as NSIndexPath).row, text: textView.text)
    }
    
 
    override func awakeFromNib() {
        super.awakeFromNib()
        
        inputTexInput.layer.cornerRadius = 5
        inputTexInput.delegate = self
    }

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var questionText: UITextView!
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

           }
    
}
