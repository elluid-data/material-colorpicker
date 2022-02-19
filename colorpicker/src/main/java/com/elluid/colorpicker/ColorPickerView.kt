package com.elluid.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

    enum class DrawingMode {
        VERTICAL,
        HORIZONTAL
    }

    fun interface OnColorSelectedListener {
        fun onColorSelected(selectedColor: Int)
    }

    private var paletteColors: Array<Int>
    private var boxPixelSize = 0f
    private var boxGap = 0f
    private var boxStroke = 0f
    private var availableWidth = 0
    private var availableHeight = 0
    private var boxStrokeColor = 0
    private var colorSelected = 0
    private var lastBoxSelected: ColorRectF? = null
    private val colorBoxes = mutableListOf<ColorRectF>()
    private val listeners = mutableListOf<OnColorSelectedListener>()

    init {
        isClickable = true
        paletteColors = getColors()
        attrs?.let {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ColorPickerView, 0, 0)
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
            boxStrokeColor = typedValue.data
            boxPixelSize = typedArray.getDimension(R.styleable.ColorPickerView_boxSize, 40f)
            boxGap = typedArray.getDimension(R.styleable.ColorPickerView_boxGap, 2f)
            boxStroke = typedArray.getDimension(R.styleable.ColorPickerView_boxStroke, 2f)
            calculateBoxSizes()
        }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var drawingMode: DrawingMode = DrawingMode.VERTICAL
        val desiredSizeVertical = (boxPixelSize * 20) + (boxGap * 20) + paddingTop + paddingBottom
        val desiredSizeHorizontal = (boxPixelSize * 10) + (boxGap * 10) + paddingLeft + paddingRight


        when(resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                when(MeasureSpec.getMode(heightMeasureSpec)) {
                    MeasureSpec.EXACTLY -> {
                        boxPixelSize = MeasureSpec.getSize(heightMeasureSpec) - boxGap * 20 / 20
                    }

                }

                availableHeight = resolveSizeAndState(desiredSizeVertical.toInt(), heightMeasureSpec, 1)
                availableWidth = resolveSizeAndState(desiredSizeHorizontal.toInt(), widthMeasureSpec, 1)

            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                when(MeasureSpec.getMode(heightMeasureSpec)) {
                    MeasureSpec.EXACTLY -> {
                        boxPixelSize = MeasureSpec.getSize(heightMeasureSpec) - boxGap * 10 / 10
                    }
                }
                availableHeight = resolveSizeAndState(desiredSizeHorizontal.toInt(), heightMeasureSpec, 1)
                availableWidth = resolveSizeAndState(desiredSizeVertical.toInt(), widthMeasureSpec, 1)
                drawingMode = DrawingMode.HORIZONTAL

            }
        }

        Log.d("COLORPICKER", "Height = $availableHeight \t Width = $availableWidth")
        setMeasuredDimension(availableWidth, availableHeight)
        if(availableHeight > 0 || availableWidth > 0 ) {
            initBoxSizes(drawingMode)
            createPalette(drawingMode)
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawPalette(canvas)
    }

    private fun calculateBoxSizes() {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels - getStatusBarHeight() - 100
        when(resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if(width < boxPixelSize * 10 + boxGap * 9) // Set max box size depending on screen, overriding XML.
                {
                    boxPixelSize = ((width - (boxGap * 9)) / 10)
                    if(height < (boxPixelSize * 22) + boxGap * 19) { // 20 rows need to fit.
                        boxPixelSize = ((height - (boxGap * 21)) / 21)
                    }
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if(height < (boxPixelSize * 10) + (boxGap * 10)) // Set max box size depending on screen, overriding XML.
                {
                    boxPixelSize = ((height - paddingTop - paddingBottom - (boxGap * 10)) - boxPixelSize * 10)
                }
            }
        }
    }

    private fun initBoxSizes(drawingMode: DrawingMode) {
        when(drawingMode) {
            DrawingMode.VERTICAL -> {
                if(availableWidth < boxPixelSize * 10) // Set max box size depending on screen, overriding XML.
                {
                    boxPixelSize = ((availableWidth - (boxGap * 9)) / 10)
                    if(availableHeight < (boxPixelSize * 20) + boxGap * 9) { // 20 rows need to fit.
                        boxPixelSize = ((availableHeight - (boxGap * 9) - getStatusBarHeight()) / 20)
                    }
                }
            }
            DrawingMode.HORIZONTAL -> {
                if(availableHeight < (boxPixelSize * 10) + (boxGap * 10)) // Set max box size depending on screen, overriding XML.
                {
                    boxPixelSize = ((availableHeight - (boxGap * 10)) / 10)
                }
            }
        }
    }

    private fun createPalette(drawingMode: DrawingMode) {
        colorBoxes.clear()
        when(drawingMode) {
            DrawingMode.VERTICAL -> createVerticalPalette()
            DrawingMode.HORIZONTAL -> createHorizontalPalette()
        }
    }

    private fun createVerticalPalette() {
        val startingPosition = (availableWidth - (boxPixelSize * 10) - (boxGap * 9 )) / 2
        var startX = startingPosition
        var startY = paddingTop.toFloat()
        var position = 0
        for(i in 0..paletteColors.size step 10) {
            for(j in 0 until 10) {
                if(position < paletteColors.size) {
                    colorBoxes.add(
                        ColorRectF(
                            startX,
                            startY,
                            startX + boxPixelSize,
                            startY + boxPixelSize,
                            paletteColors[position]
                        )
                    )
                    position++
                    startX += boxGap + boxPixelSize
                } else {
                    break
                }

            }
            startX = startingPosition
            startY += boxGap + boxPixelSize
        }
    }

    private fun createHorizontalPalette() {

        val startingPositionX = (availableWidth - (boxPixelSize * 20) - (boxGap * 19 )) / 2
        val startingPositionY = (availableHeight - (boxPixelSize * 10) - (boxGap * 10 )) / 2 + paddingTop.toFloat()
        var startX = startingPositionX
        var startY = startingPositionY
        var position = 0
        for(i in 0..paletteColors.size step 10) {
            for(j in 0 until 10) {
                if(position < paletteColors.size) {
                    colorBoxes.add(
                        ColorRectF(
                            startX,
                            startY,
                            startX + boxPixelSize,
                            startY + boxPixelSize,
                            paletteColors[position]
                        )
                    )
                    position++
                    startY += boxGap + boxPixelSize
                } else {
                    break
                }

            }
            startY = startingPositionY
            startX += boxGap + boxPixelSize
        }
    }

    private fun drawPalette(canvas: Canvas?) {
        val paint = Paint()
        for(box in colorBoxes)
        {
            paint.color = box.color
            canvas?.drawRect(box, paint)
            if(box.highlight)
            {
                val strokePaint = Paint()
                strokePaint.style = Paint.Style.STROKE
                strokePaint.color = boxStrokeColor
                strokePaint.strokeWidth = boxStroke
                canvas?.drawRect(box, strokePaint)
            }

        }
    }

    private fun getColors() : Array<Int>
    {
        return arrayOf(
                // Red
                -5138, -12846, -1074534, -1739917, -1092784, -769226, -1754827, -2937041, -3790808, -4776932,
                // Pink
                -203540, -476208, -749647, -1023342, -1294214, -1499549, -2614432, -4056997, -5434281, -7860657,
                // Purple
                -793099, -1982745, -3238952, -4560696, -5552196, -6543440, -7461718, -8708190, -9823334, -11922292,
                // Deep purple
                -1185802, -3029783, -5005861, -6982195, -8497214, -10011977, -10603087, -11457112, -12245088, -13558894,
                // Indigo
                -1512714, -3814679, -6313766, -8812853, -10720320, -12627531, -13022805, -13615201, -14142061, -15064194,
                // Blue
                -1838339, -4464901, -7288071, -10177034, -12409355, -14575885, -14776091, -15108398, -15374912, -15906911,
                // Light Blue
                -1968642, -4987396, -8268550, -11549705, -14043402, -16537100, -16540699, -16611119, -16615491, -16689253,
                // Cyan
                -2033670, -5051406, -8331542, -11677471, -14235942, -16728876, -16732991, -16738393, -16743537, -16752540,
                //Teal
                -2034959, -5054501, -8336444, -11684180, -14244198, -16738680, -16742021, -16746133, -16750244, -16757440,
                // Green
                -1509911, -3610935, -5908825, -8271996, -10044566, -11751600, -12345273, -13070788, -13730510, -14983648,
                // Light Green
                -919319, -2298424, -3808859, -5319295, -6501275, -7617718, -8604862, -9920712, -11171025, -13407970,
                // Lime
                -394265, -985917, -1642852, -2300043, -2825897, -3285959, -4142541, -5262293, -6382300, -8227049,
                // Yellow
                -537, -1596, -2659, -3722, -4520, -5317, -141259, -278483, -415707, -688361,
                // Amber
                -1823, -4941, -8062, -10929, -13784, -16121, -19712, -24576, -28928, -37120,
                // Orange
                -3104, -8014, -13184, -18611, -22746, -26624, -291840, -689152, -1086464, -1683200,
                // Deep orange
                -267801, -13124, -21615, -30107, -36797, -43230, -765666, -1684967, -2604267, -4246004,
                // Brown
                -1053719, -2634552, -4412764, -6190977, -7508381, -8825528, -9614271, -10665929, -11652050, -12703965,
                // Grey
                -328966, -657931, -1118482, -2039584, -4342339, -6381922, -9079435, -10395295, -12434878, -14606047,
                // Blue Grey
                -1249295, -3155748, -5194043, -7297874, -8875876, -10453621, -11243910, -12232092, -13154481, -14273992
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                findBox(x, y)
                invalidate()
                notify(colorSelected)
            }
        }
        return true
    }

    private fun findBox(x: Float, y: Float ) {
        for(box in colorBoxes) {
            if(x in box.left..box.right && y in box.top..box.bottom) {
                lastBoxSelected?.highlight = false
                box.highlight = true
                colorSelected = box.color
                lastBoxSelected = box
                return
            }

        }
    }

    fun addOnColorSelectedListener(listener: OnColorSelectedListener) {
        listeners.add(listener)
    }

    private fun notify(colorSelected: Int) {
        listeners.forEach {
            it.onColorSelected(colorSelected)
        }
    }

    private fun getStatusBarHeight(): Int {
        var statusBarHeight = 48
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }
}