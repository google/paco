//
//  PacoStringSelectorTableViewCell.swift
//  Paco
//
//  Created by Timo on 11/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoStringSelectorTableViewCell: PacoTableViewExpandingCellBase,UIPickerViewDataSource,UIPickerViewDelegate {
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var expandedView: UIPickerView!
    class var expandedHeight: CGFloat { get { return 200 } }
    class var defaultHeight: CGFloat  { get { return 44  } }
    
    
 
    var listChoices:JavaUtilList?
    var isExpanded:Bool?
    var parent:PacoInputTable?
    var index:PacoInputTable?
    @IBOutlet weak var selectionLabel: UILabel!
    
   
    override func getView() -> UIView {
        return expandedView
    }
    
    override func getHeight() -> CGFloat
    {
        return PacoStringSelectorTableViewCell.expandedHeight;
    }
    
    
    override func getNoneExpandedHeight() -> CGFloat
    {
        return PacoStringSelectorTableViewCell.defaultHeight;
    }
    
    override func awakeFromNib() {
        
        
        
        expandedView.delegate = self;
        expandedView.dataSource = self;
        
        
        
        
        
        
    }
 
    
    
    
  
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        
        
                return 1
    }
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        
        var listSize:Int
        
       if (listChoices != nil)
       {
          let  index:Int32  = Int32(listChoices!.size())
          listSize = Int(index)
          // self.pickerView(self.expandedView, didSelectRow:listSize-1 , inComponent: 0)
        }
        else
       {
         listSize = 0
        
        }
        return listSize
    }
    
    
    
  
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return  "hello"
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        
        var selectedString = listChoices?.getWith(Int32(row)) as? String
        selectionLabel.text = selectedString
        parent?.selected( (indexPath! as NSIndexPath).row , selected: selectedString!)
    }
    
    func pickerView(_ pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        
        
        
        let titleData = "One"
        let myTitle = NSAttributedString(string: titleData, attributes: [NSFontAttributeName:UIFont(name: "Georgia", size: 26.0)!,NSForegroundColorAttributeName:UIColor.blue])
        return myTitle
    }
 
    
 
    func pickerView(_ pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusing view: UIView?) -> UIView {
        var pickerLabel = view as! UILabel!
        if view == nil {  //if no label there yet
            pickerLabel = UILabel()
            //color the label's background
            let hue = CGFloat(row)/CGFloat(4)
            //pickerLabel.backgroundColor = UIColor(hue: hue, saturation: 1.0, brightness: 1.0, alpha: 1.0)
        }
        
        
        
        
        let titleData = listChoices?.getWith(Int32(row)) as! String
        let myTitle = NSAttributedString(string: titleData, attributes: [NSFontAttributeName:UIFont(name: "Georgia", size: 26.0)!,NSForegroundColorAttributeName:UIColor.black])
        pickerLabel!.attributedText = myTitle
        pickerLabel!.textAlignment = .center
        
        return pickerLabel!
        
    }
    
    func pickerView(_ pickerView: UIPickerView, rowHeightForComponent component: Int) -> CGFloat {
        return 44.0
    }
 
    
    
    
 
    
}
