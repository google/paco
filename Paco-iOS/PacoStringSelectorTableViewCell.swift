//
//  PacoStringSelectorTableViewCell.swift
//  Paco
//
//  Created by northropo on 11/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

class PacoStringSelectorTableViewCell: PacoTableViewExpandingCellBase,UIPickerViewDataSource,UIPickerViewDelegate {
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var expandedView: UIPickerView!
    class var expandedHeight: CGFloat { get { return 200 } }
    class var defaultHeight: CGFloat  { get { return 44  } }
 
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
        
        
        self.expandedView.selectRow(3, inComponent: 0, animated: false)
        self.pickerView(self.expandedView, didSelectRow: 0, inComponent: 0)
    }
    

    
    
    
  
    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return 1
    }
    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return 5
    }
    //MARK: Delegates
    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return  "hello"
    }
    
    func pickerView(pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        
        titleLabel.text = "hello changed"
    }
    
    func pickerView(pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        let titleData = "One"
        let myTitle = NSAttributedString(string: titleData, attributes: [NSFontAttributeName:UIFont(name: "Georgia", size: 26.0)!,NSForegroundColorAttributeName:UIColor.blueColor()])
        return myTitle
    }
    /* less conservative memory version
    func pickerView(pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusingView view: UIView?) -> UIView {
    let pickerLabel = UILabel()
    let titleData = pickerData[row]
    let myTitle = NSAttributedString(string: titleData, attributes: [NSFontAttributeName:UIFont(name: "Georgia", size: 26.0)!,NSForegroundColorAttributeName:UIColor.blackColor()])
    pickerLabel.attributedText = myTitle
    //color  and center the label's background
    let hue = CGFloat(row)/CGFloat(pickerData.count)
    pickerLabel.backgroundColor = UIColor(hue: hue, saturation: 1.0, brightness:1.0, alpha: 1.0)
    pickerLabel.textAlignment = .Center
    return pickerLabel
    }
    */
    
    /* better memory management version */
    func pickerView(pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusingView view: UIView?) -> UIView {
        var pickerLabel = view as! UILabel!
        if view == nil {  //if no label there yet
            pickerLabel = UILabel()
            //color the label's background
            let hue = CGFloat(row)/CGFloat(4)
            //pickerLabel.backgroundColor = UIColor(hue: hue, saturation: 1.0, brightness: 1.0, alpha: 1.0)
        }
        let titleData = "data one"
        let myTitle = NSAttributedString(string: titleData, attributes: [NSFontAttributeName:UIFont(name: "Georgia", size: 26.0)!,NSForegroundColorAttributeName:UIColor.blackColor()])
        pickerLabel!.attributedText = myTitle
        pickerLabel!.textAlignment = .Center
        
        return pickerLabel
        
    }
    
    func pickerView(pickerView: UIPickerView, rowHeightForComponent component: Int) -> CGFloat {
        return 44.0
    }
    // for best use with multitasking , dont use a constant here.
    //this is for demonstration purposes only.
  /*  func pickerView(pickerView: UIPickerView, widthForComponent component: Int) -> CGFloat {
        return 200
    }*/
    
    
    
    
 
    
}
