package com.Techfreaks.SafeTrigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.Techfreaks.utils.isValidTrigger

/*
* Do not create multiple objects of this class with same value of n, may create a problem :[*/
class TriggerReceiver(private val nTapCount: Int, private val operation: (Context) -> Unit) : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                if (isValidTrigger(context, nTapCount)) {
                    operation(context)
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                if(isValidTrigger(context,nTapCount)) {
                    operation(context)
                }
            }
        }
    }
}