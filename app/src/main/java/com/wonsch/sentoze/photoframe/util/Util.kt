package com.wonsch.sentoze.photoframe.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.support.v4.app.ActivityCompat

class Util {
    companion object {
        @JvmStatic
        fun requestReadExternalStorage(activity: Activity, requestCode: Int) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode
            )
        }

        @JvmStatic
        fun isBatteryCharging(context: Context): Boolean {

            var intentBattery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            var status = intentBattery!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

            var isCharging = false;
            if(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL){
                isCharging = true;
            }else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING || status == BatteryManager.BATTERY_STATUS_DISCHARGING){
                isCharging = false;
            }

            return isCharging;
        }
    }
}