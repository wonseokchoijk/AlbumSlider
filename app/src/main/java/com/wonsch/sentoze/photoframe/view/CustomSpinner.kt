package com.wonsch.sentoze.photoframe.view

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

/**
 * Spinner拡張カスタムクラス
 */
class CustomSpinner(context: Context?, attrs: AttributeSet?) : Spinner(context, attrs) {

    /** 初回呼び出し判定フラグ */
    private var isFirstStart: Boolean = true

    /** 選択ID */
    var selectedId: String = "0"

    /**
     * onItemSelectedListener Wrapper関数
     * 1. アプリ起動時に必ず1回実行されることにより選択していないインデックスが上書き保存されてしまうことを回避する為
     * 　　1回目の呼び出しは何もせずに無視する。
     * 2. ユーザーが選択したidをpreferenceに登録する。
     *
     * 3. 引数で受け取ったcallback関数を実行する。
     */
    fun setOnItemSelected(context: Context, preferenceSaveKey: String, callback: (selectedPosition:Int) -> Unit) {
        onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // アプリ開始時に必ず1回呼ばれるため、ユーザー選択保存データで初期化されないことを防止する為、
                // 必ず2回目以降実行時のみ動作
                if (isFirstStart) {
                    isFirstStart = false
                    setSelection(selectedId.toInt())
                } else {
                    // 選択インデクスを保存
                    selectedId = position.toString()

                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString(preferenceSaveKey, selectedId).apply()

                    callback(position)
                }
            }
        }
    }

    /**
     * preferenceに保存したインデックスをロードして内部の変数に格納
     */
    fun loadSelectedId(pref: SharedPreferences, preferenceSaveKey: String) {
        val id = pref.getString(preferenceSaveKey, null)

        if (id != null) {
            selectedId = id
        }
    }

    /**
     * リストから現在選択インデックスのオブジェクトを返す
     */
    fun <T> getSelectdElement(list: List<T>): T {
        try {
            return list[selectedId.toInt()]
        } catch (e: Exception) {
            this.selectedId = "0"

            return list[0]
        }
    }
}