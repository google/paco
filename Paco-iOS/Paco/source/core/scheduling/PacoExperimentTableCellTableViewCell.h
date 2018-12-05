//
//  PacoExperimentTableCellTableViewCell.h
//  Paco
//
//  Created by Timo on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PacoRefreshProtocol.h"


@class PAExperimentDAO;
@interface PacoExperimentTableCellTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *name;
@property (weak, nonatomic) IBOutlet UILabel *id;
@property (weak, nonatomic) IBOutlet UILabel *experimentId;
@property (nonatomic,strong) PAExperimentDAO* dao;
@property (nonatomic,strong) id<PacoRefreshProtocol> parent;
@property (weak, nonatomic) IBOutlet UIButton *joinBtn;
@property (weak, nonatomic) IBOutlet UILabel *status;
@end
