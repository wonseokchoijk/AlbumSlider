package com.wonsch.sentoze.photoframe.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * アルバム代表画像を表示するImageView
 * 横サイズと縦サイズの同じ正方形のImageViewを生成
 */
class RepresentImageView: ImageView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}