package com.Techfreaks.Services

import android.content.Context
import android.content.Intent
import android.text.BoringLayout
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.firebase.auth.FirebaseAuth

const val TAG = "BluetoothHelper"

fun startDiscovery(context: Context){
    val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
    val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG,"Endpoint found : " + info.endpointName)
            val data = info.endpointName.split("---")
            Toast.makeText(context, "userID = ${data[0]} :: co-ordinates = ${data[1]},${data[2]}", Toast.LENGTH_LONG).show()
            val intent = Intent(context,mEventListener::class.java)
            intent.putExtra("chainSOS",true)
            intent.putExtra("chain",info.endpointName)
            context.startService(intent)

        }

        override fun onEndpointLost(p0: String) {
        }
    }
    Nearby.getConnectionsClient(context).startDiscovery(context.packageName, discoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                Log.d(TAG, "starting discovery")
            }
            .addOnFailureListener {
                Log.e(TAG, "exception: $it")
            }
}

fun stopDiscovery(context: Context){
    Nearby.getConnectionsClient(context).stopDiscovery()
}

fun startAdvertising(context: Context, latitude : String, longitude: String,owner:Boolean,privSOS:String?): Unit {
    val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    lateinit var endpointName : String
    if(owner)
        endpointName = "$currentUser---$latitude---$longitude"
    else {
        if(privSOS?.split("---")?.size!! <5)
            endpointName = "$privSOS---$currentUser"
        else{
            stopAdvertising(context)
            startDiscovery(context)
            return;
        }
    }
    val advertisingCallback = object : ConnectionLifecycleCallback(){
        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
        }

        override fun onDisconnected(p0: String) {
        }

        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
        }

    }
    Nearby.getConnectionsClient(context).startAdvertising(endpointName, context.packageName, advertisingCallback, advertisingOptions)
            .addOnSuccessListener {
                Log.i(TAG, "started advertising with ID $endpointName")
            }
            .addOnFailureListener {
                Log.e(TAG, "unable to start advertising")
            }
}

fun stopAdvertising(context: Context){
    Nearby.getConnectionsClient(context).stopAdvertising()
}

fun restartDiscovery(context: Context){
    stopDiscovery(context)
    startDiscovery(context)
}
fun restartAdvertising(context : Context,latitude:String,longitude:String,owner:Boolean,privSOS:String){
    stopAdvertising(context)
    startAdvertising(context,latitude,longitude,owner,null)
}