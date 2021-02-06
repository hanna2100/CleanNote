package com.hanna2100.cleannote.util

import android.util.Log
import com.hanna2100.cleannote.util.Constants.DEBUG
import com.hanna2100.cleannote.util.Constants.TAG

var isUnitTest = false

fun printLogD(className: String?, message: String) {
    if(DEBUG && isUnitTest.not()) {
        Log.d(TAG, "$className: $message")
    } else if (DEBUG && isUnitTest) {
        println("$className: $message")
    }
}