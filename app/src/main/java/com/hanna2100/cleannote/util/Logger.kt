package com.hanna2100.cleannote.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hanna2100.cleannote.util.Constants.DEBUG
import com.hanna2100.cleannote.util.Constants.TAG

var isUnitTest = false

fun <T> printLogD(clazz: Class<T>?, message: String) {
    if(DEBUG && isUnitTest.not()) {
        Log.d(TAG, "${clazz?.simpleName}: $message")
    } else if (DEBUG && isUnitTest) {
        println("${clazz?.simpleName}: $message")
    }
}

fun cLog(msg: String?) {
    msg?.let {
        if(!DEBUG) {
            FirebaseCrashlytics.getInstance().log(it)
        }
    }
}