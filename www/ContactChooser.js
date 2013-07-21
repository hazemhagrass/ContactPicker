/**
 * @constructor
 */
var ContactPicker = function(){};


ContactPicker.prototype.chooseContact = function(success, failure){
    cordova.exec(success, failure, "ContactPicker", "chooseContact", []);
};

// Plug in to Cordova
cordova.addConstructor(function() {

    if (!window.Cordova) {
        window.Cordova = cordova;
    };


    if(!window.plugins) window.plugins = {};
    window.plugins.ContactPicker = new ContactPicker();
});
