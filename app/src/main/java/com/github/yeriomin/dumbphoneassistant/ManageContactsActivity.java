package com.github.yeriomin.dumbphoneassistant;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ManageContactsActivity extends TabActivity {

    private ListView phoneView;
    private ListView simView;
    private List<Contact> phoneContacts;
    private List<Contact> simContacts;
    
    private SimUtil simUtil;
    private PhoneUtil phoneUtil;
    private ProgressDialog progressDialog;
    private PermissionManager permissionManager;

    private final int EDIT_REQUEST_CODE = 42; // Any number

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivityForResult(new Intent(this, DumbphoneAssistantPreferenceActivity.class), 1);
                break;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean permissionsGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionManager = new PermissionManager(this);
            permissionsGranted = permissionManager.permissionsGranted();
        }
        if (permissionsGranted) {
            initListViews();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if this was called after editing a phone contact, refresh the view
        if (requestCode == EDIT_REQUEST_CODE) {
            phoneContacts = phoneUtil.get();
            refreshPhoneListView();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    /**
     * refreshes the sim contacts ListViews using the current values stored in simContacts
     */
    private void refreshSimListView() {
        simView.setAdapter(new SimRowAdapter(simContacts));
    }

    /**
     * refreshes the phone contacts ListView using the current values stored in phoneContacts
     */
    private void refreshPhoneListView() {
        phoneView.setAdapter(new PhoneRowAdapter(phoneContacts));
    }

    /**
     * initializes the Phone and SIM ListViews by reading the phoneContacts and simContacts, setting up
     * the ListViews and adding all required handlers to them 
     */
    private void initListViews() {
        simUtil = new SimUtil(this);
        phoneUtil = Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR
                ? new PhoneUtilDonut(this)
                : new PhoneUtilEclair(this)
        ;

        setContentView(R.layout.main);

        TabHost mTabHost = getTabHost();
        mTabHost.addTab(mTabHost.newTabSpec("tab_phone").setIndicator(getString(R.string.phone_tab_title)).setContent(R.id.phoneview));
        mTabHost.addTab(mTabHost.newTabSpec("tab_simcard").setIndicator(getString(R.string.sim_tab_title)).setContent(R.id.simview));
        mTabHost.setCurrentTab(0);

        LayoutInflater lf = getLayoutInflater();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        phoneView = (ListView) findViewById(R.id.phoneview);
        View headerPhoneView = lf.inflate(R.layout.list_item_phone, phoneView, false);
        headerPhoneView.findViewById(R.id.button_edit).setVisibility(View.INVISIBLE);
        TextView titlePhone = (TextView) headerPhoneView.findViewById(R.id.text_contact_name);
        titlePhone.setText(getString(R.string.title_move_all_contacts_to_sim));
        titlePhone.setTypeface(null, Typeface.BOLD);

        headerPhoneView.findViewById(R.id.button_to_sim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = phoneContacts.size();
                initProgressDialog(
                        R.string.progress_title_copy_all_contacts_to_sim,
                        R.string.progress_message_copy_all_contacts_to_sim,
                        size
                );
                BulkContactsWorker allToSimWorker = new BulkContactsWorker();
                allToSimWorker.setMode(BulkContactsWorker.COPY_ALL_TO_SIM);
                new Thread(allToSimWorker).start();
            }
        });
        phoneView.addHeaderView(headerPhoneView);

        simView = (ListView) findViewById(R.id.simview);
        View headerSimView = lf.inflate(R.layout.list_item_sim, simView, false);
        TextView titleSim = (TextView) headerSimView.findViewById(R.id.text_contact_name);
        titleSim.setText(getString(R.string.title_move_all_contacts_to_phone));
        titleSim.setTypeface(null, Typeface.BOLD);

        headerSimView.findViewById(R.id.button_to_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = simContacts.size();
                initProgressDialog(
                        R.string.progress_title_copy_all_contacts_to_phone,
                        R.string.progress_message_copy_all_contacts_to_phone,
                        size
                );
                BulkContactsWorker allToSimWorker = new BulkContactsWorker();
                allToSimWorker.setMode(BulkContactsWorker.COPY_ALL_TO_PHONE);
                new Thread(allToSimWorker).start();
            }
        });

        headerSimView.findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setCancelable(false)
                        .setMessage(getString(R.string.are_you_sure_all))
                        .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface i, int which) {
                                int size = simContacts.size();
                                initProgressDialog(
                                        R.string.progress_title_delete_all_sim_contacts,
                                        R.string.progress_message_delete_all_sim_contacts,
                                        size
                                );
                                BulkContactsWorker allToSimWorker = new BulkContactsWorker();
                                allToSimWorker.setMode(BulkContactsWorker.DELETE_ALL_FROM_SIM);
                                new Thread(allToSimWorker).start();
                            }
                        })
                        .setNegativeButton(getString(android.R.string.no), null)
                        .show()
                ;
            }
        });
        simView.addHeaderView(headerSimView);

        phoneContacts = phoneUtil.get();
        refreshPhoneListView();
        simContacts = simUtil.get();
        refreshSimListView();
    }

    private void startContactEditActivity(Contact contact) {

        Uri contactUri = phoneUtil.retrieveContactUri(contact);
        Intent editContact = new Intent(Intent.ACTION_EDIT, contactUri);

        editContact.setData(contactUri);

        startActivityForResult(editContact, EDIT_REQUEST_CODE);
    }

    private boolean deleteFromSim(Contact contact) {
        boolean result = simUtil.delete(contact);
        if (result) {
            simContacts.remove(contact);
        }
        return result;
    }

    private void copyToSim(Contact contact) throws Exception {

        // convert to Contact suitable for storage on SIM
        Contact newSimContact = simUtil.convertToSimContact(contact);

        // check, if already present on SIM
        if (simContacts.contains(newSimContact)) {
            throw new Exception(getString(R.string.error_sim_contact_already_present));
        }

        // create contact on SIM card
        try {
            simUtil.create(newSimContact);
        } catch (Exception e) {
            throw new Exception(getString(R.string.error_sim_contact_not_stored));
        }

        simContacts.add(0, newSimContact);
    }

    private void copyToPhone(Contact contact) throws Exception {

        Contact newPhoneContact = new Contact("", contact.getName(), contact.getNumber());

        // check, if already present on phone
        if (phoneContacts.contains(newPhoneContact)) {
            throw new Exception(getString(R.string.error_phone_contact_already_present));
        }

        // create contact on phone
        try {
            phoneUtil.create(contact);
            phoneContacts.add(0, contact);
        } catch (Exception e) {
            // This is an exception from some util class, so it is a string id
            throw new Exception(getString(Integer.parseInt(e.getMessage())));
        }
    }

    private void initProgressDialog(int stringIdTitle, int stringIdMessage, int max) {
        progressDialog.setTitle(getString(stringIdTitle));
        progressDialog.setMessage(getString(stringIdMessage, max));
        progressDialog.setProgress(0);
        progressDialog.setMax(max);
    }

    /**
     * Custom adapter which displays the contacts name and number in the listview
     */
    class ContactRowAdapter extends BaseAdapter {
        final List<Contact> contacts;
        final LayoutInflater inflater;
        int listItemId;
        
        public ContactRowAdapter(List<Contact> contacts) {
            super();
            this.contacts = contacts;
            this.inflater = getLayoutInflater();
        }
        
        public int getCount() {
            return contacts.size();
        }
        public long getItemId(int position) {
            return position;
        }
        public Object getItem(int position) {
            return contacts.get(position);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(this.listItemId, parent, false);
            }
            convertView.setClickable(false);
            
            Contact contact = (Contact)this.getItem(position);

            ((TextView)convertView.findViewById(R.id.text_contact_name)).setText(contact.getName());
            ((TextView)convertView.findViewById(R.id.text_phone)).setText(contact.getNumber());

            return convertView;
        }

        public void setListItemId(int id) {
            this.listItemId = id;
        }
    }

    class PhoneRowAdapter extends ContactRowAdapter {

        public PhoneRowAdapter(List<Contact> contacts) {
            super(contacts);
            this.setListItemId(R.layout.list_item_phone);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Contact contact = (Contact) this.getItem(position);

            ImageButton buttonEdit = (ImageButton) view.findViewById(R.id.button_edit);
            View.OnClickListener lEdit = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startContactEditActivity((Contact) v.getTag());
                }
            };
            buttonEdit.setOnClickListener(lEdit);
            buttonEdit.setTag(contact);

            ImageButton buttonToSim = (ImageButton) view.findViewById(R.id.button_to_sim);
            View.OnClickListener lToSim = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message;
                    try {
                        copyToSim((Contact) v.getTag());
                        message = getString(R.string.confirm_sim_contact_stored);
                        refreshSimListView();
                    } catch (Exception e) {
                        message = e.getMessage();
                    }
                    Toast.makeText(ManageContactsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            };
            buttonToSim.setOnClickListener(lToSim);
            buttonToSim.setTag(contact);

            return view;
        }
    }

    class SimRowAdapter extends ContactRowAdapter {

        public SimRowAdapter(List<Contact> contacts) {
            super(contacts);
            this.setListItemId(R.layout.list_item_sim);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Contact contact = (Contact) this.getItem(position);

            ImageButton buttonToPhone = (ImageButton) view.findViewById(R.id.button_to_phone);
            View.OnClickListener lToPhone = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message;
                    try {
                        Contact contact = (Contact) v.getTag();
                        copyToPhone(contact);
                        message = getString(R.string.confirm_phone_contact_number_stored, contact.getName());
                        refreshPhoneListView();
                    } catch (Exception e) {
                        message = e.getMessage();
                    }
                    Toast.makeText(ManageContactsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            };
            buttonToPhone.setOnClickListener(lToPhone);
            buttonToPhone.setTag(contact);

            ImageButton buttonDelete = (ImageButton) view.findViewById(R.id.button_delete);
            View.OnClickListener lDelete = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact contact = (Contact) v.getTag();
                    String name = contact.getName() + " <" + contact.getNumber() + ">";
                    new AlertDialog.Builder(v.getContext())
                        .setCancelable(false)
                        .setMessage(getString(R.string.are_you_sure, name))
                        .setPositiveButton(getString(android.R.string.yes), new DeleteHandler(contact))
                        .setNegativeButton(getString(android.R.string.no), null)
                        .show()
                    ;
                }
            };
            buttonDelete.setOnClickListener(lDelete);
            buttonDelete.setTag(contact);

            return view;
        }
    }

    class DeleteHandler implements DialogInterface.OnClickListener {

        private final Contact contact;

        public DeleteHandler(Contact contact) {
            this.contact = contact;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            boolean success = false;
            try {
                success = deleteFromSim(contact);
                refreshSimListView();
            } catch (Exception e) {
                // TODO: decide what to do with failed deletions
            }
            String message = getString(success
                    ? R.string.confirm_sim_contact_removed
                    : R.string.error_sim_error_during_contact_removal
            );
            Toast.makeText(ManageContactsActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    class BulkContactsWorker implements Runnable {

        final static public int DELETE_ALL_FROM_SIM = 1;
        final static public int COPY_ALL_TO_PHONE = 2;
        final static public int COPY_ALL_TO_SIM = 3;

        private int mode;

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
            List<Contact> cloneList = new ArrayList<>(this.mode == COPY_ALL_TO_SIM
                    ? phoneContacts
                    : simContacts
            );
            int failuresCounter = 0;
            for (Contact contact: cloneList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.incrementProgressBy(1);
                    }
                });
                try {
                    switch (this.mode) {
                        case COPY_ALL_TO_SIM:
                            copyToSim(contact);
                            break;
                        case COPY_ALL_TO_PHONE:
                            copyToPhone(contact);
                            break;
                        case DELETE_ALL_FROM_SIM:
                            deleteFromSim(contact);
                            break;
                        default:
                            throw new RuntimeException("Unknown mode supplied to BulkContactsWorker");
                    }
                } catch (Exception e) {
                    failuresCounter++;
                }
            }
            final int failures = failuresCounter;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if (mode == COPY_ALL_TO_PHONE) {
                        refreshPhoneListView();
                    } else {
                        refreshSimListView();
                    }
                    if (failures > 0) {
                        String message = getString(R.string.error_bulk_copy, failures);
                        if (mode == COPY_ALL_TO_SIM) {
                            message = message + " " + getString(R.string.error_sim_full);
                        }
                        Toast.makeText(ManageContactsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            initListViews();
        }
    }
}