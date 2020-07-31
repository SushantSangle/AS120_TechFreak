package com.Techfreaks.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent

val NOTIF_CHANNEL_ID = "safeTriggerChannel"

fun updateSharedPrefs(mContext: Context, prefName: String, key: String, isChecked: Boolean) {
    val pref = mContext.getSharedPreferences(prefName, MODE_PRIVATE)
    val editor = pref.edit()
    editor.putInt(key, if (isChecked) 1 else 0)
    editor.apply()
}
fun updateContactList(mContext: Context,Contacts: Set<String>){
    val cPref = mContext.getSharedPreferences("safeTriggerSettings",MODE_PRIVATE)
    val cEditor = cPref.edit()
    cEditor.putStringSet("Contacts",Contacts)
    cEditor.apply()
}

fun getAllSettings(mContext: Context) :  MutableMap<String, *>? {
    val preferences = mContext.getSharedPreferences("safeTriggerSettings", MODE_PRIVATE)
    return preferences.all
}

fun getContactList(mContext: Context) : MutableSet<String>? {
    return mContext.getSharedPreferences("safeTriggerSettings", MODE_PRIVATE).getStringSet("Contacts",null)
}

fun initialSetupNotificationMode(mContext: Context){
    val `in` = Intent()
    `in`.action = "android.settings.CHANNEL_NOTIFICATION_SETTINGS"
    `in`.putExtra("android.provider.extra.CHANNEL_ID", NOTIF_CHANNEL_ID)
    `in`.putExtra("android.provider.extra.APP_PACKAGE", mContext.packageName)
    mContext.startActivity(`in`)
}
