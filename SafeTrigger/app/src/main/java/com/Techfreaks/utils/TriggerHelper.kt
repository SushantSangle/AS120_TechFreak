package com.Techfreaks.utils

import android.content.Context
import android.util.Log

val TIMESTAMP_KEY = "time"
const val COUNT_KEY = "count"

fun registerTimestamp(mContext: Context, triggerPref: String) {
    val pref = mContext.getSharedPreferences(triggerPref, Context.MODE_PRIVATE)
    val editor = pref.edit()
    val time = System.currentTimeMillis()
    editor.putLong(TIMESTAMP_KEY, time)
    editor.commit()
}

fun getTimestamp(mContext: Context, triggerPref: String): Long {
    val pref = mContext.getSharedPreferences(triggerPref, Context.MODE_PRIVATE)
    return pref.getLong(TIMESTAMP_KEY, 0)
}

fun registerCurrentTap(mContext: Context, currentTap: Int, triggerPref: String) {
    val pref = mContext.getSharedPreferences(triggerPref, Context.MODE_PRIVATE)
    val editor = pref.edit()
    editor.putInt(COUNT_KEY, currentTap)
    editor.commit()
}

fun getCurrentTap(mContext: Context, triggerPref: String): Int {
    val pref = mContext.getSharedPreferences(triggerPref, Context.MODE_PRIVATE)
    return pref.getInt(COUNT_KEY, 1)
}

fun resetTapCount(mContext: Context, triggerPref: String) {
    registerCurrentTap(mContext, 1, triggerPref)
}

fun isValidTrigger(mContext: Context, nTapCount: Int): Boolean {
    val prefName = nTapCount.toString()
    val triggerN = System.currentTimeMillis()
    val triggerNMinusOne = getTimestamp(mContext, prefName)
    val difference = triggerN - triggerNMinusOne
    val tapCount = getCurrentTap(mContext, prefName)
    registerTimestamp(mContext, prefName)

    if (difference in 1..1000L) {
        if (tapCount == nTapCount) {
            resetTapCount(mContext, prefName)
            return true
        }
        registerCurrentTap(mContext, tapCount + 1, prefName)
        return false
    }
    resetTapCount(mContext, prefName)
    return false
}