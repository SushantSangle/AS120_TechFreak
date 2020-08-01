package com.Techfreaks.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.Techfreaks.SafeTrigger.MainActivity;
import com.Techfreaks.SafeTrigger.R;
import com.Techfreaks.SafeTrigger.SOS_placeholder;
import com.Techfreaks.SafeTrigger.TriggerReceiver;
import com.Techfreaks.utils.SmsListener;
import com.Techfreaks.utils.SmsReceiver;

import static android.content.ContentValues.TAG;
import static com.Techfreaks.Services.FiveTapKt.fiveTapReceiver;
import static com.Techfreaks.Services.ThreeTapKt.threeTapReceiver;

public class mEventListener extends Service {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "safeTriggerChannel";

    TriggerReceiver triggerReceiver = threeTapReceiver();
    TriggerReceiver superTrigger = fiveTapReceiver();

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void smsReceived(String messageText) {
                messageHelper.parseInputMsg(messageText,getApplicationContext());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startID){
         try {
             boolean term = intent.getBooleanExtra("TERMINATE", false);
             if (term) {
                 stopForeground(true);
                 stopSelf();
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
        Intent smsIntent = new Intent(mContext,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext,0, smsIntent,0);
        Notification alertSmS = new NotificationCompat.Builder(mContext,"Alert_channel")
                .setSmallIcon(R.drawable.logo_bw)
                .setContentTitle("Alert SOS Received")
                .setContentText("Please check your SMS application for potential SOS requests.")
                .build();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm!=null;
        nm.notify(69,alertSmS);
    }
    public static void cancelSms(Context mContext){
        Intent notificationIntent = new Intent(mContext, SOS_placeholder.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent,0 );
        Notification cancel_build = new NotificationCompat.Builder(mContext,"safeCheck")
                .setSmallIcon(R.drawable.logo_bw)
                .setContentTitle("Cancel SOS?")
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentText("Press cancel SOS to let people know you are safe")
                .setContentIntent(pendingIntent).build();
        Notification Cancel_build_private = new NotificationCompat.Builder(mContext,"SafeCheck")
                .setSmallIcon(R.drawable.logo_bw)
                .setContentTitle("Cancel SOS?")
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(cancel_build)
                .setContentText("Unlock your phone to cancel Trigger").build();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        nm.notify(420,Cancel_build_private);

    }

}
