package com.Techfreaks.Services

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.Techfreaks.SafeTrigger.R
import com.Techfreaks.SafeTrigger.SOS_placeholder
import com.Techfreaks.SafeTrigger.TriggerReceiver
import com.google.android.gms.location.LocationServices
import com.google.firebase.inappmessaging.CommonTypesProto


private val NOTIF_CHANNEL_ID = ""


@RequiresApi(Build.VERSION_CODES.M)
private fun getLocationAndForward(mContext: Context) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
//    cancelNotification(mContext)
    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    messageHelper.firstTime=true;
    fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                messageHelper.SendMsg(mContext,1,1,location)
                if(location==null){
                    messageHelper.SendMsg(mContext,1,1,null)
                }
            }
}

private fun cancelNotification(mContext : Context){
    val notificationIntent = Intent(mContext, NotificationHandler::class.java)
//    val notificationIntent = Intent(mContext,TriggerReceiver::class.java)
//    notificationIntent.putExtra("StopSOS",true)
    val pendingIntent = PendingIntent.getService(mContext, 0, notificationIntent, 0)
    val notificationBuild = NotificationCompat.Builder(mContext,"Cancel_channel")
            .setContentTitle("Cancel SOS ?")
            .setSmallIcon(R.drawable.ic_baseline_announcement_24)
            .setContentText("Press cancel SOS to let people know you are safe.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(R.drawable.ic_baseline_announcement_24,"Cancel SOS",pendingIntent)
            .build();
    val nv = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nv.notify(420,notificationBuild)
}

fun fiveTapReceiver(): TriggerReceiver {
    return TriggerReceiver(5) {
//        getLocationAndForward(it)
        val intent = Intent(it,mEventListener::class.java)
        intent.putExtra("startSOS",true)
        it.startService(intent)
        cancelNotification(it)
    }
}