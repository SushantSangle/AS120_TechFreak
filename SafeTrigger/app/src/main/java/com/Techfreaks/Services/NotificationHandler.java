package com.Techfreaks.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class NotificationHandler extends Service {
    public NotificationHandler() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}
    
    String TAG = "NotificationHandler";
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(){
        super.onCreate();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent,int flags,int startID){
        Log.d(TAG, "onStartCommand: OnStartReached");
        messageHelper.firstTime=messageHelper.EnableMessage;
        messageHelper.SendMsg(this,1,2,null);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert nm != null;
        nm.cancel(420);
        mEventListener.stopLocationSharing = true;
//        Intent intent2 = new Intent(this,mEventListener.class);
//        intent.putExtra("stopSOS",true);
//        startService(intent2);
        stopSelf();
        return super.onStartCommand(intent,flags,startID);
    }
}
