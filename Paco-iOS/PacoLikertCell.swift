//
//  PacoLikertCell.swift
//  Paco
//
//  Created by northropo on 11/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoLikertCell: PacoTableViewExpandingCellBase,UIGestureRecognizerDelegate {
    
    
    @IBOutlet weak var likertLabel: UILabel!
    let  CHECKED = "\u{2611}"
    let  UNCHECKED = "\u{2B1C}"
    var  labels  = [UILabel]()
    var  checkedIndex:Int = -1
    let  CHECKED2 = "\u{2705}"
    let  UNCHECKED2 = "\u{2B1C}"
    
    var numberOfCheckmarks:Int?
    

    override func awakeFromNib() {
        super.awakeFromNib()
        
        arraingeLabels()
        
        
        
        
    }
    
   func arraingeLabels()
   {
    if numberOfCheckmarks == nil
    {
        return
    }
    
    
    
    let selfRect:CGRect = self.bounds
    
    var  buttonWidth:Float  =  30
    var  buttonHeight:Float =  40
    var width   = UIScreen.mainScreen().bounds.width
    let screenWidth:Float = Float(width)
    
    
    var n:Int = 7
    var startX:Float   = ( screenWidth -  Float(numberOfCheckmarks!)  * buttonWidth)/2
    
    
    var firstLabel:UILabel = UILabel(frame: CGRectMake( CGFloat(startX)   , CGFloat(Float(selfRect.size.height - 50)) ,120,30))
    firstLabel.font = UIFont(name: "HelveticaNeue", size: 10)
    firstLabel.text = input!.getLeftSideLabel()
    self.contentView.addSubview(firstLabel)
    
   // CGFloat(Float(Float(selfRect.size.height) - buttonWidth * Float(numberOfCheckmarks!)
    var secondLabel:UILabel = UILabel(frame:CGRectMake( CGFloat(Float(startX) + buttonWidth * Float(numberOfCheckmarks!)  - 120-12) , CGFloat(Float(selfRect.size.height - 50)) ,120,30))
    secondLabel.font = UIFont(name: "HelveticaNeue", size: 10)
    secondLabel.textAlignment = NSTextAlignment.Right
    secondLabel.text = input?.getRightSideLabel()
    self.contentView.addSubview(secondLabel)
    
    for var i = 0; i < Int(numberOfCheckmarks!); i++
    {

        var frame:CGRect   =
        CGRectMake( CGFloat(startX + buttonWidth * Float(i)) , CGFloat(Float(selfRect.size.height - 30)) ,40,30)
        var label:UILabel = UILabel(frame: frame)
        label.text = "\(UNCHECKED)"
        labels.append(label)
        let   recognizer  =  UITapGestureRecognizer(target: self,action: Selector("checkOrUncheck:"))
        recognizer.delegate = self
        recognizer.numberOfTapsRequired = 1
        label.userInteractionEnabled = true
        label.addGestureRecognizer(recognizer)
        self.contentView.addSubview(label)

        label.tag = i
    }
  }
    
    @IBAction func checkOrUncheck(recognizer:AnyObject)
    {
        let label = recognizer.view as! UILabel
        let index:Int = label.tag
        checkedIndex = index
        
        
        var l:UILabel
        for l in labels
        {
            l.text = "\(UNCHECKED)"
  
        }
        
        labels[index].text = "\(CHECKED)"
        
        
         println("selected label \(label.tag)")
        
        
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

             }
    
    
 
    
}
