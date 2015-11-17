//
//  PacoOutput.swift
//  Paco
//
//  Created by northropo on 11/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

enum InputType {
    case OpenText
    case SingleSelect
    case MultipleSelect
    case Photo
    case Location
    case LikeT
    case Unknown
}
class PacoOutput: NSObject {
    
    var   input:PAInput2?
    var   val:AnyObject?
    var   type:InputType?
   
}
