/**
 * @constructor
 */
var ContactPicker = function() {};


ContactPicker.prototype.chooseContact = function(success, failure) {
	var newContantInfo = null;
	cordova.exec(function(contactInfo) {
		newContantInfo = {
			id: contactInfo.id,
			displayName: contactInfo.displayName,
			email: contactInfo.email,
			photoUrl: contactInfo.photoUrl,
			address: [],
			selectedPhone: contactInfo.selectedPhone,
			phones: []
		};
		for (var i in contactInfo.phones) {
			if (contactInfo.phones[i].length)
			newContantInfo.phones = newContantInfo.phones.concat(contactInfo.phones[i]);
		};
		if(contactInfo.address && contactInfo.address.length){
			for (var i in contactInfo.address) {
				newContantInfo.address.push(contactInfo.address[i]);
			};
		}else{
			newContantInfo.address.push("")
		}
		success(newContantInfo);
	}, failure, "ContactPicker", "chooseContact", []);
};

ContactPicker.prototype.addContact = function(contact, success, failure) {
	var newContact = {};
	newContact.id = contact.id ? contact.id : "";
	newContact.email = contact.email ? contact.email : "";
	newContact.displayName = contact.displayName ? contact.displayName : "";
	newContact.nickname = contact.nickname ? contact.nickname : "";
	newContact.mobileNumber = contact.mobileNumber ? contact.mobileNumber : "";
	cordova.exec(success, failure, "ContactPicker", "addContact", [newContact]);
};

// Plug in to Cordova
cordova.addConstructor(function() {

	if (!window.Cordova) {
		window.Cordova = cordova;
	};


	if (!window.plugins) window.plugins = {};
	window.plugins.ContactPicker = new ContactPicker();
});
