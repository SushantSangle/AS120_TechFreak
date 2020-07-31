package com.Techfreaks.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    public static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(" ", "onReceive: SMS received");
        Bundle input = intent.getExtras();
        Object[] pdus = (Object[]) input.get("pdus");

        for(int i=0;i<pdus.length;i++){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String sender = smsMessage.getDisplayOriginatingAddress();
            //You must check here if the sender is your provider and not another one with same text.

            String messageBody = smsMessage.getMessageBody();

            //Pass on the text to our listener.
            try{
                mListener.smsReceived(messageBody);
            }catch(Exception e){
                Log.d("SMS_RECEIVER", "onReceive: empty message receivedZ");
            }
        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
