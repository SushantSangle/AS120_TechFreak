package com.Techfreaks.SafeTrigger;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;


import android.database.Cursor;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.Techfreaks.utils.SharedPreferencesKt;
import com.Techfreaks.Services.mEventListener;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import com.Techfreaks.utils.permissionHandler;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Mobile Data mode" ;
    public static boolean MasterSwitchVal,Contact_BVal,Local_BVal,Media_BVal,Contact_SVal,Local_SVal,Media_SVal;
    Map<String,?> settings;
    Set<String> Contacts;
    TextView ContactList;

    private static final String NOTIF_CHANNEL_ID = "safeTriggerChannel";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        permissionHandler.checkPermissions(this); //check for all permissions needed by the application
        //The above method does not automatically read the permissions requested in Manifest.
        final Switch MasterSwitch = findViewById(R.id.Master_switch);

        final CheckBox Contact_B = findViewById(R.id.contact_B);
        final CheckBox Local_B = findViewById(R.id.local_auth_B);
        final CheckBox Local_S = findViewById(R.id.local_auth_S);
//        final CheckBox Media_B = findViewById(R.id.media_push_B);
//        final CheckBox Media_S = findViewById(R.id.media_push_S);
        final CheckBox Contact_S = findViewById(R.id.contact_S);
        final Button AddContact = findViewById(R.id.AddContact);

        ContactList = findViewById(R.id.ContactList);

        createChannels();
        if(MasterSwitchVal) {
            if (!getMobileDataState()) {
                setMobileDataState(true);
            }
            startService(new Intent(getApplicationContext(), mEventListener.class));
        }
        MasterSwitch.setChecked(MasterSwitchVal);

        Contact_B.setChecked(Contact_BVal);
        Local_B.setChecked(Local_BVal);
//        Media_B.setChecked(Media_BVal);

        Contact_S.setChecked(Contact_SVal);
        Local_S.setChecked(Local_SVal);
//        Media_S.setChecked(Media_SVal);

        final ScrollView MainScroll = findViewById(R.id.MainScroll);

        if(MasterSwitchVal){
            MainScroll.setVisibility(View.VISIBLE);
        }
        else{
            MainScroll.setVisibility(View.GONE);
        }

        ContactList.setText(getContactList());

        MasterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "MasterSwitchVal", isChecked);
                MasterSwitchVal = isChecked;
                if(MasterSwitchVal) {
                    MainScroll.setVisibility(View.VISIBLE);
                    if (!getMobileDataState()) {
                        setMobileDataState(true);
                    }
                    startService(new Intent(getApplicationContext(), mEventListener.class));
                }
                else{
                    MainScroll.setVisibility(View.GONE);
                    if (!getMobileDataState()) {
                        setMobileDataState(true);
                    }
                    Intent intent = new Intent(getApplicationContext(),com.Techfreaks.Services.mEventListener.class);
                    intent.putExtra("TERMINATE",true);
                    startService(intent);
                }
            }
        });
        Contact_B.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "Contact_BVal", isChecked);
                Contact_BVal = isChecked;
            }
        });
        Local_B.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "Local_BVal", isChecked);
                Local_BVal = isChecked;
            }
        });
//        Media_B.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "Media_BVal", isChecked);
//                Media_BVal = isChecked;
//            }
//        });
        Contact_S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "Contact_SVal", isChecked);
                Contact_SVal = isChecked;
            }
        });
        Local_S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "Local_SVal", isChecked);
                Local_SVal = isChecked;
            }
        });
//        Media_S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                SharedPreferencesKt.updateSharedPrefs(getApplicationContext(), "safeTriggerSettings", "Media_SVal", isChecked);
//                Media_SVal = isChecked;
//            }
//        });

        AddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Contacts!=null && Contacts.size()>=5) {

                    Toast.makeText(getApplicationContext(), "Maximum no. of contacts already set", Toast.LENGTH_SHORT).show();
                } else {
                    selectContact();
                }
            }
        });
    }

    /*code for getting contact */
    static final int REQUEST_SELECT_CONTACT = 1;

    public void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_CONTACT);
        }
    }

    @Override
    public void onActivityResult(int RequestCode, int ResultCode, Intent ResultIntent) {

        super.onActivityResult(RequestCode, ResultCode, ResultIntent);

        if (ResultCode == Activity.RESULT_OK) {

            Uri uri;
            Cursor cursor1, cursor2;
            String TempNameHolder, TempNumberHolder, TempContactID, IDresult;

            uri = ResultIntent.getData();

            assert uri != null;
            cursor1 = getContentResolver().query(uri, null, null, null, null);

            assert cursor1 != null;
            if (cursor1.moveToFirst()) {

                TempNameHolder = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                TempContactID = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));

                IDresult = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (IDresult.equals("1")) {

                    cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + TempContactID, null, null);

                    assert cursor2 != null;
                    if (cursor2.moveToNext()) {

                        TempNumberHolder = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        boolean exists = addContact(TempNumberHolder+" : "+TempNameHolder);

                        Toast.makeText(getApplicationContext(), (exists?"Contact already added":(TempNumberHolder + " added")), Toast.LENGTH_SHORT).show();
                        ContactList.setText(getContactList());
                    }
                    cursor2.close();
                }

            }
            cursor1.close();
        }
    }


    void getSettings(){
        settings = SharedPreferencesKt.getAllSettings(this);
        try {
            assert settings != null;
            MasterSwitchVal = ((Object)1 == settings.get("MasterSwitchVal"));
            Contact_BVal    = ((Object)1 == settings.get("Contact_BVal"));
            Local_BVal      = ((Object)1 == settings.get("Local_BVal"));
            Media_BVal      = ((Object)1 == settings.get("Media_BVal"));
            Contact_SVal    = ((Object)1 == settings.get("Contact_SVal"));
            Local_SVal      = ((Object)1 == settings.get("Local_SVal"));
            Media_SVal      = ((Object)1 == settings.get("Media_SVal"));

            Contacts = SharedPreferencesKt.getContactList(this);
        }catch(NullPointerException e){
            Toast.makeText(this,"No contact Settings found",Toast.LENGTH_SHORT).show();
            Contacts = new HashSet<>();
        }
    }

    String getContactList(){
        try {
            Iterator<String> i = Contacts.iterator();
            StringBuilder OutputString = new StringBuilder();
            while (i.hasNext())
                OutputString.append(i.next()).append("\n");
            return (OutputString.toString());
        }catch(NullPointerException e){
            return ("No contacts added");
        }
    }

    boolean addContact(String contact){
        if(Contacts == null)    Contacts = new HashSet<>();
        if(Contacts.contains(contact)) return true;

        Contacts.add(contact);
        SharedPreferencesKt.updateContactList(this,Contacts);
        return false;
    }

    public void createChannels(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, name, importance);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            channel.setDescription(description);

            AudioAttributes achannel = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            NotificationChannel channel1 = new NotificationChannel("Alert_channel", (CharSequence)"Alert", NotificationManager.IMPORTANCE_HIGH);
            channel1.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.siren),achannel);
            channel1.setBypassDnd(true);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert nm != null;
            nm.createNotificationChannel(channel);
            nm.createNotificationChannel(channel1);
        }

    }

    public boolean getMobileDataState()
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            return (boolean) getMobileDataEnabledMethod.invoke(telephonyService);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error getting mobile data state", ex);
        }

        return false;
    }

    public void setMobileDataState(boolean mobileDataEnabled)
    {
        try {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = Objects.requireNonNull(telephonyService).getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
        } catch (Exception ex) {
            Log.e("MainActivity", "Error setting mobile data state", ex);
        }
    }
}