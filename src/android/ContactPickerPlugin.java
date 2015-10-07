package com.badrit.ContactPicker;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContactPickerPlugin extends CordovaPlugin {

    private Context context;
    private CallbackContext callbackContext;

    private static final int CHOOSE_CONTACT = 1;
    private static final int INSERT_CONTACT = 2;

    @Override
    public boolean execute(String action, JSONArray data,
                           CallbackContext callbackContext) {
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
        } else if (action.equals("addContact")) {

            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

            try {
                JSONObject contact = data.getJSONObject(0);
                if (contact != null) {
                    intent.putExtra(Intents.Insert.NAME, contact.getString("displayName"));
                    intent.putExtra(Intents.Insert.EMAIL, contact.getString("email"));
                    intent.putExtra(Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                    intent.putExtra(Intents.Insert.PHONE, contact.getString("mobileNumber"));
                }
            } catch (Exception ex) {
            }


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
        Cursor c = context.getContentResolver().query(contactData, null, null,
                null, null);

        String id = "";
        String selectedPhone = "";
        if (requestCode == INSERT_CONTACT) {
            c.moveToLast();
            id = c.getInt(c.getColumnIndexOrThrow(PhoneLookup._ID)) + "";
        } else if (requestCode == CHOOSE_CONTACT) {
            c.moveToFirst();
            id = c.getInt(c
                    .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                    + "";
            selectedPhone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        try {
            String name = getContactName(c);
            String email = getContactEmail(id);
            JSONObject phones = getContactPhones(id);
            String photoUrl = getContactPhotoUrl(id);

            JSONObject contact = new JSONObject();
            contact.put("id", id);
            contact.put("email", email);
            contact.put("displayName", name);
            contact.put("phones", phones);
            contact.put("photoUrl", photoUrl);
            contact.put("selectedPhone", selectedPhone);

            callbackContext.success(contact);

            c.close();

        } catch (Exception e) {
            Log.v("ContactPicker", "Parsing contact failed: " + e.getMessage());
            callbackContext.error("Parsing contact failed: " + e.getMessage());
        }
    }

    private String getContactPhotoUrl(String id) {
        try {
            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                .parseLong(id));

        Uri photoUri = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        String path = getContactPhotoThumbnail(id, photoUri);

        return path;
    }

    private String getContactPhotoThumbnail(String id, Uri thumbUri) {
        AssetFileDescriptor afd = null;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            afd = context.getContentResolver().
                    openAssetFileDescriptor(thumbUri, "r");

            FileDescriptor fdd = afd.getFileDescriptor();

            inputStream = new FileInputStream(fdd);
            File file = File.createTempFile("contact_" + id, ".tmp");
            file.deleteOnExit();
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();

            return "file://" + file.getAbsolutePath();
        } catch (Exception e) {
        } finally {
            try {
                if (afd != null)
                    afd.close();
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
            }
        }
        return "";
    }

    private JSONObject getContactPhones(String id) throws JSONException {
        Cursor phonesCur = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{id}, null);

        JSONObject phones = new JSONObject();

        while (phonesCur.moveToNext()) {
            int index = phonesCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = phonesCur.getString(index);
            int type = phonesCur
                    .getInt(phonesCur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            phones.put(type + "", phoneNumber);
        }
        phonesCur.close();

        return phones;
    }

    private String getContactEmail(String id) {
        String email = "";
        Cursor emailCur = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{id}, null);
        while (emailCur.moveToNext())
            email = emailCur
                    .getString(emailCur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        emailCur.close();
        return email;
    }

    private String getContactName(Cursor c) {
        return c.getString(c
                .getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
    }
}