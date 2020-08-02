package com.Techfreaks.Services

import android.content.Context
import android.util.Log
import android.widget.Toast
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
        }

        override fun onEndpointLost(p0: String) {
            TODO("Not yet implemented")
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

fun startAdvertising(context: Context, latitude : String, longitude: String): Unit {
    val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val endpointName = "$currentUser---$latitude---$longitude"
    val advertisingCallback = object : ConnectionLifecycleCallback(){
        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            TODO("Not yet implemented")
        }

        override fun onDisconnected(p0: String) {
            TODO("Not yet implemented")
        }

        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            TODO("Not yet implemented")
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