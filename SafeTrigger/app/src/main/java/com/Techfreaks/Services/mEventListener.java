package com.Techfreaks.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.Techfreaks.SafeTrigger.R;
import com.Techfreaks.SafeTrigger.SOS_placeholder;
import com.Techfreaks.SafeTrigger.TriggerReceiver;
import com.Techfreaks.utils.SmsListener;
import com.Techfreaks.utils.SmsReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executor;

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

    private TriggerReceiver triggerReceiver = threeTapReceiver();
    private TriggerReceiver superTrigger = fiveTapReceiver();

    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Handler mServiceHandler;
    public static boolean stopLocationSharing = false;
    private HandlerThread handlerThread;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        try {
            boolean term = intent.getBooleanExtra("TERMINATE", false);
            if (term) {
                stopForeground(true);
                stopSelf();
            }
            if(intent.getBooleanExtra("startSOS",false)){
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
                stopLocationSharing=false;
                stopLocationService();
            }
            Toast.makeText(this,"Current Location :"+location.getLatitude()+" "+location.getLongitude(),Toast.LENGTH_SHORT ).show();
            double a = location.getLongitude();
            copAlert(location);
            messageHelper.firstTime = messageHelper.EnableMessage;
            messageHelper.SendMsg(this, 1, 1, location);
        }catch(Exception e){
            Log.e(TAG, "onLocationChanged: "+e.toString());
        }
    }
    private void stopLocationService(){
        try {
            handlerThread.quitSafely();
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }catch(Exception e){
            Log.e(TAG, "handleLocationRequests: "+e.toString() );
        }
    }

    private void handleLocationRequests(boolean startSOS,boolean stopSOS){

        if (startSOS && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
    private void copAlert(Location location){

    }

}
