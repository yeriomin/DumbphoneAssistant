package com.github.yeriomin.dumbphoneassistant;

import android.app.Activity;
import android.net.Uri;

public abstract class PhoneUtil extends Util {

    public PhoneUtil(Activity activity) {
        super(activity);
    }
    
    /**
     * Retrieves the Uri to the contact using the Contacts.Phones path. If the given Contact contains an ID it will be
     * assumed that it's an ID from the Contacts.Phones path and used. If no ID is present, the name and number is used
     * to resolve the first matching contact. 
     * 
     * @param contact The contact containing the required information to be able to resolve the Uri
     * @return the Uri of the found contact or null if none found 
     */
    public abstract Uri retrieveContactUri(Contact contact);

}