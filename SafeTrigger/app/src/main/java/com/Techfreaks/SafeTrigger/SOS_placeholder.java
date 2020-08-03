package com.Techfreaks.SafeTrigger;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.Techfreaks.utils.SharedPreferencesKt;

import java.util.Set;


//This is the class for The options that will be shown after the three button trigger
public class SOS_placeholder extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static String selectedContact;
    private String[] contactsCurrent;
    public static SOS_placeholder activity;

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.setShowWhenLocked(true);
            this.setTurnScreenOn(true);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_o_s_placeholder);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        Spinner contactList = findViewById(R.id.spinner);
        Set<String> contacts = SharedPreferencesKt.getContactList(this);
        assert contacts != null;
        String contactsUnited = String.join(",",contacts);
        contactsCurrent = contactsUnited.split(",");

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, contactsCurrent);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactList.setOnItemSelectedListener(this);
        contactList.setAdapter(aa);

        Button call_number = findViewById(R.id.call_emergency_contact);
        call_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                try {
                    String contactNumber = SOS_placeholder.selectedContact.split(" : ", 2)[0];
                    intent.setData(Uri.parse("tel:" + contactNumber));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        if (ActivityCompat.checkSelfPermission(SOS_placeholder.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            startActivity(intent);
                        }else{
                            Toast.makeText(SOS_placeholder.this,"You have not permitted this application to make calls yet please allow",Toast.LENGTH_SHORT).show();
                            ActivityCompat.requestPermissions(SOS_placeholder.this,new String[]{Manifest.permission.CALL_PHONE},201);
                        }

                    }

                }catch(Exception e){

                    Toast.makeText(SOS_placeholder.this,"No number selected",Toast.LENGTH_SHORT).show();
                    Log.e("SOS_PLACEHOLDER_ERROR", "onClick: "+e.toString());
                }
            }
        });

        Button sendMessage = findViewById(R.id.send_sms_to_emergency_contact);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedContact = contactsCurrent[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
