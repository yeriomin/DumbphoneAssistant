package com.github.yeriomin.dumbphoneassistant;

import android.telephony.PhoneNumberUtils;

public class Contact implements Comparable<Contact> {

    private String id;
    private String name;
    private String number;
    private String label;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getNumberWithoutSeparators() {
        return PhoneNumberUtils.stripSeparators(number);
    }

    public String getLabel() {
        return label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected Contact(String id, String name, String number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    protected Contact(String id, String name, String number, String label) {
        this(id, name, number);
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Contact)) {
            return false;
        }
        return PhoneNumberUtils.compare(number, ((Contact) o).number);
    }

    @Override
    public int compareTo(Contact other) {
        return this.name.compareTo(other.name);
    }
}