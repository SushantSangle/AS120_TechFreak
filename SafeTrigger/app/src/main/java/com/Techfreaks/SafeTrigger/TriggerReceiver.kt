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
class TriggerReceiver(private val nTapCount: Int, private val operation: (Context) -> Unit) : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                if (isValidTrigger(context, nTapCount)) {
                    operation(context)
                    if(nTapCount == 3) {
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
                    operation(context)
                    if(nTapCount == 3) {
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


}