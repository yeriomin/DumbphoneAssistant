package com.github.yeriomin.dumbphoneassistant;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;

import java.util.ArrayList;

public class PhoneUtilDonut extends PhoneUtil {

    public PhoneUtilDonut(Activity activity) {
        super(activity);
    }

    public ArrayList<Contact> get() {
        final String[] phoneProjection = new String[]{
                People.NAME,
                People.NUMBER,
                People._ID
        };

        Cursor results = resolver.query(
                People.CONTENT_URI,
                phoneProjection,
                null,
                null,
                People.NAME
        );

        // create array of Phone contacts and fill it
        final ArrayList<Contact> phoneContacts = new ArrayList<>();
        if (null != results) {
            while (results.moveToNext()) {
                final Contact phoneContact = new Contact(
                        results.getString(results.getColumnIndex(People._ID)),
                        results.getString(results.getColumnIndex(People.NAME)),
                        results.getString(results.getColumnIndex(People.NUMBER))
                );
                phoneContacts.add(phoneContact);
            }
            results.close();
        }
        return phoneContacts;
    }

    public void create(Contact newPhoneContact) throws Exception {
        // first, we have to create the contact
        ContentValues newPhoneValues = new ContentValues();
        newPhoneValues.put(Contacts.People.NAME, newPhoneContact.getName());
        Uri newPhoneRow = resolver.insert(Contacts.People.CONTENT_URI, newPhoneValues);

        // then we have to add a number
        newPhoneValues.clear();
        newPhoneValues.put(Contacts.People.Phones.TYPE, Contacts.People.Phones.TYPE_MOBILE);
        newPhoneValues.put(Contacts.Phones.NUMBER, newPhoneContact.getNumber());
        // insert the new phone number in the database using the returned uri from creating the contact
        newPhoneRow = resolver.insert(Uri.withAppendedPath(newPhoneRow, Contacts.People.Phones.CONTENT_DIRECTORY), newPhoneValues);

        // if contacts uri returned, there was an error with adding the number
        if (newPhoneRow.getPath().contains("people")) {
            throw new Exception(String.valueOf(R.string.error_phone_number_not_stored));
        }

        // if phone uri returned, everything went OK
        if (!newPhoneRow.getPath().contains("phones")) {
            // some unknown error has happened
            throw new Exception(String.valueOf(R.string.error_phone_number_error));
        }
    }


    public Uri retrieveContactUri(Contact contact) {
        String id = contact.getId();
        String[] projection = new String[] { Contacts.Phones.PERSON_ID };
        String path = null;
        Cursor result;
        if (null != id) {
            Uri uri = ContentUris.withAppendedId(Contacts.Phones.CONTENT_URI, Long.valueOf(id));
            result = resolver.query(uri, projection, null, null, null);
        } else {
            String selection = "name='?' AND number='?'";
            String[] selectionArgs = new String[] { contact.getName(), contact.getNumber() };
            result = resolver.query(Contacts.Phones.CONTENT_URI, projection, selection, selectionArgs, null);
        }
        if (null != result) {
            result.moveToNext();
            path = result.getString(0);
            result.close();
        }
        if (null == path) {
            return null;
        }
        return Uri.withAppendedPath(Contacts.People.CONTENT_URI, path);
    }
}
