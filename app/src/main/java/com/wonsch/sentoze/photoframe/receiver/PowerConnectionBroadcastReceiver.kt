package com.wonsch.sentoze.photoframe.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wonsch.sentoze.photoframe.MainActivity

class PowerConnectionBroadcastReceiver: BroadcastReceiver() {

    var powerConnected: Boolean = false

    override fun onReceive(context: Context?, intent: Intent?) {
        var action = intent!!.action
        var mainActivity = context as MainActivity

        if (action == Intent.ACTION_POWER_CONNECTED) {
            powerConnected = true

        } else if (action == Intent.ACTION_POWER_DISCONNECTED ) {
            powerConnected = false

        } else {
            powerConnected = false
        }

        mainActivity.changeDisplayLockByChargeEvent()
    }

    fun isPowerConneted(): Boolean {
        return this.powerConnected
    }
}