//
//  PacoTestViewer.m
//  Paco
//
//  Created by Tim O'brien on 3/3/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoTestViewer.h"
#import "PacoService.h"
#import "PacoPublicDefinitionLoader.h"
#import "PacoEnumerator.h" 
#import "PacoService.h" 
#import "PacoSerializer.h"
#import "PacoSerializeUtil.h"







@interface PacoTestViewer ()

@property(nonatomic, strong) id<PacoEnumerator> myExperimentsIterator;
@property(nonatomic, strong) id<PacoEnumerator> publicExperimentIterator;
@property(nonatomic, strong) PacoService* service;
@property(nonatomic, strong) PacoPublicDefinitionLoader* loader;

@end

@implementation PacoTestViewer

- (void)viewDidLoad {
    [super viewDidLoad];
    
   _publicExperimentIterator =  [PacoPublicDefinitionLoader  publicExperimentsEnumerator];
   _myExperimentsIterator = [PacoPublicDefinitionLoader  myExperimentsEnumerator];
    
    
    
    _service = [[PacoService alloc] init];
    _loader  = [[PacoPublicDefinitionLoader alloc] init];
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    
    
}

//loadMyDefinitionIDListWithBlock

- (IBAction)paginate:(id)sender {
    
  
   
    
  
    
   [_myExperimentsIterator loadNextPage:^(NSArray * array, NSError * error) {
       
       
       NSArray* classNames = [PacoSerializeUtil getClassNames];
       PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:classNames withNameOfClassAttribute:@"nameOfClass"];
       
       
       
       PAExperimentDAO * dao  =  [serializer buildModelObject:array[0]];
       
       
       NSLog(@" next page %@", dao );
       
   }];
     
 

 
    
    
    
    
}


- (IBAction)count:(id)sender {
    
    
    
    
    
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
