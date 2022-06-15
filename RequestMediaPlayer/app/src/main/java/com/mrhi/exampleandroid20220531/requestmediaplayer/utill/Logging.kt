package com.mrhi.exampleandroid20220531.requestmediaplayer.utill

import android.util.Log

class Logging {
    companion object{
        fun d( str: String) { Log.d("SHA1028EN", str) }
        fun v( str: String) { Log.v("SHA1028EN", str) }
        fun e( str: String) { Log.e("SHA1028EN", str) }
    }
}