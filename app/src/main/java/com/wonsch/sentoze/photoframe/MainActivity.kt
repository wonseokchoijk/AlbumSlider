package com.wonsch.sentoze.photoframe

import `in`.arjsna.filechooser.FileChooseHelperActivity
import `in`.arjsna.filechooser.FileLibUtils
import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.daimajia.slider.library.Indicators.PagerIndicator
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.DefaultSliderView
import com.daimajia.slider.library.Tricks.InfiniteViewPager
import com.daimajia.slider.library.Tricks.ViewPagerEx
import com.mxn.soul.flowingdrawer_core.FlowingDrawer
import com.wonsch.sentoze.photoframe.Consts.Consts
import com.wonsch.sentoze.photoframe.Consts.SpinnerValueConst
import com.wonsch.sentoze.photoframe.receiver.PowerConnectionBroadcastReceiver
import com.wonsch.sentoze.photoframe.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    /** 外部ストレージ読み取りリクエストコード */
    private val REQUEST_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1000

    /** ディレクトリー選択リクエストコード  */
    private val DIRECTORY_SELECT_REQUEST_CODE = 1

    /** 現在表示Bucket ID */
    private var bucketIdDisplaying: String? = null

    /** 現在アルバム代表画像ファイルパス */
    private var selectedRepresentImageFilePath: String? = null

    /** 現在選択アルバム名 */
    private var selectedBucketName: String? = null

    /** 充電状態取得用ブロードキャスト */
    private var powerConnectionBroadcastReceiver: BroadcastReceiver? = null

    /** 画面スライダー */
    private var mSlider: SliderLayout? = null

    private var time = 0
    private var timerTask: Timer? = null

    /**
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Preference保存値初期化用コード。テスト用
// PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit()

        // 充電状態取得用ブロードキャスト初期化・登録 ---------------------------------------------------------------
        powerConnectionBroadcastReceiver = PowerConnectionBroadcastReceiver()
        var intentFilterPwrConnected: IntentFilter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        intentFilterPwrConnected.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerConnectionBroadcastReceiver, intentFilterPwrConnected)
        // ----------------------------------------------------------------------------------------------------------

        // preference保存データを取得
        loadData()

        // 「アルバム選択」メニュークリック時イベント ---------------------------------------------------------------
        select_album.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_down_anim))
            val addPhotosIntent = Intent(this@MainActivity, FileChooseHelperActivity::class.java)
            addPhotosIntent.putExtra(FileLibUtils.FILE_TYPE_TO_CHOOSE, FileLibUtils.FILE_TYPE_IMAGES)
            startActivityForResult(addPhotosIntent, DIRECTORY_SELECT_REQUEST_CODE)
        }
        // ----------------------------------------------------------------------------------------------------------

        // 表示秒数選択イベント -------------------------------------------------------------------------------------
        select_slide_show_seconds_spinner.setOnItemSelected(this, Consts.SLIDE_SHOW_SEC_ID) { position ->
            mSlider?.let {
                var secondsArray = resources.getStringArray(R.array.select_slide_show_seconds_array)
                var durationSec = secondsArray[position].toLong() * 1000L
                it.setDuration(durationSec)
                it.startAutoCycle()
            }
        }
        // -----------------------------------------------------------------------------------------------------------

        // 切り替え効果選択イベント ----------------------------------------------------------------------------------
        select_flip_type.setOnItemSelected(this, Consts.FLIP_TYPE) { position ->
            mSlider?.let{
                it.setPresetTransformer(SpinnerValueConst.selectFlipTypeList[position])
                it.startAutoCycle()
            }
        }
        // -----------------------------------------------------------------------------------------------------------

        // 画面レイアウト選択イベント --------------------------------------------------------------------------------
        select_layout.setOnItemSelected(this, Consts.LAYOUT_TYPE) { position ->
            this.changeLayout(position)
        }
        // -----------------------------------------------------------------------------------------------------------

        // 背景色選択イベント ----------------------------------------------------------------------------------------
        select_background_color.setOnItemSelected(this, Consts.BACKGROUND_COLOR) { position ->
            this.changeBackgroundColor(position)
        }
        // -----------------------------------------------------------------------------------------------------------

        // 表示順イベント --------------------------------------------------------------------------------------------
        select_display_order.setOnItemSelected(this, Consts.DISPLAY_ORDER) { position ->
            this.changeDIsplayOrder(position)
        }
        // -----------------------------------------------------------------------------------------------------------

        // ロック設定イベント ----------------------------------------------------------------------------------------
        select_display_lock.setOnItemSelected(this, Consts.DISPLAY_LOCK) {position ->
            this.changeDisplayLock(position)
        }
        // -----------------------------------------------------------------------------------------------------------

        // ステータスバー表示イベント --------------------------------------------------------------------------------
        switch_show_status_bar.setOnCheckedChangeListenerAndSave(Consts.STATUS_BAR) { isChecked ->
            this.changeStatusBarDIsplay(isChecked)
        }
        // -----------------------------------------------------------------------------------------------------------

        // ユーザー権限確認・取得 ------------------------------------------------------------------------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 権限が許容されない場合
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // 既に権限が拒否されている場合
                alert(getString(R.string.authority_asking_message), getString(R.string.authority_asking_reason)) {
                    yesButton {
                        // 権限要請
                        Util.requestReadExternalStorage(this@MainActivity, REQUEST_READ_EXTERNAL_STORAGE_REQUEST_CODE)
                    }
                    noButton {}
                }.show()
            } else {
                // 権限要請
                Util.requestReadExternalStorage(this, REQUEST_READ_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        } else {
          getPhotos(this.bucketIdDisplaying, this.selectedRepresentImageFilePath, this.selectedBucketName, null)
        }
        // -----------------------------------------------------------------------------------------------------------
    }

    /**
     * onActivityResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // アルバム選択時
        if (resultCode == Activity.RESULT_OK && requestCode == DIRECTORY_SELECT_REQUEST_CODE) {
            this.bucketIdDisplaying = data!!.getStringExtra("selectedBucketId")
            this.selectedRepresentImageFilePath = data.getStringExtra("selectedRepresentImageFilePath")
            this.selectedBucketName = data.getStringExtra("selectedBuckedName")

            this.getPhotos(this.bucketIdDisplaying, this.selectedRepresentImageFilePath, this.selectedBucketName, null)
        }
    }

    /**
     * 画面レイアウト変更
     */
    private fun changeLayout(position: Int) {
        BaseSliderView.IMAGEVIEW_SCALE_TYPE = SpinnerValueConst.layoutList[position]

        mSlider?.let {

            var previousPositionImageView = it.realAdapter.getSliderView(it.currentPosition - 1).imageView
            var currentPositionImageView = it.realAdapter.getSliderView(it.currentPosition).imageView
            var nextPositionImageView = it.realAdapter.getSliderView(it.currentPosition + 1).imageView

            if (previousPositionImageView != null) {
                previousPositionImageView.scaleType = SpinnerValueConst.layoutList[position]
            }

            if (currentPositionImageView != null) {
                currentPositionImageView.scaleType = SpinnerValueConst.layoutList[position]
            }

            if (nextPositionImageView != null) {
                nextPositionImageView.scaleType = SpinnerValueConst.layoutList[position]
            }

            it.moveNextPosition()
        }
    }

    /**
     * 背景色変更
     */
    private fun changeBackgroundColor(position: Int) {

        BaseSliderView.BACKGROUND_COLOR = SpinnerValueConst.backgroundColor[position]

        mSlider?.let {

            var viewPager:InfiniteViewPager = it.viewPager
            viewPager.setBackgroundColor(ContextCompat.getColor(this, SpinnerValueConst.backgroundColor[position]))

            var previousPositionImageView = it.realAdapter.getSliderView(it.currentPosition - 1).imageView
            var currentPositionImageView = it.realAdapter.getSliderView(it.currentPosition).imageView
            var nextPositionImageView = it.realAdapter.getSliderView(it.currentPosition + 1).imageView

            if (previousPositionImageView != null) {
                previousPositionImageView.setBackgroundColor(ContextCompat.getColor(this, SpinnerValueConst.backgroundColor[position]))
            }

            if (currentPositionImageView != null) {
                currentPositionImageView.setBackgroundColor(ContextCompat.getColor(this, SpinnerValueConst.backgroundColor[position]))
            }

            if (nextPositionImageView != null) {
                nextPositionImageView.setBackgroundColor(ContextCompat.getColor(this, SpinnerValueConst.backgroundColor[position]))
            }

            it.moveNextPosition()
        }
    }

    /**
     * クラス外部から画面ロック設定変更実行(充電時)
     */
    fun changeDisplayLockByChargeEvent() {
        this.changeDisplayLock(select_display_lock.selectedId.toInt())
    }

    /**
     * 画面ロック設定変更
     */
    private fun changeDisplayLock(position: Int) {

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        val displayLockVal = SpinnerValueConst.displayLock[position]

        if (displayLockVal.value != null) {
            window.addFlags(displayLockVal.value)

        } else { // 充電中ロックなし選択時

            var powerReceiver = powerConnectionBroadcastReceiver as PowerConnectionBroadcastReceiver

            if (powerReceiver.isPowerConneted()) { // 充電中の場合
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else { // 充電中でない場合
                window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            }
        }
    }

    /**
     * ステータスバー表示切替
     */
    private fun changeStatusBarDIsplay(isChecked: Boolean) {
        if (! isChecked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 表示順変更
     */
    private fun changeDIsplayOrder(position: Int) {
        this.getPhotos(this.bucketIdDisplaying, this.selectedRepresentImageFilePath, this.selectedBucketName, position)
    }

    /**
     * onPause
     */
    override fun onPause() {
        super.onPause()
        mSlider?.let{
            it.stopAutoCycle()
        }
    }

    /**
     * onResume
     */
    override fun onResume() {
        super.onResume()
        mSlider?.let{
            it.startAutoCycle()
        }
    }

    /**
     * onDestroy
     */
    override fun onDestroy() {

        if (powerConnectionBroadcastReceiver != null) {
            unregisterReceiver(powerConnectionBroadcastReceiver)
            powerConnectionBroadcastReceiver = null
        }

        super.onDestroy()
    }

    /**
     * アプリ起動時Preference登録データロード
     */
    private fun loadData() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val bucketId = pref.getString(Consts.BUCKET_ID, null)
        val representImage = pref.getString(Consts.REPRESENT_IMAGE, null)
        val representName = pref.getString(Consts.REPRESENT_NAME, null)

        select_slide_show_seconds_spinner.loadSelectedId(pref, Consts.SLIDE_SHOW_SEC_ID)
        select_flip_type.loadSelectedId(pref, Consts.FLIP_TYPE)

        select_layout.loadSelectedId(pref, Consts.LAYOUT_TYPE)
        this.changeLayout(select_layout.selectedId.toInt())

        select_background_color.loadSelectedId(pref, Consts.BACKGROUND_COLOR)
        this.changeBackgroundColor(select_background_color.selectedId.toInt())

        select_display_order.loadSelectedId(pref, Consts.DISPLAY_ORDER)

        select_display_lock.loadSelectedId(pref, Consts.DISPLAY_LOCK)
        (powerConnectionBroadcastReceiver as PowerConnectionBroadcastReceiver).powerConnected = Util.isBatteryCharging(this)
        this.changeDisplayLock(select_display_lock.selectedId.toInt())

        switch_show_status_bar.loadSwitchVal(pref, Consts.STATUS_BAR)

        this.changeStatusBarDIsplay(switch_show_status_bar.isChecked)

        if (bucketId != null) {
            this.bucketIdDisplaying = bucketId
        }

        if (representImage != null) {
            this.selectedRepresentImageFilePath = representImage
        }

        if (representName != null) {
            this.selectedBucketName = representName
        }
    }

    // 権限リクエスト結果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.getPhotos(this.bucketIdDisplaying, this.selectedRepresentImageFilePath, this.selectedBucketName, null)
                } else {
                    toast(getString(R.string.authority_denied))
                }
            }
        }
    }

    /**
     * 画面設定初期化
     */
    private fun initDisplay() {
        defaultMessageText.visibility = View.VISIBLE
        sliderFrameLayout.visibility = View.GONE

        represent_name.text = getString(R.string.no_represent_image)
        Glide.with(this).load("").into(selected_represent_image)

        this.bucketIdDisplaying = null
        this.selectedRepresentImageFilePath = null
        this.selectedBucketName = null

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()
        editor.putString(Consts.BUCKET_ID, null)
        editor.putString(Consts.REPRESENT_IMAGE, null)
        editor.putString(Consts.REPRESENT_NAME, null)

        editor.apply()
    }

    private fun getPhotos(bucketId: String?, representImageViewPath: String?, selectedBucketName: String?, position: Int?) {

        if (bucketId == null) {
            this.initDisplay()
            return
        }

        defaultMessageText.visibility = View.GONE
        sliderFrameLayout.visibility = View.VISIBLE

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, // Return all rows
            MediaStore.Images.Media.BUCKET_ID + " = '" + bucketId + "'",
            null,
            if (position != null) {
                SpinnerValueConst.displayOrder[position]
            } else {
                select_display_order.getSelectdElement(SpinnerValueConst.displayOrder)
            }
        )

        // slider.removeAllSliders()にバグがあり、プログラム終了後、多量の画像が存在するアルバムを再度呼び出すと
        // スクロールのパフォーマンスが重くなる為、それを回避する為、レイアウトを削除して再追加。
        sliderFrameLayout.removeAllViews()
        var slider = SliderLayout(this)
        var sliderLayoutParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        slider.layoutParams = sliderLayoutParam

        sliderFrameLayout.addView(slider)

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))

                var sliderView = DefaultSliderView(this)
                sliderView.description(uri)
                    .image(File(uri))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this)
                    .bundle(Bundle())

                slider.addSlider(sliderView)
            }

            slider.setDuration(resources.getStringArray(R.array.select_slide_show_seconds_array)[select_slide_show_seconds_spinner.selectedId.toInt()].toLong() * 1000L)
            slider.setPresetTransformer(select_flip_type.getSelectdElement(SpinnerValueConst.selectFlipTypeList))

            slider.indicatorVisibility = PagerIndicator.IndicatorVisibility.Invisible

            slider.addOnPageChangeListener(this)

            mSlider = slider
            cursor.close()
        } else {
            this.initDisplay()
            return
        }

        // サイドメニューを閉じる
        var mDrawer = findViewById<FlowingDrawer>(R.id.drawerlayout)
        mDrawer.closeMenu()

        if (representImageViewPath != null) {
            Glide.with(this).load(representImageViewPath).into(selected_represent_image)
        }

        if (selectedBucketName != null) {
            represent_name.text = selectedBucketName
        } else {
            represent_name.text = ""
        }

        // bucketId保存
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()
        editor.putString(Consts.BUCKET_ID, bucketId)
        editor.putString(Consts.REPRESENT_IMAGE, representImageViewPath)
        editor.putString(Consts.REPRESENT_NAME, selectedBucketName)
        editor.apply()
    }

    override fun onStop() {
        mSlider?.let {
            it.stopAutoCycle()
        }
        super.onStop()
    }
    override fun onSliderClick(slider: BaseSliderView?) {
//        Toast.makeText(this, slider?.let{it.bundle.get("extra").toString()}, Toast.LENGTH_SHORT).show()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
    // slider interface実装 end ---------------------------------------------------------------------

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }
}
