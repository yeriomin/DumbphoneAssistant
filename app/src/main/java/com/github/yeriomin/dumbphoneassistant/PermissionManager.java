package com.github.yeriomin.dumbphoneassistant;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(23)
public class PermissionManager {

    public final static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 125; // Any number
    private final String[] permissionsRequired = new String[] {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS
    };
    private ArrayList<String> permissionsGranted = new ArrayList<String>();
    private Activity activity;

    PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean permissionsGranted() {
        boolean granted = true;
        ArrayList<String> permissionsNotGranted = new ArrayList<String>();
        for (int i = 0; i < this.permissionsRequired.length; i++) {
            if (activity.checkSelfPermission(this.permissionsRequired[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(this.permissionsRequired[i]);
            } else {
                this.permissionsGranted.add(this.permissionsRequired[i]);
            }
        }
        if (permissionsNotGranted.size() > 0) {
            granted = false;
            String[] notGrantedArray = permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
            activity.requestPermissions(notGrantedArray, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
        return granted;
    }

    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = false;
        List<String> requiredPermissions = Arrays.asList(this.permissionsRequired);
        for (int i = 0; i < permissions.length; i++) {
            if (requiredPermissions.contains(permissions[i])
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                this.permissionsGranted.add(permissions[i]);
            }
        }
        if (this.permissionsGranted.size() == this.permissionsRequired.length) {
            granted = true;
        }
        return granted;
    }
}
