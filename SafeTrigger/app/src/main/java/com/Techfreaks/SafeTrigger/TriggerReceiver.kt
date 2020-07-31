package com.Techfreaks.SafeTrigger

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.Techfreaks.Services.messageHelper
import com.Techfreaks.utils.isValidTrigger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/*
* Do not create multiple objects of this class with same value of n, may create a problem :[*/
class TriggerReceiver(private val nTapCount: Int) : BroadcastReceiver() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val NOTIF_CHANNEL_ID = "safeTriggerChannel"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                if (isValidTrigger(context, nTapCount)) {
                    if(nTapCount == 5) {
                        getLocationAndForward(context)
                        Log.d("MyReceiver :: onReceive", "triggerDetected")
                    }else if(nTapCount == 3) {
                        Handler().post {
                            val SOS_3_launch = Intent(context,com.Techfreaks.SafeTrigger.SOS_placeholder::class.java)
                            SOS_3_launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(SOS_3_launch)
                        }
                    }
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                if(isValidTrigger(context,nTapCount)) {
                    if(nTapCount == 5) {
                        getLocationAndForward(context)
                        Log.d("MyReceiver :: onReceive", "triggerDetected")
                    }else if(nTapCount == 3) {
                        Handler().post {
                            val SOS_3_launch = Intent(context,com.Techfreaks.SafeTrigger.SOS_placeholder::class.java);
                            SOS_3_launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(SOS_3_launch)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocationAndForward(mContext: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        cancelNotification(mContext)
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        messageHelper.firstTime=true;
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                        messageHelper.SendMsg(mContext,1,1,location)
                    if(location==null){
                        messageHelper.SendMsg(mContext,1,1,null)
                    }
                }
    }

    private fun cancelNotification(mContext : Context){
        val notificationIntent = Intent(mContext, com.Techfreaks.Services.NotificationHandler::class.java)
        val pendingIntent = PendingIntent.getService(mContext, 0, notificationIntent, 0)
        val notificationBuild = NotificationCompat.Builder(mContext,NOTIF_CHANNEL_ID)
                .setContentTitle("Cancel SOS ?")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentText("Press cancel SOS to let people know you are safe.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.ic_baseline_announcement_24,"Cancel SOS",pendingIntent)
                .build();
        val nv = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nv.notify(420,notificationBuild)
    }
}