package com.badrit.ContactPicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import org.json.JSONObject;

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
        if (requestCode == INSERT_CONTACT) {
            c.moveToLast();
            id = c.getInt(c.getColumnIndexOrThrow(PhoneLookup._ID)) + "";
        } else if (requestCode == CHOOSE_CONTACT) {
            c.moveToFirst();
            id = c.getInt(c
                    .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                    + "";
        }

        try {

            String name = c
                    .getString(c
                            .getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            String email = "";

            Cursor emailCur = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[] { id }, null);
            while (emailCur.moveToNext())
                email = emailCur
                        .getString(emailCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            emailCur.close();

            Cursor phonesCur = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[] { id }, null);
            JSONObject phones = new JSONObject();
            JSONArray homeNumper = new JSONArray();
            JSONArray mobileNumber = new JSONArray();
            JSONArray workNumper = new JSONArray();
            JSONArray faxWorkNumper = new JSONArray();
            JSONArray faxHomeNumper = new JSONArray();
            JSONArray pagerNumper = new JSONArray();
            JSONArray otherNumper = new JSONArray();
            JSONArray callbackNumper = new JSONArray();
            JSONArray carNumper = new JSONArray();
            JSONArray companyMainNumper = new JSONArray();
            JSONArray isdnNumper = new JSONArray();
            JSONArray mainNumper = new JSONArray();
            JSONArray otherFaxNumper = new JSONArray();
            JSONArray radioNumper = new JSONArray();
            JSONArray telexNumper = new JSONArray();
            JSONArray ttyTddNumper = new JSONArray();
            JSONArray workMobileNumper = new JSONArray();
            JSONArray workPagerNumper = new JSONArray();
            JSONArray assistantNumper = new JSONArray();
            JSONArray mmsNumper = new JSONArray();

            while (phonesCur.moveToNext()) {
                int type = phonesCur
                        .getInt(phonesCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    homeNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("homeNumper", homeNumper);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    mobileNumber
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("mobileNumber", mobileNumber);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    workNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("workNumper", workNumper);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                    faxWorkNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("faxWorkNumper", faxWorkNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                    faxWorkNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("faxHomeNumper", faxHomeNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                    pagerNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("pagerNumper", pagerNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                    otherNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("otherNumper", otherNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
                    callbackNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("callbackNumper", callbackNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
                    carNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("carNumper", carNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                    companyMainNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("companyMainNumper", companyMainNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
                    isdnNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("isdnNumper", isdnNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                    mainNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("mainNumper", mainNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                    otherFaxNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("otherFaxNumper", otherFaxNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
                    radioNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("radioNumper", radioNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
                    telexNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("telexNumper", telexNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
                    ttyTddNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("ttyTddNumper", ttyTddNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                    workMobileNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("workMobileNumper", workMobileNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                    workPagerNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("workPagerNumper", workPagerNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
                    assistantNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("assistantNumper", assistantNumper);

                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
                    mmsNumper
                            .put(phonesCur.getString(phonesCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phones.put("mmsNumper", mmsNumper);

                    break;
                }
            }

            phonesCur.close();

            JSONObject contact = new JSONObject();
            contact.put("id", id);
            contact.put("email", email);
            contact.put("displayName", name);
            contact.put("phones", phones);

            callbackContext.success(contact);

            c.close();

        } catch (Exception e) {
            Log.v("wapp", "Parsing contact failed: " + e.getMessage());
            callbackContext.error("Parsing contact failed: " + e.getMessage());
        }
    }
}