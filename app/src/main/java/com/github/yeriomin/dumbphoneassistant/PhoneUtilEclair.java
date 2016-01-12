package com.github.yeriomin.dumbphoneassistant;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

import java.util.ArrayList;

/**
 * @TargetApi(5)
 */
public class PhoneUtilEclair extends PhoneUtil {

    public PhoneUtilEclair(Activity activity) {
        super(activity);
    }

    public ArrayList<Contact> get() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {
                PhoneLookup._ID,
                PhoneLookup.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor results = resolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        // create array of Phone contacts and fill it
        final ArrayList<Contact> phoneContacts = new ArrayList<Contact>(results.getCount());
        while (results.moveToNext()) {
            final Contact phoneContact = new Contact(
                    results.getString(results.getColumnIndex(PhoneLookup._ID)),
                    results.getString(results.getColumnIndex(PhoneLookup.DISPLAY_NAME)),
                    results.getString(results.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            );
            phoneContacts.add(phoneContact);
        }
        results.close();
        return phoneContacts;
    }

    public boolean create(Contact newPhoneContact) throws Exception {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newPhoneContact.getName())
                .build()
        );
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newPhoneContact.getNumber())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
                .build()
        );

        ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);

        Uri uri = results[0].uri;
        // if contacts uri returned, there was an error with adding the number
        if (uri.getPath().contains("people")) {
            throw new Exception(String.valueOf(R.string.error_phone_number_not_stored));
        }
        // if phone uri returned, everything went OK
        if (!uri.getPath().contains("phones")) {
            // some unknown error has happened
            throw new Exception(String.valueOf(R.string.error_phone_number_error));
        }
        newPhoneContact.setId(uri.getLastPathSegment());
        return true;
    }

    public Uri retrieveContactUri(Contact contact) {
        String lookupKey;
        Long contactId;
        Cursor result = null;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.RawContacts.CONTACT_ID };
        String selection;

        // at first try to resolve with contacts id
        if (contact.getId() != null) {
            selection = PhoneLookup._ID + "=?";
            result = resolver.query(uri, projection, selection, new String[] { contact.getId() }, null);
            // check if unique result
            if (result.getCount() != 1) {
                result.close();
                result = null;
            }
        }
        
        // if no contact id or no result, try alternate method
        if (result == null) {
            selection = ContactsContract.Contacts.DISPLAY_NAME + " = '" + contact.getName()
                    + "' AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + contact.getNumber() + "'"
            ;
            result = resolver.query(uri, projection, selection, null, null);
            // check if unique result
            if (result.getCount() != 1) {
                result.close();
                result = null;
            }
        }
                
        // check for result
        if (result == null) {
            return null;
        }
        
        // get results
        result.moveToNext();
        lookupKey = result.getString(0);
        contactId = result.getLong(1);
        result.close();

        // create contact URI
        return ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
    }
}
