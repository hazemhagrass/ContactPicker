package com.badrit.contact_picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.Intents;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContactPickerPlugin extends CordovaPlugin {

    private Context context;
    private CallbackContext callbackContext;

    private static final int CHOOSE_CONTACT = 1;
    private static final int INSERT_CONTACT = 2;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.context = cordova.getActivity().getApplicationContext();
        if (action.equals("chooseContact")) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

            cordova.startActivityForResult(this, intent, CHOOSE_CONTACT);

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return true;
        }else if (action.equals("addContact")) {

            Intent intent = new Intent(Intents.Insert.ACTION,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            
            cordova.startActivityForResult(this, intent, INSERT_CONTACT);

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("wapp", "index=" + resultCode);
        if (resultCode == Activity.RESULT_OK) {

            Uri contactData = data.getData();
            Cursor c =  context.getContentResolver().query(contactData, null, null, null, null);

            if (c.moveToFirst()) {
                try {
                    int id = c.getInt(c.getColumnIndexOrThrow(PhoneLookup._ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String email = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA));

                    JSONObject contact = new JSONObject();
                    contact.put("id", id);
                    contact.put("email", email);
                    contact.put("displayName", name);
                    callbackContext.success(contact);

                } catch (Exception e) {
                    callbackContext.error("Parsing contact failed: " + e.getMessage());
                }

            } else {
                callbackContext.error("Contact was not available.");
            }

            c.close();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            callbackContext.error("No contact was selected.");
        }
    }

}