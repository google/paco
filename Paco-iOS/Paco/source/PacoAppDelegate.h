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

#import <UIKit/UIKit.h>
#import <CoreData/CoreData.h>
#import "Paco-Swift.h"




@class PacoMainSwiftViewController;
@class PacoMainViewController;
@class PacoTestingControllerTableViewController;
@class PacoMyExperiments;
@class PacoNotificationTester;

/* 888 */
@class ScheduleTestViewController;
@class PacoTableExperimentsController;
@class PacoJoinedExperimentsController;
@class PacoConfigController;
@class PacoResponseTableViewController;
@class   PacoTestViewer;
@class  HubPaginatedTableViewController;
@class ScheduleEditor;
@class PacoHubExperiments;
@class PacoViewController;
@class PacoConfigurationViewController;

@class PacoTestCustomFeedBackViewController;

static NSString *dataSource;

@interface PacoAppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) ScheduleTestViewController *testViewController;
@property (strong, nonatomic) PacoResponseTableViewController *responseMessageController;
@property (strong, nonatomic) PacoTableExperimentsController *testTableViewController;
@property (strong, nonatomic) PacoMainSwiftViewController *swiftViewController;
@property (strong,nonatomic) PacoMyExperiments* myExperiments;
@property (strong,nonatomic) PacoMyExperiments* hub;
@property (strong,nonatomic) PacoJoinedExperimentsController* joinedExperiment;
@property (strong,nonatomic) PacoConfigController* configController;
@property (strong,nonatomic) PacoHubExperiments* publicExperiments;
@property (strong,nonatomic) ScheduleEditor* scheduleEditor;
@property (strong,nonatomic) PacoViewController* goController;
@property (strong,nonatomic) PacoNotificationTester* alertTester;
@property (strong,nonatomic) PacoConfigurationViewController* configurationManager;
@property (strong,nonatomic) PacoTestCustomFeedBackViewController* custumFeedback;

@property (strong,nonatomic) PacoTestViewer* testPagination;
@property (strong,nonatomic) HubPaginatedTableViewController* pg;


@property (strong,nonatomic) PacoTestingControllerTableViewController*  swiftTest;

@property (strong,nonatomic) UIViewController*  viewController;

@property (strong,nonatomic) UIImageView * pacoView;

@property (strong,nonatomic)  UITabBarController *tabBar;


@property (strong, nonatomic) UILocalNotification* notificationFromAppLaunch;
@property(nonatomic,copy) NSArray *scheduledLocalNotifications NS_AVAILABLE_IOS(4_0);

@property (nonatomic, assign, readonly) BOOL isFirstLaunch;
@property (nonatomic, assign, readonly) BOOL isFirstOAuth2;


/* core data */
 
 @property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
 @property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
 @property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;

 
 - (void)saveContext;
 - (NSURL *)applicationDocumentsDirectory;
/* process received notificaiton */
- (void)processReceivedNotification:(UILocalNotification*)notification mustShowSurvey:(BOOL)mustShowSurvey;
- (void)processNotificationIfNeeded;
/*   can be hidden */
- (void)showNoSurveyNeededForNotification:(UILocalNotification*)notification;
- (void)showSurveyForNotification:(UILocalNotification*)notification;

@end
