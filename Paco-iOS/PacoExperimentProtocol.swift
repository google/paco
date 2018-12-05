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
    
    func didSelect(_ experiment:PAExperimentDAO)
    func didClose(_ experiment:PAExperimentDAO)
    func email(_ experiment:PAExperimentDAO)
    func editTime(_ experiment:PAExperimentDAO)
    func showEditView(_ experiment:PAExperimentDAO,indexPath:IndexPath)
    
}
