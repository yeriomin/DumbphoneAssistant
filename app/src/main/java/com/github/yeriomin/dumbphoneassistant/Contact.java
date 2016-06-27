package com.github.yeriomin.dumbphoneassistant;

public class Contact {

    private String id = null;
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

    /**
     * Null-safe string compare
     */
    private boolean compareStrings(final String one, final String two) {
        if (one == null ^ two == null) {
            return false;
        }
        if (one == null && two == null) {
            return true;
        }
        return one.compareTo(two) == 0;
    }

    @Override
    public boolean equals(Object o) {
        // if not Contact, can't be true
        if(!(o instanceof Contact)) {
            return false;
        }
        Contact c = (Contact)o;
        
        // only if id's present, compare them
        if((id != null) && (id.length()) > 0 && (c.id.length() > 0)) {
            return c.id.compareTo(id) == 0;
        }
        
        // finally if numbers not equal...
        return compareStrings(number, c.number);
    }
}