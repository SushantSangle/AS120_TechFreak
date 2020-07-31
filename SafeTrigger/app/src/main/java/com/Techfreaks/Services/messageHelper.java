package com.Techfreaks.Services;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Techfreaks.SafeTrigger.R;
import com.Techfreaks.SafeTrigger.SOS_placeholder;
import com.Techfreaks.utils.SharedPreferencesKt;

public class messageHelper {

    final static String TAG = "SmsStatus";
    public static Boolean firstTime;
    public static int msgModeStatic;
    public static Location locationStatic;

    public static BroadcastReceiver sendSMS = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(getResultCode()){
                case Activity.RESULT_OK:
                    Log.d(TAG, "onReceive: RESULT_ok");
                    Toast.makeText(context,"SMS SENT",Toast.LENGTH_SHORT).show();
                    vibrate(1,context);
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.e(TAG, "onReceive: generic_error");
                    Toast.makeText(context, "Generic failure",Toast.LENGTH_SHORT).show();
                    SendMsg(context,2,msgModeStatic,locationStatic);
                    break;

                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Log.e(TAG, "onReceive: RESULT_ERROR_NO_SERVICE");
                    Toast.makeText(context, "No service",
                            Toast.LENGTH_SHORT).show();
                    SendMsg(context,2,msgModeStatic,locationStatic);
                    break;

                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Log.e(TAG, "onReceive: RESULT_ERROR_NULL_PDU ");
                    Toast.makeText(context, "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    SendMsg(context,2,msgModeStatic,locationStatic);
                    break;

                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Log.e(TAG, "onReceive: RESULT_ERROR_RADIO_OFF");
                    Toast.makeText(context, "Radio off",
                            Toast.LENGTH_SHORT).show();
                    SendMsg(context,2,msgModeStatic,locationStatic);
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void SendMsg(final Context mContext, final int mode,final int msgMode, @Nullable final Location location) {
        if(!firstTime) return;
        msgModeStatic = msgMode;
        locationStatic = location;
        SubscriptionManager sm = mContext.getSystemService(SubscriptionManager.class);

        int default_subscription_id = SmsManager.getDefaultSmsSubscriptionId();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            Log.e(TAG, "SendMsg: Error with READ_PHONE_STATE");
            return;
        }
        assert sm != null;
        List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();

        if(mode==2) {
            if (default_subscription_id == subscriptions.get(0).getSubscriptionId())
                default_subscription_id = subscriptions.get(1).getSubscriptionId();
            else
                default_subscription_id = subscriptions.get(0).getSubscriptionId();
        }
        Log.d(TAG, "SendMsg: subscription id :"+default_subscription_id);
        final SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(default_subscription_id);

        final Set<String> Contacts = SharedPreferencesKt.getContactList(mContext);
        final String messageString = location!=null?getMsgData(msgMode,location):getMsgData(msgMode,null);

        final PendingIntent sentPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("SMS_SENT"), 0);

        mContext.registerReceiver(sendSMS,new IntentFilter("SMS_SENT"));

        if(mode==2) firstTime=false;
        if(firstTime)
            mEventListener.cancelSms(mContext);
        try {
            assert Contacts != null;
            for (String Contact : Contacts) {
                final String contactNumber = Contact.split(" : ", 2)[0];
                Toast.makeText(mContext, "Phone number is: " + contactNumber, Toast.LENGTH_LONG).show();
                smsManager.sendTextMessage(contactNumber, null, messageString, sentPendingIntent, null);

            }
        }catch(NullPointerException e){
            Toast.makeText(mContext, "No Contacts added", Toast.LENGTH_SHORT).show();
        }

    }


    protected static void vibrate(int mode,Context context){
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert vibrator != null;
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            assert vibrator != null;
            vibrator.vibrate(200);
        }
    }

    protected static String getMsgData(int mode,@Nullable Location location){
        if(mode == 1)
            return "#SafeTrigger SOS, I am in an emergency "+(location!=null?("\n Direction:"+"https://www.google.com/maps/dir/?api=1&destination="+location.getLatitude()+","+location.getLongitude()):".")+" generated by SafeTrigger.";
        else
            return "#SafeTrigger, The previous sent SOS was a false alarm, sorry for you inconvenience.";
    }

    public static void parseInputMsg(String messageBody,Context mContext){
        Pattern pattern = Pattern.compile("^#SafeTrigger",Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(messageBody);
        if(match.find()){
            Log.d(TAG, "parseInputMsg: SOS message Received");
            Toast.makeText(mContext,"ALERT RECEIVED",Toast.LENGTH_SHORT).show();
            mEventListener.alertSmS(mContext);
            MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.siren);
            mediaPlayer.start();
        }
    }

}
