//
//  PacoResponseTableViewController.swift
//  Paco
//
//  Created by Timo on 11/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit
import Foundation
 

class PacoResponseTableViewController: UITableViewController,PacoInputTable {
    
      var selectedIndexPath :IndexPath?
      var validationOn:Bool?
      var  inputs:JavaUtilList!
    
      var    selectedString:String!
    
      var   cells = [Int:PacoTableViewExpandingCellBase]()
      var   inputValues = [Int:String]()
    
    
      let cellID         = "cellDate"
      let cellSelectId   = "cellSelect"
      let cellText       = "cellText"
      let cellMC   = "cellMultipleChoice"
      let cellLikert   = "cellLikeArt"
    
    
    enum InputType {
        case openText
        case singleSelect
        case multipleSelect
        case photo
        case location
        case likert
        case likertSmiley
        case unknown
    }
    
    
    
     func textChanged(_ row:Int,text:String)
     {
        inputValues[row] = text;
     }
    
    
    
    func selected(_ row: Int, selected: String)
    {
        inputValues[row] = selected;
    }
    
    init!(nibName nibNameOrNil: String!, bundle nibBundleOrNil: Bundle!, input userform:JavaUtilList )
    {
        
        self.inputs = userform
        super.init(nibName: nibNameOrNil , bundle: nibBundleOrNil)
    
    }
    

    required init!(coder aDecoder: NSCoder!)
    {
         super.init(coder:aDecoder)
 
    }
 
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        let backBtn = UIBarButtonItem(title: "Save", style: UIBarButtonItemStyle.plain, target: self, action: #selector(PacoResponseTableViewController.btnBack(_:)))
        
        
        let cancelBtn = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.plain, target: self, action: #selector(PacoResponseTableViewController.btnCancel(_:)))
 
        navigationItem.leftBarButtonItem  = backBtn
        navigationItem.rightBarButtonItem = cancelBtn
        
        
       // self.navigationController!.navigationItem.leftBarButtonItem = backBtn
        
        
        self.tableView.register(UINib(nibName:"PacoPickerTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellID)
        self.tableView.register(UINib(nibName:"PacoStringSelectorTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellSelectId)
        self.tableView.register(UINib(nibName:"PacoTextTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellText)

        self.tableView.register(UINib(nibName:"PacoMultipleChoiceCellTableViewCell", bundle: nil), forCellReuseIdentifier:self.cellMC)
        
        self.tableView.register(UINib(nibName:"PacoLikertCell", bundle: nil), forCellReuseIdentifier:self.cellLikert)
        
        
        
        
        

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    
    func registerTableCells()
    {
 
     
    }

    
    
    
    @IBAction func btnBack(_ btn:AnyObject)
    {

        
        
       self.navigationController?.popViewController(animated: true)

    }
    
    
    @IBAction func btnCancel(_ btn:AnyObject)
    {
        
        
        for(index,cell) in cells {
            
            
            let isValid:Bool =  cell.isValid()
            var ouput:PacoOutput = cell.getResuts()
            validationOn = false
            if(isValid == false)
            {
                validationOn = true
                self.tableView.reloadData()
                break
            }
       
            
         
            print("   \(index)   -- \(cell)  ")
            
            
            
        }
        
       // self.navigationController?.popViewControllerAnimated(true)
        
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        let count  = inputs.size() as Int32
        let  number2 = Int(count)
        return  number2
      
    }
    
    
    func getInputType(_ input:PAInput2) -> InputType
    {
        var  inputTypeResponse:InputType
        switch input.getResponseType() {
            
        case "list":
            
        let isMultiSelect  = input.value(forKey: "multiselect_") as! JavaLangBoolean
        if(isMultiSelect.booleanValue())
        {
            inputTypeResponse = InputType.multipleSelect
        }
        else
        {
             inputTypeResponse = InputType.singleSelect
            
        }
            
        case "open text": inputTypeResponse = InputType.openText
        case "photo": inputTypeResponse = InputType.photo
        case "location": inputTypeResponse = InputType.location
        case "likert": inputTypeResponse = InputType.likert
        default: inputTypeResponse = InputType.unknown
            
        }
        
        return inputTypeResponse
        
        
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        var  index = Int32((indexPath as NSIndexPath).row)
        var input:PAInput2 =  inputs.getWith(index) as! PAInput2
        
        var steps:JavaLangInteger   =   input.value(forKeyEx: "likertSteps") as!  JavaLangInteger
        var numSteps = steps.intValue
        
        
         var    cell:PacoTableViewExpandingCellBase?
         var t = getInputType(input)
        
            switch t
            {
                
            case InputType.openText:
                
                let   c:PacoTextTableViewCell
                c  = tableView.dequeueReusableCell(withIdentifier: self.cellText, for: indexPath) as! PacoTextTableViewCell
                c.inputLabel.text  = input.getName()
                c.inputTexInput.text  = inputValues[(indexPath as NSIndexPath).row]
                c.indexPath = indexPath
                c.parent = self
                cell = c
                
                
            case InputType.multipleSelect:
                let c:PacoMultipleChoiceCellTableViewCell;
                  c = tableView.dequeueReusableCell(withIdentifier: self.cellMC, for: indexPath) as! PacoMultipleChoiceCellTableViewCell
                   c.listChoices = input.getListChoices()
                   c.input = input;
                c.selectLabel.text = input.getName()
                c.reloadTable(input.getListChoices(),singleSelect:false)
              
                cell = c
            case InputType.singleSelect:
                
                let   c:PacoMultipleChoiceCellTableViewCell
                c = tableView.dequeueReusableCell(withIdentifier: self.cellMC, for: indexPath) as! PacoMultipleChoiceCellTableViewCell
                c.selectLabel.text = input.getName()
                c.listChoices = input.getListChoices()
                c.input = input;
           
                c.reloadTable(input.getListChoices(),singleSelect:true)
                
                
                cell = c
                
                
            case InputType.likert:
                
                let   c:PacoLikertCell
                
                c = tableView.dequeueReusableCell(withIdentifier: self.cellLikert, for: indexPath) as! PacoLikertCell
                
                c.likertLabel.text =  input.getName()
                c.input = input;
                 c.numberOfCheckmarks = numSteps
                c.arraingeLabels()
                cell = c
                
            default:
                
                let   c:PacoTextTableViewCell
                c  = tableView.dequeueReusableCell(withIdentifier: self.cellText, for: indexPath) as! PacoTextTableViewCell
                c.titleLabel.text = "Unknown type"
             
                cell = c
                
            }
      
            if( validationOn == true )
            {
                 cell!.displayValidationIndicator()
             }
        
            cell!.input = input
            
             cells[(indexPath as NSIndexPath).row] = cell;
        
        return cell!
      
        
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let previousIndexPath = selectedIndexPath
        if indexPath == selectedIndexPath {
            selectedIndexPath = nil
        } else {
            selectedIndexPath = indexPath
        }
        
        var indexPaths : Array<IndexPath> = []
        if let previous = previousIndexPath {
            indexPaths += [previous]
        }
        if let current = selectedIndexPath {
            indexPaths += [current]
        }
        if indexPaths.count > 0 {
            tableView.reloadRows(at: indexPaths, with: UITableViewRowAnimation.automatic)
        }
    }
    
    override func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        (cell as! PacoTableViewExpandingCellBase).watchFrameChanges()
    }
    
    override func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        (cell as! PacoTableViewExpandingCellBase).ignoreFrameChanges()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        for cell  in tableView.visibleCells
        {
            
            if cell is PacoTableViewExpandingCellBase
            {
               //  cell.ignoreFrameChanges()
            }
        }
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        let  index = Int32((indexPath as NSIndexPath).row)
        let input:PAInput2 =  inputs.getWith(index) as! PAInput2
        var height:CGFloat
        let t = getInputType(input)
        
        switch t
        {
            
        case InputType.openText:
            
 
             height = 200
            
        case InputType.multipleSelect:
            
            input.getListChoices().size();
            let count  = inputs.size() as Int32
            height  = CGFloat(count*55  ) + 100
            
   
        case InputType.singleSelect:

            input.getListChoices().size();
            let count  = inputs.size() as Int32
            height  = CGFloat(count*55 ) + 100
   
            
         case InputType.likert:
            height = 80
            
         default:
            height = 40

            
        }
        
 
 
        return height
      }
   
   
    
    
}
