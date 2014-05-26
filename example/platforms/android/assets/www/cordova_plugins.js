cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/com.badrit.ContactPicker/www/ContactPicker.js",
        "id": "com.badrit.ContactPicker.ContactPicker",
        "clobbers": [
            "navigator.ContactPicker"
        ]
    },
    {
        "file": "plugins/org.apache.cordova.device/www/device.js",
        "id": "org.apache.cordova.device.device",
        "clobbers": [
            "device"
        ]
    }
]
});