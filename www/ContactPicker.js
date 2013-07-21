/**
 * @constructor
 */
var ContactPicker = function(){};


ContactPicker.prototype.chooseContact = function(success, failure){
    cordova.exec(success, failure, "ContactPicker", "chooseContact", []);
};

ContactPicker.prototype.addContact = function(success, failure){
    cordova.exec(success, failure, "ContactPicker", "addContact", []);
};

// Plug in to Cordova
cordova.addConstructor(function() {

    if (!window.Cordova) {
        window.Cordova = cordova;
    };


    if(!window.plugins) window.plugins = {};
    window.plugins.ContactPicker = new ContactPicker();
});
