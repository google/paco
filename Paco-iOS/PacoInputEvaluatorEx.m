/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

#import "PacoInputEvaluatorEx.h"
#import "ExperimentDAO.h" 
#import "PacoExperiment.h" 
#import "PAExperimentDAO+Helper.h" 
#import "Input2.h" 
#import "PacoExperimentInput.h" 
#import "PacoExpressionExecutor.h"
#import "ExperimentGroup.h"
#import "NSObject+J2objcKVO.h"
#import "PAExperimentGroup+PacoGroupHelper.h" 
#import "NSObject+J2objcKVO.h"


@interface PacoInputEvaluatorEx  ()

@property(nonatomic, strong) PacoExperiment* experiment;
@property(nonatomic, strong) NSDictionary * questions;

@property(nonatomic, strong) NSArray* visibleInputs;
// key: "inputName", value: inputValue
@property(nonatomic, strong) NSMutableDictionary* inputValueDict;
// key: "inputName", value: NSPredicate object
@property(nonatomic, strong) NSDictionary* expressionDict;
// key: "inputName", value: PacoExperimentInput object
@property(nonatomic, strong) NSDictionary* indexDict;


@property(nonatomic,strong) NSDictionary *  inputDict;

// key: "inputName", value: PacoExperimentInput object @property(nonatomic, strong) NSDictionary* inputValueDict;
@end



@implementation PacoInputEvaluatorEx

- (id)initWithExperimentAndGroup:(PacoExperiment*)experiment group:(PAExperimentGroup*) group;
{
    self = [super init];
    if (self) {
        
        _experiment = experiment;
        _group = group;
        
        NSArray* inputs = [_group allInputs];
        NSString* name  = [_group  valueForKeyEx:@"name"];
        _inputValueDict =   [[NSMutableDictionary alloc] initWithObjectsAndKeys:name,inputs, nil];
        
 
        //[[NSMutableDictionary alloc] initWithDictionary:[_experiment.experimentDao inputs]];
        
        [self buildIndex];

    }
    return self;
}


- (void)buildIndex
{
    
    NSMutableDictionary* dict =
    [NSMutableDictionary  new];
    
    NSDictionary * inputs =[self.experiment.experimentDao inputs];
    
    NSArray* allKeys = [inputs allKeys];
    
    for ( NSString  * key in   allKeys  )
    {
        IOSObjectArray* array  =  [inputs valueForKey:key];
        
       for( PAInput2* input in array)
       {
             NSString * inputName = [input getName];
             NSAssert([inputName length] > 0, @"input name should not be empty!");
             dict[inputName] = input;
       }
    }

    self.indexDict = [self processDictionry:dict];
  
}


-(NSDictionary*) processDictionry:(NSDictionary*) dictionary
{
    NSMutableDictionary* returnDictionary = [NSMutableDictionary new];
    NSArray * allKeys = [dictionary allKeys];
    
    for(NSString* key in allKeys)
    {
        PAInput2  * input = [dictionary objectForKey:key];
        if(input != nil)
        {
           [returnDictionary setValue:[PacoExperimentInput pacoExperimentInputFromInput2:input] forKey:key  ];
        }
        else
        {
            [returnDictionary setValue:[NSNull null]  forKey:key];
        }
    }
    return returnDictionary;
}






- (void)tagInputsAsDependency:(NSArray*)inputNameList {
    for (NSString* name in inputNameList) {
        PacoExperimentInput* input = (self.indexDict)[name];
        NSAssert(input != nil, @"input should not be nil!");
        input.isADependencyForOthers = YES;
    }
}


/* fetch all inputs accross all groups*/

-(NSDictionary*) makeInputDictionary
{
    
    if(self.questions)
        return self.questions;
        
    
    NSArray * inputs = [self.group  allInputs];
    NSMutableArray* pacoExperiments = [NSMutableArray new];
    
    
    for ( PAInput2* questionInput in inputs)
    {
        PacoExperimentInput *question
                = [PacoExperimentInput pacoExperimentInputFromInput2:questionInput];
        [pacoExperiments addObject:question];
    }

    NSString * groupName = [self.group getName];
    NSDictionary * dict = [[NSDictionary alloc]  initWithObjectsAndKeys:pacoExperiments,groupName, nil];
    

    
    
    
    
    self.questions = dict;
    
    return dict;
    
    
}









//run time: 2 * N
- (void)buildExpressionDictionaryIfNecessary {
    
    //build expression dictionary lazily
    if (self.expressionDict != nil) {
        return;
    }
    
    
    //run time: N
    
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    NSMutableDictionary* variableDict = [NSMutableDictionary dictionary];
    
   // NSDictionary* inputDictionary =  [self.experiment.experimentDao inputs];
    
   // NSArray * inputs  = [self.group allInputs];
    //NSString * name  = [self.group getName];
    
    
    
    
    /* fetch the inputs for the var group */
    
    if([[_inputDict allKeys] count] == 0)
      _inputDict = [self makeInputDictionary];
    
    
    NSArray * arrayOfInput;
    
    NSArray * keys = [_inputDict allKeys];
    /* actually only loops once because there should only be one key. refactor later*/
    for (NSString* key in keys  ) {
        
        
     arrayOfInput = [_inputDict objectForKey:key];
        
        
        /* process array of inputs */
        for (PacoExperimentInput* input in arrayOfInput)
        {
            /* we basically do a conversion so we can use existing code */
            
           // PacoExperimentInput* input  = [PacoExperimentInput pacoExperimentInputFromInput2:basicInput];
            
            NSAssert([input.name length] > 0, @"input name should non empty!");
            BOOL isMultiSelectedList = (input.responseEnumType == ResponseEnumTypeList && input.multiSelect);
            variableDict[input.name] = @(isMultiSelectedList);
            
        }

    }
    
    //run time: N
   
//    
//    inputDictionary = [self makeInputDictionary];
//    keys = [inputDictionary allKeys];
//    for ( NSString* key in keys)
//    {
//        
        /* we can reuse the basic we hve here. */
    
        //NSArray * arrayOfInput = [inputDictionary objectForKey:key];
    
    
    
        
        for (PacoExperimentInput* input in arrayOfInput)
        {
        
                //PacoExperimentInput* input  = [PacoExperimentInput pacoExperimentInputFromInput2:basicInput];
                
                if (!input.conditional) {
                    continue;
                }
                NSString* rawExpression = input.conditionalExpression;
                //we should be able to handle bad data on server safely
                if (0 == [rawExpression length]) {
                    
                   // DDLogError(@"Error: expression should not be empty!");
                    continue;
                }
                
                void(^completionBlock)(NSPredicate*, NSArray*) =
                ^(NSPredicate* predicate, NSArray* dependencyVariables){
                    if (predicate == nil) {
                       // DDLogError(@"[ERROR]failed to create a predicate for inputName: %@, expression: %@",
                                  /// input.name, rawExpression);
                        NSLog(@" error");
                    }else {
                        dict[input.name] = predicate;
                    }
                    [self tagInputsAsDependency:dependencyVariables];
                };
            
            NSLog(@" raw expression %@ \n varables %@", rawExpression, variableDict);
            
                
                [PacoExpressionExecutor predicateWithRawExpression:rawExpression
                                            withVariableDictionary:variableDict
                                                          andBlock:completionBlock];
        }
//    }
    
    self.expressionDict = dict;
  
}




//run time: 2 * N
- (NSArray*)evaluateAllInputs {
    
    [self buildExpressionDictionaryIfNecessary];

    NSDictionary * dictionary = [self makeInputDictionary];
    NSArray*  keys = [dictionary allKeys];
    
    
    //run time: N
    for ( NSString*  key  in keys  )
    {
        NSArray* array =   [dictionary objectForKey:key];
        for ( PacoExperimentInput* question in array)
        {
             (self.inputValueDict)[question.name] = [question valueForValidation];
        }
    }
    
    
    
    //run time: N
    NSMutableArray *questions = [NSMutableArray array];
    NSDictionary* tempDict = [self makeInputDictionary];
     keys = [tempDict allKeys];
    
    for ( NSString* key in keys )
    {
        /* get the Inputs for respective group name */
        NSArray*  array =   [dictionary objectForKey:key];
        for ( PacoExperimentInput* question   in array)
        {
            
            /* convert PacoExperimentInput to PacoExperimentInput and get reuse of previous codebase */
      
            BOOL visible =  [self evaluateSingleInput:question];
            
            
            if (visible) {
                [questions addObject:question];
            } else {
                //for the invisible inputs, their values are not valid to use for evaluating anymore, even if
                //their responseObject is not nil, so we should mark their values to be null
                (self.inputValueDict)[question.name] = [NSNull null];
            }
        }
        self.visibleInputs = questions;
           
     }
        
    return self.visibleInputs;
}

//In case of any possible error, we return YES so that those inputs can at least show up
- (BOOL)evaluateSingleInput:(PacoExperimentInput*)input{
    if (!input.conditional) {
        return YES;
    }
    NSPredicate* predicate = (self.expressionDict)[input.name];
    if (predicate == nil) {
        //DDLogError(@"[ERROR]No predicate to evaluate inputName: %@", input.name);
        return YES;
    }
    
    BOOL satisfied = NO;
    @try {
        satisfied = [predicate evaluateWithObject:nil substitutionVariables:self.inputValueDict];
    }
    @catch (NSException *exception) {
        satisfied = YES;
      //  DDLogError(@"[ERROR]Exception to evaluate single input: %@", [exception description]);
    }
    @finally {
        if (satisfied) {
            //NSLog(@"[Satisfied]InputName:%@, Expression:%@", input.name, input.conditionalExpression);
        }else {
            //NSLog(@"[NOT Satisfied]InputName:%@, Expression:%@", input.name, input.conditionalExpression);
        }
        return satisfied;
    }
}



+ (PacoInputEvaluatorEx*)evaluatorWithExperiment:(PacoExperiment*)experiment andGroup:(PAExperimentGroup*) group
{
    PacoInputEvaluatorEx* inputEvaluator = [[PacoInputEvaluatorEx alloc] initWithExperimentAndGroup:experiment  group:group];
    return inputEvaluator;
    
}

- (NSError*) validateVisibleInputs
{
    
    /* TODO implement */ 
    return Nil;
    
}



@end
