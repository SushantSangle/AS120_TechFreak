package com.Techfreaks.Services

import android.content.Intent
import android.os.Handler
import com.Techfreaks.SafeTrigger.SOS_placeholder
import com.Techfreaks.SafeTrigger.TriggerReceiver

fun threeTapReceiver(): TriggerReceiver {
    return TriggerReceiver(3) {
        Handler().post {
            val SOS_3_launch = Intent(it, SOS_placeholder::class.java)
            SOS_3_launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.startActivity(SOS_3_launch)
        }
    }
}