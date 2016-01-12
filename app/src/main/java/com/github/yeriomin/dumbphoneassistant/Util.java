package com.github.yeriomin.dumbphoneassistant;

import android.app.Activity;
import android.content.ContentResolver;

import java.util.ArrayList;

public abstract class Util {

    protected static Activity activity;
    protected ContentResolver resolver;

    public Util(Activity activity) {
        this.activity = activity;
        this.resolver = activity.getContentResolver();
    }
    
    /**
     * Retrieves all contacts
     * 
     * @return List containing all Contact objects
     */
    public abstract ArrayList<Contact> get();

    /**
     * Creates a contact
     * 
     * @param newContact The Contact object containing the name and number of the contact
     * @return Success or not
     */
    public abstract boolean create(Contact newContact) throws Exception;

}