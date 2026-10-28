package com.aivora.app.data.call

import android.util.Log

/**
 * Single tag for the Wave 16 call module so logcat filters stay consistent:
 * `adb logcat -s AivoraCall`
 */
object CallLog {
    const val TAG = "AivoraCall"

    fun d(msg: String) = Log.d(TAG, msg)

    fun i(msg: String) = Log.i(TAG, msg)

    fun w(msg: String, t: Throwable? = null) {
        if (t != null) Log.w(TAG, msg, t) else Log.w(TAG, msg)
    }

    fun e(msg: String, t: Throwable? = null) {
        if (t != null) Log.e(TAG, msg, t) else Log.e(TAG, msg)
    }
}
