package com.github.yeriomin.dumbphoneassistant;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;

import java.util.ArrayList;

@TargetApi(5)
public class PhoneUtilEclair extends PhoneUtil {

    public PhoneUtilEclair(Activity activity) {
        super(activity);
    }

    public ArrayList<Contact> get() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {
                PhoneLookup._ID,
                PhoneLookup.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
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
        final ArrayList<Contact> phoneContacts = new ArrayList<>();
        int indexId = results.getColumnIndex(PhoneLookup._ID);
        int indexName = results.getColumnIndex(PhoneLookup.DISPLAY_NAME);
        int indexType = results.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
        int indexLabel = results.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
        int indexNumber = results.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        while (results.moveToNext()) {
            int type = results.getInt(indexType);
            String custom = results.getString(indexLabel);
            final Contact phoneContact = new Contact(
                    results.getString(indexId),
                    results.getString(indexName),
                    results.getString(indexNumber),
                    (String) Phone.getTypeLabel(this.activity.getResources(), type, custom)
            );
            phoneContacts.add(phoneContact);
        }
        results.close();
        return phoneContacts;
    }

    public void create(Contact contact) throws Exception {
        String name = contact.getName();
        // Prevents previously placed phone type suffixes from being interpreted as part of the name
        if (name.charAt(name.length() - 2) == ',') {
            name = name.substring(0, name.length() - 2);
            contact.setName(name);
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        );
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                .build()
        );
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getNumber())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        );

        ContentProviderResult[] results;
        try {
            results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            throw new Exception(String.valueOf(R.string.error_phone_number_error));
        }

        if (results.length > 2) {
            Uri uri = results[2].uri;
            // if contacts uri returned, there was an error with adding the number
            if (uri.getPath().contains("people")) {
                throw new Exception(String.valueOf(R.string.error_phone_number_not_stored));
            }
            contact.setId(uri.getLastPathSegment());
        }
    }

    public Uri retrieveContactUri(Contact contact) {
        String lookupKey;
        Long contactId;
        Cursor result = null;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.RawContacts.CONTACT_ID };
        String selection;
        String[] selectionArgs;

        // at first try to resolve with contacts id
        if (contact.getId() != null) {
            selection = PhoneLookup._ID + "=?";
            selectionArgs = new String[] { contact.getId() };
            result = resolver.query(uri, projection, selection, selectionArgs, null);
            // check if unique result
            if (result.getCount() != 1) {
                result.close();
                result = null;
            }
        }
        
        // if no contact id or no result, try alternate method
        if (result == null) {
            selection = ContactsContract.Contacts.DISPLAY_NAME + " = '?' AND "
                    + ContactsContract.CommonDataKinds.Phone.NUMBER + " = '?'"
            ;
            selectionArgs = new String[] { contact.getName(), contact.getNumber() };
            result = resolver.query(uri, projection, selection, selectionArgs, null);
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
