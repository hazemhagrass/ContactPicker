#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <AddressBookUI/AddressBookUI.h>
#import <Cordova/CDVPlugin.h>

@interface ContactPicker : CDVPlugin <ABPersonViewControllerDelegate, ABPeoplePickerNavigationControllerDelegate, ABNewPersonViewControllerDelegate>

@property(strong) NSString* callbackID;

- (void) chooseContact:(CDVInvokedUrlCommand*)command;

- (void) addContact:(CDVInvokedUrlCommand*)command;

@end
