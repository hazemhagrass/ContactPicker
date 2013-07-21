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

- (void)addContact:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options{
    self.callbackID = [arguments objectAtIndex:0];
    
    ABNewPersonViewController *newPersonController = [[ABNewPersonViewController alloc] init];
    newPersonController.newPersonViewDelegate = self;
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:newPersonController];
    
    [self.viewController presentViewController:navigationController animated:YES completion:nil];
}

- (void)newPersonViewController:(ABNewPersonViewController *)newPersonView didCompleteWithNewPerson:(ABRecordRef)person{
    NSLog(@"new person is added");
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

- (BOOL)peoplePickerNavigationController: (ABPeoplePickerNavigationController *)peoplePicker
shouldContinueAfterSelectingPerson:(ABRecordRef)person {
        NSString * firstName, *email;
        firstName = (__bridge NSString *)ABRecordCopyValue(person, kABPersonFirstNameProperty);
        
        ABMultiValueRef multi = ABRecordCopyValue(person, kABPersonEmailProperty);
        if(ABMultiValueGetCount(multi) > 0)
            email = (__bridge NSString *)ABMultiValueCopyValueAtIndex(multi, 0);
        else
            email = @"";
        
        
        NSLog(@"%@ %@", firstName, email);
        
        NSMutableDictionary* contact = [NSMutableDictionary dictionaryWithCapacity:2];
        [contact setObject:email forKey: @"email"];
        [contact setObject:firstName forKey: @"displayName"];
        
        [super writeJavascript:[[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:contact] toSuccessCallbackString:self.callbackID]];
        [self.viewController dismissModalViewControllerAnimated:YES];
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