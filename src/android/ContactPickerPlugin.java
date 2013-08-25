package com.badrit.contact_picker;

import android.app.Activity;
import android.content.ContentUris;
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

            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            
            intent.putExtra("finishActivityOnSaveCompleted", true);
            
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

        if (resultCode != Activity.RESULT_OK)
            return;
        
        Uri contactData = data.getData();
        Cursor c =  context.getContentResolver().query(contactData, null, null, null, null);

        if(requestCode == INSERT_CONTACT)
            c.moveToLast();

        else if (requestCode == CHOOSE_CONTACT)
            c.moveToFirst();

        try {
            String id = c.getInt(c.getColumnIndexOrThrow(PhoneLookup._ID)) + "";
            String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            String email = "";
            
            Cursor emailCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null); 
            if (emailCur.moveToNext()) 
                email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            emailCur.close();
            
            JSONObject contact = new JSONObject();
            contact.put("id", id);
            contact.put("email", email);
            contact.put("displayName", name);
            
            callbackContext.success(contact);

            c.close();

        } catch (Exception e) {
             Log.v("wapp", "Parsing contact failed: " + e.getMessage());
            callbackContext.error("Parsing contact failed: " + e.getMessage());
        }
    }

}