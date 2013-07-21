#import "ContactPicker.h"
#import <Cordova/CDVAvailability.h>

@implementation ContactPicker
@synthesize callbackID;

- (void) chooseContact:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options{
    self.callbackID = [arguments objectAtIndex:0];
    
    ABPeoplePickerNavigationController *picker = [[ABPeoplePickerNavigationController alloc] init];
    picker.peoplePickerDelegate = self;
    [self.viewController presentModalViewController:picker animated:YES];
}

- (BOOL)peoplePickerNavigationController:(ABPeoplePickerNavigationController*)peoplePicker
      shouldContinueAfterSelectingPerson:(ABRecordRef)person
                                property:(ABPropertyID)property
                              identifier:(ABMultiValueIdentifier)identifier
{
    if (kABPersonEmailProperty == property)
    {
        ABMultiValueRef multi = ABRecordCopyValue(person, kABPersonEmailProperty);
        int index = ABMultiValueGetIndexForIdentifier(multi, identifier);
        NSString *email = (NSString *)ABMultiValueCopyValueAtIndex(multi, index);
        NSString *displayName = (NSString *)ABRecordCopyCompositeName(person);

        NSMutableDictionary* contact = [NSMutableDictionary dictionaryWithCapacity:2];
        [contact setObject:email forKey: @"email"];
        [contact setObject:displayName forKey: @"displayName"];

        [super writeJavascript:[[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:contact] toSuccessCallbackString:self.callbackID]];
        [self.viewController dismissModalViewControllerAnimated:YES];
        return NO;
    }
    return YES;
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
