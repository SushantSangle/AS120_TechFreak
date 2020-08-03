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
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert nm != null;
        nm.cancel(420);
        BluetoothHelperKt.stopAdvertising(this);
        mEventListener.stopLocationSharing = true;
//        Intent intent2 = new Intent(this,mEventListener.class);
//        intent.putExtra("stopSOS",true);
//        startService(intent2);
        stopSelf();
        return super.onStartCommand(intent,flags,startID);
    }
}
