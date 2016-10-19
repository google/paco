//
//  PacoLikertCell.swift
//  Paco
//
//  Created by Timo on 11/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoLikertCell: PacoTableViewExpandingCellBase {
    
    
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
    
    let  buttonWidth:Float  =  30
    var  buttonHeight:Float =  40
    let width   = UIScreen.main.bounds.width
    let screenWidth:Float = Float(width)
    
    
    var n:Int = 7
    let startX:Float   = ( screenWidth -  Float(numberOfCheckmarks!)  * buttonWidth)/2
    
    
    let firstLabel:UILabel = UILabel(frame: CGRect( x: CGFloat(startX)   , y: CGFloat(Float(selfRect.size.height - 50)) ,width: 120,height: 30))
    firstLabel.font = UIFont(name: "HelveticaNeue", size: 10)
    firstLabel.text = input!.getLeftSideLabel()
    self.contentView.addSubview(firstLabel)
    
   // CGFloat(Float(Float(selfRect.size.height) - buttonWidth * Float(numberOfCheckmarks!)
    let secondLabel:UILabel = UILabel(frame:CGRect( x: CGFloat(Float(startX) + buttonWidth * Float(numberOfCheckmarks!)  - 120-12) , y: CGFloat(Float(selfRect.size.height - 50)) ,width: 120,height: 30))
    secondLabel.font = UIFont(name: "HelveticaNeue", size: 10)
    secondLabel.textAlignment = NSTextAlignment.right
    secondLabel.text = input?.getRightSideLabel()
    self.contentView.addSubview(secondLabel)
    
    for i in 0 ..< Int(numberOfCheckmarks!)
    {

        let frame:CGRect   =
        CGRect( x: CGFloat(startX + buttonWidth * Float(i)) , y: CGFloat(Float(selfRect.size.height - 30)) ,width: 40,height: 30)
        let label:UILabel = UILabel(frame: frame)
        label.text = "\(UNCHECKED)"
        labels.append(label)
        let   recognizer  =  UITapGestureRecognizer(target: self,action: #selector(PacoLikertCell.checkOrUncheck(_:)))
        recognizer.delegate = self
        recognizer.numberOfTapsRequired = 1
        label.isUserInteractionEnabled = true
        label.addGestureRecognizer(recognizer)
        self.contentView.addSubview(label)

        label.tag = i
    }
  }
    
    @IBAction func checkOrUncheck(_ recognizer:AnyObject)
    {
       /* let label = recognizer.view as! UILabel
        let index:Int = label.tag
        checkedIndex = index
        
        
        var l:UILabel
        for l in labels
        {
            l.text = "\(UNCHECKED)"
  
        }
        
        labels[index].text = "\(CHECKED)"
        
        
         print("selected label \(label.tag)")
        */
        
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

             }
    
    
 
    
}
