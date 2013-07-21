#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <AddressBookUI/AddressBookUI.h>
#import <Cordova/CDVPlugin.h>

@interface ContactPicker : CDVPlugin <ABPersonViewControllerDelegate>

@property(strong) NSString* callbackID;

- (void) chooseContact:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;

- (void) addContact:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;

@end
