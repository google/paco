//
//  PacoTextTableViewCell.swift
//  Paco
//
//  Created by northropo on 11/6/15.
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
    
    
    
    override func getResuts() -> PacoOutput
    {
      
        var output  =  PacoOutput()
        output.input = input
        output.val = inputTexInput.text
        output.type = InputType.OpenText
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
    
    func textViewDidChange(textView: UITextView) {
        
         parent?.textChanged(indexPath!.row, text: textView.text)
    }
    
 
    override func awakeFromNib() {
        super.awakeFromNib()
        
        inputTexInput.layer.cornerRadius = 5
        inputTexInput.delegate = self
    }

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var questionText: UITextView!
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

           }
    
}
