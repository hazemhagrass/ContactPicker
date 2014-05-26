# ContactPicker plugin for Cordova / PhoneGap

This Plugin is inspired from ContactChooser plugin [here](https://github.com/monday-consulting/ContactChooser)

This Plugin brings up a native iOS or Android contact-picker overlay, accessing the addressbook and returning the selected contact's name, phone number and email, also new contact can be added.
## Usage

This has been successfully tested from Cordova 2.2.0 through to version 3.1.0.

Example Usage: 

1. **Pick contact**

```js
window.plugins.ContactPicker.chooseContact(function(contactInfo) {
//to get all phones numbers
    for (var i = 0; i < contactInfo.phones.length; i++) {
	     alert(contactInfo.phones[i]);
    };
    alert(contactInfo.displayName + " " + contactInfo.phones[0] + " " + contactInfo.email);
});
```

The method which will return a JSON. Example:

```json
{
    displayName: "John Doe",
    email: "john.doe@mail.com",
    phones: ["numbers"]
}
```
2. **Add new contact**

```js
//you can prefilled variable by add contact object 
var contact = {};
contact.displayName = "displayName";
contact.email = "email";
contact.phoneNumber = "phoneNumber";
window.plugins.ContactPicker.addContact(contact, function(contactInfo) {
    alert(contactInfo.displayName + " " + contactInfo.phones[0] + " " + contactInfo.email);
});
//or you can leave it empty by ad passing null in parameter
window.plugins.ContactPicker.addContact(null, function(contactInfo) {
    alert(contactInfo.displayName + " " + contactInfo.phones[0] + " " + contactInfo.email);
});
```

## MIT Licence

Copyright 2013 Monday Consulting GmbH

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
