
import Foundation
import UIKit
 
 
 
 
 
/**
*  Inline/Expanding date picker for table views.
*/
@objc open class DatePickerCell: UITableViewCell {
    

    
    var  isESM:Bool?
    var  groupName:String?
    var  timeLabelStr:String?
    var  timeOfDayString:String?
    var  theType:String?
    var  millisecondsSinceMidnight:Int?
    var  signalTime:PASignalTime?
    var  schedule:PASchedule?
    
    
    
    
    
    
    /**
    *  UIView subclass. Used as a subview in UITableViewCells. Does not change color when the UITableViewCell is selected.
    */
    class DVColorLockView:UIView {
        
        var lockedBackgroundColor:UIColor {
            set {
                super.backgroundColor = newValue
            }
            get {
                return super.backgroundColor!
            }
        }
        
        override var backgroundColor:UIColor? {
            set {
            }
            get {
                return super.backgroundColor
            }
        }
    }
    
    // Only create one NSDateFormatter to save resources.
    static let dateFormatter = DateFormatter()
    
    /// The selected date, set to current date/time on initialization.
    open var date:Date = Date() {
        didSet {
            datePicker.date = date
            //DatePickerCell.dateFormatter.dateStyle = dateStyle
            DatePickerCell.dateFormatter.timeStyle = timeStyle
            rightLabel.text = DatePickerCell.dateFormatter.string(from: date)
        }
    }
    /// The timestyle.
    open var timeStyle = DateFormatter.Style.short {
        didSet {
            DatePickerCell.dateFormatter.timeStyle = timeStyle
            rightLabel.text = DatePickerCell.dateFormatter.string(from: date)
        }
    }
    /// The datestyle.
    open var dateStyle = DateFormatter.Style.medium {
        didSet {
            DatePickerCell.dateFormatter.dateStyle = dateStyle
            rightLabel.text = DatePickerCell.dateFormatter.string(from: date)
        }
    }

    /// Label on the left side of the cell.
    open var leftLabel = UILabel()
    /// Label on the right side of the cell.
    open var rightLabel = UILabel()
    /// Color of the right label. Default is the color of a normal detail label.
    open var rightLabelTextColor = UIColor(hue: 0.639, saturation: 0.041, brightness: 0.576, alpha: 1.0) {
        didSet {
            rightLabel.textColor = rightLabelTextColor
        }
    }
    
    var seperator = DVColorLockView()
    
    var datePickerContainer = UIView()
    /// The datepicker embeded in the cell.
    open var datePicker: UIDatePicker = UIDatePicker()
    
    

    
    /// Is the cell expanded?
    open var expanded = false
    var unexpandedHeight = CGFloat(44)
    
    /**
    Creates the DatePickerCell
    
    - parameter style:           A constant indicating a cell style. See UITableViewCellStyle for descriptions of these constants.
    - parameter reuseIdentifier: A string used to identify the cell object if it is to be reused for drawing multiple rows of a table view. Pass nil if the cell object is not to be reused. You should use the same reuse identifier for all cells of the same form.
    
    - returns: An initialized DatePickerCell object or nil if the object could not be created.
    */
    override public init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        
        super.init(style: style, reuseIdentifier: reuseIdentifier)
                
        //setup()
    }
    

    
    open  func setup() {
        
        
        datePicker.datePickerMode = UIDatePickerMode.time
        // The datePicker overhangs the view slightly to avoid invalid constraints.
        self.clipsToBounds = true
        
        let views = [leftLabel, rightLabel, seperator, datePickerContainer, datePicker]
        for view in views {
            self.contentView .addSubview(view)
            view.translatesAutoresizingMaskIntoConstraints = false
        }
        
        datePickerContainer.clipsToBounds = true
        datePickerContainer.addSubview(datePicker)
        
        // Add a seperator between the date text display, and the datePicker. Lighter grey than a normal seperator.
        seperator.lockedBackgroundColor = UIColor(white: 0, alpha: 0.1)
        datePickerContainer.addSubview(seperator)
        datePickerContainer.addConstraints([
            NSLayoutConstraint(
                item: seperator,
                attribute: NSLayoutAttribute.left,
                relatedBy: NSLayoutRelation.equal,
                toItem: datePickerContainer,
                attribute: NSLayoutAttribute.left,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: seperator,
                attribute: NSLayoutAttribute.right,
                relatedBy: NSLayoutRelation.equal,
                toItem: datePickerContainer,
                attribute: NSLayoutAttribute.right,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: seperator,
                attribute: NSLayoutAttribute.height,
                relatedBy: NSLayoutRelation.equal,
                toItem: nil,
                attribute: NSLayoutAttribute.notAnAttribute,
                multiplier: 1.0,
                constant: 0.5
            ),
            NSLayoutConstraint(
                item: seperator,
                attribute: NSLayoutAttribute.top,
                relatedBy: NSLayoutRelation.equal,
                toItem: datePickerContainer,
                attribute: NSLayoutAttribute.top,
                multiplier: 1.0,
                constant: 0
            ),
            ])
        
        
        rightLabel.textColor = rightLabelTextColor
        
        // Left label.
        self.contentView.addConstraints([
            NSLayoutConstraint(
                item: leftLabel,
                attribute: NSLayoutAttribute.height,
                relatedBy: NSLayoutRelation.equal,
                toItem: nil,
                attribute: NSLayoutAttribute.notAnAttribute,
                multiplier: 1.0,
                constant: 44
            ),
            NSLayoutConstraint(
                item: leftLabel,
                attribute: NSLayoutAttribute.top,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.top,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: leftLabel,
                attribute: NSLayoutAttribute.left,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.left,
                multiplier: 1.0,
                constant: self.separatorInset.left
            ),
            ])
        
        // Right label
        self.contentView.addConstraints([
            NSLayoutConstraint(
                item: rightLabel,
                attribute: NSLayoutAttribute.height,
                relatedBy: NSLayoutRelation.equal,
                toItem: nil,
                attribute: NSLayoutAttribute.notAnAttribute,
                multiplier: 1.0,
                constant: 44
            ),
            NSLayoutConstraint(
                item: rightLabel,
                attribute: NSLayoutAttribute.top,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.top,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: rightLabel,
                attribute: NSLayoutAttribute.right,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.right,
                multiplier: 1.0,
                constant: -self.separatorInset.left
            ),
            ])
        
        // Container.
        self.contentView.addConstraints([
            NSLayoutConstraint(
                item: datePickerContainer,
                attribute: NSLayoutAttribute.left,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.left,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: datePickerContainer,
                attribute: NSLayoutAttribute.right,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.right,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: datePickerContainer,
                attribute: NSLayoutAttribute.top,
                relatedBy: NSLayoutRelation.equal,
                toItem: leftLabel,
                attribute: NSLayoutAttribute.bottom,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: datePickerContainer,
                attribute: NSLayoutAttribute.bottom,
                relatedBy: NSLayoutRelation.equal,
                toItem: self.contentView,
                attribute: NSLayoutAttribute.bottom,
                multiplier: 1.0,
                constant: 1
            ),
            ])
        
        // Picker constraints.
        datePickerContainer.addConstraints([
            NSLayoutConstraint(
                item: datePicker,
                attribute: NSLayoutAttribute.left,
                relatedBy: NSLayoutRelation.equal,
                toItem: datePickerContainer,
                attribute: NSLayoutAttribute.left,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: datePicker,
                attribute: NSLayoutAttribute.right,
                relatedBy: NSLayoutRelation.equal,
                toItem: datePickerContainer,
                attribute: NSLayoutAttribute.right,
                multiplier: 1.0,
                constant: 0
            ),
            NSLayoutConstraint(
                item: datePicker,
                attribute: NSLayoutAttribute.top,
                relatedBy: NSLayoutRelation.equal,
                toItem: datePickerContainer,
                attribute: NSLayoutAttribute.top,
                multiplier: 1.0,
                constant: 0
            ),
            ])
        
        datePicker.addTarget(self, action: #selector(DatePickerCell.datePicked), for: UIControlEvents.valueChanged)
         // Clear seconds.
      //  let timeIntervalSinceReferenceDateWithoutSeconds = floor(date.timeIntervalSinceReferenceDate / 60.0) * 60.0
          //  self.date   NSDate(timeIntervalSinceReferenceDate: timeIntervalSinceReferenceDateWithoutSeconds)
            leftLabel.text =  timeLabelStr
           rightLabel.text = timeOfDayString
    }
    
    /**
    Needed for initialization from a storyboard.

    - parameter aDecoder: An unarchiver object.
    - returns: An initialized DatePickerCell object or nil if the object could not be created.
    */
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
       // setup()
    }
    
    /**
    Determines the current desired height of the cell. Used in the UITableViewDelegate's heightForRowAtIndexPath method.
    
    - returns: The cell's height.
    */
    open func datePickerHeight() -> CGFloat {
        let expandedHeight = unexpandedHeight + CGFloat(datePicker.frame.size.height)
        return expanded ? expandedHeight : unexpandedHeight
    }
    
    /**
    Used to notify the DatePickerCell that it was selected. The DatePickerCell will then run its selection animation and expand or collapse.
    
    - parameter tableView: The tableview the DatePickerCell was selected in.
    */
    open func selectedInTableView(_ tableView: UITableView) {
        expanded = !expanded
        
        UIView.transition(with: rightLabel, duration: 0.25, options:UIViewAnimationOptions.transitionCrossDissolve, animations: { () -> Void in
                self.rightLabel.textColor = self.expanded ? self.tintColor : self.rightLabelTextColor
        }, completion: nil)
        
        tableView.beginUpdates()
        tableView.endUpdates()
    }
    
 
    func datePicked() {
        
        
        
         if leftLabel.text == "Start"{
            
            let milliseconds:Int   = NSDate.milliseconds(sinceMidnight: datePicker.date) as Int
           // let millsSinceMidnight:JavaLangInteger  = JavaLangInteger(int :  jint(milliseconds) )
            
            
            
            self.schedule?.setESMSStartTime( jint(milliseconds) )
             date = datePicker.date
 
        }
        else if leftLabel.text == "End"{
            
            let milliseconds:Int   = NSDate.milliseconds(sinceMidnight: datePicker.date) as Int
            self.schedule?.setESMSEndime(jint(milliseconds) )
             date = datePicker.date
        }
         else{
        
          let milliseconds:Int   = NSDate.milliseconds(sinceMidnight: datePicker.date) as Int
          let millsSinceMidnight:JavaLangInteger  = JavaLangInteger(value :  jint(milliseconds) )

        
          self .signalTime?.setFixedTimeMillisFromMidnightWith(millsSinceMidnight)
          date = datePicker.date
        }
        
    }
}
