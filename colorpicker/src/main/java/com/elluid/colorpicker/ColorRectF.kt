package com.elluid.colorpicker

import android.graphics.RectF

class ColorRectF constructor(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    val color: Int
    ) : RectF(left, top, right, bottom) {

    var highlight = false
}

