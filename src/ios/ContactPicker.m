#import "ContactPicker.h"
#import <Cordova/CDVAvailability.h>

@implementation ContactPicker
@synthesize callbackID;

- (void) chooseContact:(CDVInvokedUrlCommand*)command{
    self.callbackID = command.callbackId;
    
    ABPeoplePickerNavigationController *picker = [[ABPeoplePickerNavigationController alloc] init];
    picker.peoplePickerDelegate = self;
    [self.viewController presentModalViewController:picker animated:YES];
}

- (void)addContact:(CDVInvokedUrlCommand *)command{
    self.callbackID = command.callbackId;
    
    ABNewPersonViewController *newPersonController = [[ABNewPersonViewController alloc] init];
    newPersonController.newPersonViewDelegate = self;
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:newPersonController];
    
    [self.viewController presentViewController:navigationController animated:YES completion:nil];
}

- (NSMutableDictionary *)convertToDictionary:(ABRecordRef)person {
    NSString *fullName, *email;
    fullName = (__bridge NSString*)ABRecordCopyCompositeName(person);
    if (!fullName) {
        fullName = @"";
    }
    
    ABMultiValueRef multi = ABRecordCopyValue(person, kABPersonEmailProperty);
    if(ABMultiValueGetCount(multi) > 0)
        email = (__bridge NSString *)ABMultiValueCopyValueAtIndex(multi, 0);
    else
        email = @"";
    
    
    NSLog(@"%@ %@", fullName, email);
    
    NSMutableDictionary* contact = [NSMutableDictionary dictionaryWithCapacity:2];
    if (email) {
    }
    [contact setObject:email forKey: @"email"];
    [contact setObject:fullName forKey: @"displayName"];
    
    ABRecordID recordID = ABRecordGetRecordID(person); // ABRecordID is a synonym (typedef) for int32_t
    [contact setObject:@(recordID) forKey:@"id"];
    return contact;
}

- (void)respondToJS:(NSMutableDictionary *)contact {
    [super writeJavascript:[[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:contact] toSuccessCallbackString:self.callbackID]];
    [self.viewController dismissModalViewControllerAnimated:YES];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:contact];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
}

- (void)newPersonViewController:(ABNewPersonViewController *)newPersonView didCompleteWithNewPerson:(ABRecordRef)person{
    
    NSMutableDictionary *contact;
    contact = [self convertToDictionary:person];
    
    [self respondToJS:contact];
}

- (BOOL)peoplePickerNavigationController: (ABPeoplePickerNavigationController *)peoplePicker
shouldContinueAfterSelectingPerson:(ABRecordRef)person {
    
    NSMutableDictionary *contact;
    contact = [self convertToDictionary:person];
    
    [self respondToJS:contact];
    
    return NO;
}


- (BOOL) personViewController:(ABPersonViewController*)personView shouldPerformDefaultActionForPerson:(ABRecordRef)person property:(ABPropertyID)property identifier:(ABMultiValueIdentifier)identifierForValue
{
    return YES;
}

- (void)peoplePickerNavigationControllerDidCancel:(ABPeoplePickerNavigationController *)peoplePicker{
    [self.viewController dismissModalViewControllerAnimated:YES];
    [super writeJavascript:[[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
      messageAsString:@"People picker abort"]
    toErrorCallbackString:self.callbackID]];
}

@end