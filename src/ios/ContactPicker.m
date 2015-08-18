#import "ContactPicker.h"
#import <Cordova/CDVAvailability.h>

@implementation ContactPicker
@synthesize callbackID;

#pragma mark - Public interface

- (void) chooseContact:(CDVInvokedUrlCommand*)command{
    self.callbackID = command.callbackId;
    
    [self checkAdressBookAccessWithCallback:^{
        [self showPeoplePickerNavigationController];
    }];
}

- (void)addContact:(CDVInvokedUrlCommand *)command{
    self.callbackID = command.callbackId;
    
    [self checkAdressBookAccessWithCallback:^{
        [self showNewPersonViewController];
    }];
}

#pragma mark - Showing Address Book view controllers

- (void)showPeoplePickerNavigationController {
    ABPeoplePickerNavigationController *picker = [[ABPeoplePickerNavigationController alloc] init];
    picker.peoplePickerDelegate = self;
    [self.viewController presentViewController:picker animated:YES completion:nil];
}

- (void)showNewPersonViewController {
    ABNewPersonViewController *newPersonController = [[ABNewPersonViewController alloc] init];
    newPersonController.newPersonViewDelegate = self;
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:newPersonController];
    
    [self.viewController presentViewController:navigationController animated:YES completion:nil];
}

#pragma mark - Address Book Access

- (void)checkAdressBookAccessWithCallback:(void (^)(void))callback {
    [self requestAccessWithCallback:^(BOOL success) {
        if (success) {
            if (callback) {
                callback();
            }
        }
        else {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                                     messageAsString:@"People picker denied access"]
                                        callbackId:self.callbackID];
        }
    }];
}

- (void)requestAccessWithCallback:(void (^)(BOOL success))callback {
    ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, NULL);
    
    if (ABAddressBookGetAuthorizationStatus() == kABAuthorizationStatusNotDetermined) {
        ABAddressBookRequestAccessWithCompletion(addressBookRef, ^(bool granted, CFErrorRef error) {
            if (callback) {
                callback(granted);
            }
        });
    }
    else if (ABAddressBookGetAuthorizationStatus() == kABAuthorizationStatusAuthorized) {
        if (callback) {
            callback(YES);
        }
    }
    else {
        if (callback) {
            callback(NO);
        }
    }
}

#pragma mark - Delegate calls

- (void)newPersonViewController:(ABNewPersonViewController *)newPersonView didCompleteWithNewPerson:(ABRecordRef)person{
    NSMutableDictionary *contact;
    if (person) {
        contact = [self convertToDictionary:person];
    }
    else { // If the user taps cancel, person is passed as NULL. This code block can be moved to convertToDictionary:
        //        contact = @{@"email" : @"",
        //                    @"displayName" : @"",
        //                    @"id" : @""}; // either create a dictionary with the same keys as expected, but with empty strings as values.
        
        //        contact = @{}; //, create an empty dictionary
        
        //        contact = nil; // or keep it as nil.
    }
    
    
    [self respondToJS:contact];
}

- (BOOL)peoplePickerNavigationController: (ABPeoplePickerNavigationController *)peoplePicker
      shouldContinueAfterSelectingPerson:(ABRecordRef)person {
    
    NSMutableDictionary *contact;
    contact = [self convertToDictionary:person];
    
    [self respondToJS:contact];
    
    return NO;
}

- (void)peoplePickerNavigationController:(ABPeoplePickerNavigationController *)peoplePicker didSelectPerson:(ABRecordRef)person{
    [self peoplePickerNavigationController:peoplePicker shouldContinueAfterSelectingPerson:person];
}

- (BOOL) personViewController:(ABPersonViewController*)personView shouldPerformDefaultActionForPerson:(ABRecordRef)person property:(ABPropertyID)property identifier:(ABMultiValueIdentifier)identifierForValue
{
    return YES;
}

- (void)peoplePickerNavigationControllerDidCancel:(ABPeoplePickerNavigationController *)peoplePicker{
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                             messageAsString:@"People picker abort"]
                                callbackId:self.callbackID];
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Parsing Used Data

- (NSString *)imageURLForRecord:(ABRecordRef)person fullName:(NSString *)fullName {
    CFDataRef imageData = ABPersonCopyImageData(person);
    if (!imageData) {
        return @"";
    }
    NSData *data = (__bridge NSData *)(imageData);
    
    NSString *tmpDirectory = NSTemporaryDirectory();
    NSString *fileName = [NSString stringWithFormat:@"%@_image.png", [fullName isEqualToString:@""] ? [NSDate date] : fullName];
    NSString *imagePath = [tmpDirectory stringByAppendingPathComponent:fileName];
    [data writeToFile:imagePath atomically:YES];
    
    CFRelease(imageData);
    
    return [NSURL fileURLWithPath:imagePath].absoluteString;
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
    
    ABMultiValueRef multiPhones = ABRecordCopyValue(person, kABPersonPhoneProperty);
    
    NSMutableDictionary* phones = [NSMutableDictionary dictionaryWithCapacity:2];
    
    for(CFIndex i = 0; i < ABMultiValueGetCount(multiPhones); i++) {
        NSString *label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiPhones, i);
        
        label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiPhones, i);
        NSLog(@"Phone Label: %@", label);

        [phones setObject:(__bridge NSString*)ABMultiValueCopyValueAtIndex(multiPhones, i) forKey: label];
    }
    
    ABMultiValueRef multiAddresses = ABRecordCopyValue(person, kABPersonAddressProperty);
    NSMutableDictionary* address = [NSMutableDictionary dictionaryWithCapacity:2];
    
    for (CFIndex i = 0; i < ABMultiValueGetCount(multiAddresses); i++) {
        NSString *label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiAddresses, i);
        
        label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiAddresses, i);
        NSLog(@"Phone Label: %@", label);
        NSDictionary *dictionary = (__bridge NSDictionary *)(ABMultiValueCopyValueAtIndex(multiAddresses, i));
        
        NSArray *keys = @[(__bridge NSString *)kABPersonAddressStreetKey, (__bridge NSString *)kABPersonAddressCityKey,
                          (__bridge NSString *)kABPersonAddressStateKey, (__bridge NSString *)kABPersonAddressCountryKey];
        
        NSMutableArray *values = [NSMutableArray array];
        
        for (NSString *key in keys) {
            NSString *value = dictionary[key];
            if (value && ![value isEqualToString:@""]) {
                [values addObject:value];
            }
        }
        
        [address setObject:[values componentsJoinedByString:@", "]forKey: label];
    }
    
    NSString *imageURL = [self imageURLForRecord:person fullName:fullName];
    
    NSLog(@"%@ %@", fullName, email);
    
    NSMutableDictionary* contact = [NSMutableDictionary dictionaryWithCapacity:2];
    if (email) {
    }
    [contact setObject:email forKey: @"email"];
    [contact setObject:fullName forKey: @"displayName"];
    [contact setObject:phones forKey:@"phones"];
    [contact setObject:address forKey:@"address"];
    contact[@"photoUrl"] = imageURL;
    
    ABRecordID recordID = ABRecordGetRecordID(person); // ABRecordID is a synonym (typedef) for int32_t
    [contact setObject:@(recordID) forKey:@"id"];
    return contact;
}

#pragma mark - Response

- (void)respondToJS:(NSMutableDictionary *)contact {
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:contact]
                                callbackId:self.callbackID];
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:contact];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
}

@end