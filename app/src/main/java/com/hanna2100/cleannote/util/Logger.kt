package com.hanna2100.cleannote.util

import android.util.Log
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