//
//  PacoExperimentProtocol.swift
//  Paco
//
//  Created by Timo on 10/21/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

import UIKit

public protocol PacoExperimentProtocol
{
    
    func didSelect(experiment:PAExperimentDAO)
    func didClose(experiment:PAExperimentDAO)
    func email(experiment:PAExperimentDAO)
    func editTime(experiment:PAExperimentDAO)
    func showEditView(experiment:PAExperimentDAO,indexPath:NSIndexPath)
    
}
