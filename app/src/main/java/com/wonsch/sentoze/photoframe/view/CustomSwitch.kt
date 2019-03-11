package com.wonsch.sentoze.photoframe.view

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.widget.Switch

/**
 * Switch拡張カスタムクラス
 */
class CustomSwitch(context: Context?, attrs: AttributeSet?) : Switch(context, attrs) {

    /**
     * preferenceに登録した値をロードする
     */
    fun loadSwitchVal(pref: SharedPreferences, preferenceSaveKey: String) {
        val value = pref.getString(preferenceSaveKey, null)

        if (value != null) {
            isChecked = value.toBoolean()
        }
    }

    /**
     * ユーザーが選択した値をpreferenceに登録してcallback関数を実行
     */
    fun setOnCheckedChangeListenerAndSave(preferenceSaveKey: String, callback: (isChecked: Boolean) -> Unit) {

        setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(preferenceSaveKey, isChecked.toString()).apply()
            Log.d("photoFrame", "saved:" + isChecked.toString())

            callback(isChecked)
        }
    }
}