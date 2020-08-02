package com.Techfreaks.utils;

import android.Manifest;
import android.app.Activity;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_DENIED;


public class permissionHandler {

    public static void checkPermissions(Activity context){
        String [] permissions = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA,
        };

        Boolean flag = false;
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(context, permission)==PERMISSION_DENIED)
                flag=true;
        }
        if(!flag)
            Toast.makeText(context,"All permissions Granted",Toast.LENGTH_SHORT).show();
        else
            ActivityCompat.requestPermissions(context,permissions,200);
    }

}
