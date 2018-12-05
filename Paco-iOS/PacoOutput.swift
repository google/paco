//
//  PacoOutput.swift
//  Paco
//
//  Created by Timo on 11/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

enum InputType {
    case openText
    case singleSelect
    case multipleSelect
    case photo
    case location
    case likeT
    case unknown
}
class PacoOutput: NSObject {
    
    var   input:PAInput2?
    var   val:AnyObject?
    var   type:InputType?
    var   complete:Bool?
}
