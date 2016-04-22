//
//  PacoInputEvaluatorEx.m
//  Paco
//
//  Created by Northrop O'brien on 4/15/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoInputEvaluatorEx.h"
#import "ExperimentDAO.h" 
#import "PacoExperiment.h" 
#import "PAExperimentDAO+Helper.h" 
#import "Input2.h" 
#import "PacoExperimentInput.h" 
#import "PacoExpressionExecutor.h"




@interface PacoInputEvaluatorEx  ()

@property(nonatomic, strong) PacoExperiment* experiment;
@property(nonatomic, strong) NSArray* visibleInputs;
// key: "inputName", value: inputValue
@property(nonatomic, strong) NSMutableDictionary* inputValueDict;
// key: "inputName", value: NSPredicate object
@property(nonatomic, strong) NSDictionary* expressionDict;
// key: "inputName", value: PacoExperimentInput object
@property(nonatomic, strong) NSDictionary* indexDict;

// key: "inputName", value: PacoExperimentInput object @property(nonatomic, strong) NSDictionary* inputValueDict;
@end



@implementation PacoInputEvaluatorEx

- (id)initWithExperiment:(PacoExperiment*)experiment {
    self = [super init];
    if (self) {
        _experiment = experiment;
        _inputValueDict = [_experiment.experimentDao inputs];
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

//run time: 2 * N
- (void)buildExpressionDictionaryIfNecessary {
    
    //build expression dictionary lazily
    if (self.expressionDict != nil) {
        return;
    }
    
    
    //run time: N
    
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    
    NSMutableDictionary* variableDict = [NSMutableDictionary dictionary];
    NSDictionary* inputDictionary =  [self.experiment.experimentDao inputs];
    NSArray * keys = [inputDictionary allKeys];
    for (NSString* key in keys  ) {
        
        NSArray * arrayOfInput = [inputDictionary objectForKey:key];
        for (PAInput2* basicInput in arrayOfInput)
        {
            
            
            
            PacoExperimentInput* input  = [PacoExperimentInput pacoExperimentInputFromInput2:basicInput];
            
            NSAssert([input.name length] > 0, @"input name should non empty!");
            BOOL isMultiSelectedList = (input.responseEnumType == ResponseEnumTypeList && input.multiSelect);
            variableDict[input.name] = @(isMultiSelectedList);
            
        }

    }
    
    //run time: N
   
    
    inputDictionary =  [self.experiment.experimentDao inputs];
    keys = [inputDictionary allKeys];
    for ( NSString* key in keys)
    {
        
        
        NSArray * arrayOfInput = [inputDictionary objectForKey:key];
        
        for (PAInput2* basicInput in arrayOfInput)
        {
        
                PacoExperimentInput* input  = [PacoExperimentInput pacoExperimentInputFromInput2:basicInput];
                
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
                
                
                [PacoExpressionExecutor predicateWithRawExpression:rawExpression
                                            withVariableDictionary:variableDict
                                                          andBlock:completionBlock];
        }
    }
    
    self.expressionDict = dict;
  
}

//run time: 2 * N
- (NSArray*)evaluateAllInputs {
    
    [self buildExpressionDictionaryIfNecessary];
    
    
    
    NSDictionary * dictionary = [self.experiment.experimentDao inputs];
    NSArray*  keys = [dictionary allKeys];
    
    
    //run time: N
    for ( NSString*  key  in keys  )
    {
        
        NSArray* array =   [dictionary objectForKey:key];
        
        for ( PAInput2* questionInput in array)
        {
             PacoExperimentInput *question  = [PacoExperimentInput pacoExperimentInputFromInput2:questionInput];
             (self.inputValueDict)[question.name] = [question valueForValidation];
        }
  
    
        
    }
    
    //run time: N
    NSMutableArray *questions = [NSMutableArray array];
    NSDictionary* tempDict = [self.experiment.experimentDao inputs];
     keys = [tempDict allKeys];
    
    for ( NSString* key in keys )
    {
        NSArray*  array =   [dictionary objectForKey:key];
        for ( PAInput2* questionInput  in array)
        {
            
            PacoExperimentInput *question  = [PacoExperimentInput pacoExperimentInputFromInput2:questionInput];
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



+ (PacoInputEvaluatorEx*)evaluatorWithExperiment:(PacoExperiment*)experiment
{
    PacoInputEvaluatorEx* inputEvaluator = [[PacoInputEvaluatorEx alloc] initWithExperiment:experiment];
    
    return inputEvaluator;
    
}

- (NSError*) validateVisibleInputs
{
    return Nil;
    
}



@end
