package com.wonsch.sentoze.photoframe.Consts

import android.provider.MediaStore
import android.view.WindowManager
import android.widget.ImageView
import com.daimajia.slider.library.SliderLayout

/**
 * Spinner選択項目定数クラス
 */
class SpinnerValueConst {

    companion object {
        /** 画面切り替え選択項目 */
        val selectFlipTypeList = listOf(
            SliderLayout.Transformer.Default,
            SliderLayout.Transformer.Accordion,
            SliderLayout.Transformer.Background2Foreground,
            SliderLayout.Transformer.CubeIn,
            SliderLayout.Transformer.DepthPage,
//        SliderLayout.Transformer.Fade,
            SliderLayout.Transformer.FlipHorizontal,
            SliderLayout.Transformer.FlipPage,
            SliderLayout.Transformer.Foreground2Background,
            SliderLayout.Transformer.RotateDown,
            SliderLayout.Transformer.RotateUp,
            SliderLayout.Transformer.Stack,
            SliderLayout.Transformer.Tablet,
            SliderLayout.Transformer.ZoomIn,
            SliderLayout.Transformer.ZoomOutSlide,
            SliderLayout.Transformer.ZoomOut
        )

        /** 画面レイアウト */
        val layoutList = listOf(
            ImageView.ScaleType.FIT_CENTER,
//            ImageView.ScaleType.FIT_START,
//            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.FIT_XY,
//            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP
//            ImageView.ScaleType.CENTER_INSIDE
        )

        /** 背景色 */
        val backgroundColor = listOf(
            android.R.color.white,
            android.R.color.black,
            android.R.color.holo_blue_dark,
            android.R.color.holo_green_dark,
            android.R.color.holo_red_dark
        )

        /** 表示順 */
        val displayOrder = listOf(
            "RANDOM()",
            MediaStore.MediaColumns.DATE_ADDED + " ASC",
            MediaStore.MediaColumns.DATE_ADDED + " DESC",
            MediaStore.MediaColumns.DISPLAY_NAME + " ASC",
            MediaStore.MediaColumns.DISPLAY_NAME + " DESC"
        )

        /** 表示ロック */
        val displayLock = listOf(
            DISPLAY_LOCK.KEEP_SCREEN_ON,
            DISPLAY_LOCK.ALLOW_LOCK_WHILE_SCREEN_ON,
            DISPLAY_LOCK.KEEP_SCREEN_ON_WHILE_CHARGING
        )
    }

    /**
     * 表示順enum
     */
    enum class DISPLAY_LOCK(val value: Int?) {
        KEEP_SCREEN_ON(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON),
        ALLOW_LOCK_WHILE_SCREEN_ON(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON),
        KEEP_SCREEN_ON_WHILE_CHARGING(null);
    }
}