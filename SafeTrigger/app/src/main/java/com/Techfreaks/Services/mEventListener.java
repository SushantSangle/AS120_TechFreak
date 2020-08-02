package com.Techfreaks.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.Techfreaks.SafeTrigger.MainActivity;
import com.Techfreaks.SafeTrigger.R;
import com.Techfreaks.SafeTrigger.TriggerReceiver;
import com.Techfreaks.utils.SharedPreferencesKt;
import com.Techfreaks.utils.SmsListener;
import com.Techfreaks.utils.SmsReceiver;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static com.Techfreaks.Services.FiveTapKt.fiveTapReceiver;
import static com.Techfreaks.Services.ThreeTapKt.threeTapReceiver;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class mEventListener extends Service implements com.google.android.gms.location.LocationListener {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "safeTriggerChannel";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;
    private long FASTEST_INTERVAL = 5000;
    private int REQUEST_FINE_LOCATION = 99;
    private String uid, copFoundID = "";
    private boolean copFound=false;
    private GeoQuery geoQuery;
    private GeoFire geoFire;
    private int radius = 1, complaint_id;
    boolean locationSet = false;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReference2;
    private Boolean cancelRequest = false, copSearch=true;

    private TriggerReceiver triggerReceiver = threeTapReceiver();
    private TriggerReceiver superTrigger = fiveTapReceiver();

    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Handler mServiceHandler;
    public static boolean stopLocationSharing = false;
    private HandlerThread handlerThread;
    Location mLastLocation;
    private boolean copSOS;

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void smsReceived(String messageText) {
                messageHelper.parseInputMsg(messageText, getApplicationContext());
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        BluetoothHelperKt.stopDiscovery(this);
        BluetoothHelperKt.startDiscovery(this);

        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                try {
                    double longitude = locationResult.getLastLocation().getLongitude();
                    onLocationChanged(locationResult.getLastLocation());
                } catch (Exception e) {
                    Log.e(TAG, "onLocationResult: " + e.toString());
                }
            }
        };
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        handlerThread = new HandlerThread("HandlerThreadName");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            boolean term = intent.getBooleanExtra("TERMINATE", false);
            if (term) {
                stopForeground(true);
                stopSelf();
            }
            if(intent.getBooleanExtra("startSOS",false)){
                BluetoothHelperKt.stopDiscovery(this);
                BluetoothHelperKt.stopAdvertising(this);
                handleLocationRequests(true,false);
            }
            if(intent.getBooleanExtra("stopSOS",false)){
                handleLocationRequests(false,true);
            }
         }catch(Exception e){
             Log.e(TAG, "onStartCommand: "+e.toString());
         }
        startForeground();
        return super.onStartCommand(intent,flags,startID);
    }
    
    private void startForeground(){
        registerReceiver(triggerReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(triggerReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(superTrigger, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(superTrigger, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        Intent notificationIntent = new Intent(this, com.Techfreaks.SafeTrigger.SOS_placeholder.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , notificationIntent,0 );

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.logo_bw)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("To prevent service from dying in background")
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build());
    }

    public static void alertSmS(Context mContext){
        Intent smsIntent = new Intent(Intent.ACTION_MAIN);
        smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
        smsIntent.setType("vnd.android-dir/mms-sms");
        PendingIntent pendingIntent = PendingIntent.getService(mContext,0, smsIntent,0);
        Notification alertSmS = new NotificationCompat.Builder(mContext,"Alert_channel")
                .setSmallIcon(R.drawable.logo_bw)
                .setContentTitle("Alert SOS Received")
                .setContentText("Please check your SMS application for potential SOS requests.")
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm!=null;
        nm.notify(69,alertSmS);
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if(stopLocationSharing){
                BluetoothHelperKt.stopAdvertising(this);
                BluetoothHelperKt.stopDiscovery(this);
                BluetoothHelperKt.startDiscovery(this);
                stopLocationSharing=false;
                stopLocationService();
            }
            Toast.makeText(this,"Current Location :"+location.getLatitude()+" "+location.getLongitude(),Toast.LENGTH_SHORT ).show();
            double a = location.getLongitude();

            BluetoothHelperKt.startAdvertising(this,String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
            if(copSOS) copAlert(location);
            messageHelper.firstTime = messageHelper.EnableMessage;
            messageHelper.SendMsg(this, 1, 1, location);
        }catch(Exception e){
            Log.e(TAG, "onLocationChanged: "+e.toString());
        }
    }
    private void stopLocationService(){
        try {
            if(copSOS) noCopFound();
            handlerThread.quitSafely();
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            messageHelper.firstTime=messageHelper.EnableMessage;
            messageHelper.SendMsg(this,1,2,null);
        }catch(Exception e){
            Log.e(TAG, "handleLocationRequests: "+e.toString() );
        }
    }

    private void handleLocationRequests(boolean startSOS,boolean stopSOS){

        if(SharedPreferencesKt.getCopSOSMode(this) || MainActivity.copsos){
            saveComplaintToCloud();
            copSOS = true;
            copFound=false;
            copSearch=true;
            copFoundID="";
        }
        else copSOS = false;
        if (startSOS && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            messageHelper.EnableMessage = MainActivity.Contact_SVal;
            handlerThread.start();
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,
                    handlerThread.getLooper());
        }
        if(stopSOS){
            try {
                Looper.myLooper().quit();
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }catch(Exception e){
                Log.e(TAG, "handleLocationRequests: "+e.toString() );
            }

        }
    }
    private void saveComplaintToCloud() {

        //Getting the complaint created date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
        DateFormat dfDate = new SimpleDateFormat("yyyy/MM/dd");
        String currTime = mdformat.format(calendar.getTime());
        String currDate = dfDate.format(Calendar.getInstance().getTime());

        //generating a random complaint number.
        Random random = new Random();
        complaint_id = random.nextInt(900000) + 100000;

        //Uploading the complaint details to Firebase db.
        databaseReference2 = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
        databaseReference2.child("complaint_id").setValue(complaint_id);
        databaseReference2.child("Citizen_uid").setValue(uid);
        databaseReference2.child("complaint_create_date").setValue(currDate);
        databaseReference2.child("complaint_create_time").setValue(currTime);
    }
    private void copAlert(Location location){
        Log.d("onchanged/////////////", String.valueOf(location));
        //Adding the citizen updated location to ongoing_complaints object of Firebase db.
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(String.valueOf(complaint_id));
        geoFire = new GeoFire(databaseReference1);
        geoFire.setLocation("citizen_location", new GeoLocation(location.getLatitude(), location.getLongitude()));
        mLastLocation = location;
        //If the location is set for first time, then set the complaint created coordinates to the ongoing_complaints object and find the nearest cop.
        if(!locationSet) {
            locationSet = true;
            databaseReference2.child("complaint_create_loc_lat").setValue(location.getLatitude());
            databaseReference2.child("complaint_create_loc_lng").setValue(location.getLongitude());
            getNearestCop();
        }

    }
    public void getNearestCop() {
        if(copFound){
            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                    .child(String.valueOf(complaint_id));
            geoFire = new GeoFire(databaseReference1);
            geoFire.setLocation("citizen_location", new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            return;
        }
        //Database reference for all on_duty cops from Firebase db.
        databaseReference = FirebaseDatabase.getInstance().getReference("cops_onduty");

        //Using GeoFire.queryAtLocation method for searching for nearest cop by incrementing the radius every time.
        GeoFire geoFire = new GeoFire(databaseReference);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);

        //Removing previous listeners.
        geoQuery.removeAllListeners();

        //Adding a new listener for geo query.
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //If a cop is found, then add the complaint id and citizen id to cops object in firebase db and cop id and complaint id to citizen object.
                if (!copFound && copSearch) {
                    copSearch = false;
                    copFound = true;

                    //The found cop ID.
                    copFoundID = key;

                    //Adding the citizen id and complaint id to cop object in Firebase db.
                    DatabaseReference copReference = FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(copFoundID);
                    HashMap map = new HashMap();
                    map.put("citizenID", uid);
                    map.put("complaint_id", complaint_id);
                    copReference.updateChildren(map);

                    //Adding the cop id and complaint id to the assigned object in Firebase db.
                    DatabaseReference citizenReference = FirebaseDatabase.getInstance().getReference().child("actors").child("citizens").child(uid);
                    HashMap map1 = new HashMap();
                    map1.put("assigned_cop_id", copFoundID);
                    map1.put("complaint_id", complaint_id);
                    citizenReference.updateChildren(map1);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            //Method called if the key is not found.
            @Override
            public void onGeoQueryReady() {

                //If the key/cop is not found, then increase the radius if search.
                if (!copFound && copSearch) {

                    //Incrementing the radius by 1 km.
                    radius++;
                    Toast.makeText(mEventListener.this, "radius: " + radius, Toast.LENGTH_SHORT).show();

                    if (radius > 700) {
                        noCopFound();
                    } else {

                        //If cop not found but radius is less than 10, then search again with the incremented radius.
                        getNearestCop();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private void noCopFound(){


        //Removing the current stored location from Firebase db and stopping the live location updates.
        try {

            //Removing citizen location from the Firebase db.
            geoFire.removeLocation("citizen_location");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("geo citizen", String.valueOf(e));

        }

        try {
            //Stopping all listeners from listening.
            geoQuery.removeAllListeners();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("geo listen", String.valueOf(e));
        }
        geoQuery.removeAllListeners();

        //Removing the current complaint from the ongoing_complaints object of Firebase db.
        try {
            //Stopping the live location updates.
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

//            DatabaseReference removeCompDetailsRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
//            removeCompDetailsRef.removeValue();
            databaseReference2 = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
            databaseReference2.removeValue();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("database", String.valueOf(e));

        }
        //Toggling the visibilit
        copSearch = false;
        radius = 1;

    }
}
