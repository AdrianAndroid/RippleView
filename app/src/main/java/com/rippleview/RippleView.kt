package com.rippleview

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class RippleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val centerPaint: Paint = Paint() // 中心圆paint
    private var radius: Int = 100 // 中心圆半径
    private val spreadPaint: Paint = Paint() // 扩散圆paint
    private var centerX: Float = 0F // 圆心x
    private var centerY: Float = 0F // 圆心y
    private var distance: Int = 5 // 每次圆递增间距
    private var maxRadius: Int = 80 // 最大圆半径
    private var delayMilliseconds: Int = 30 // 扩散延迟间隔,越大扩散越慢
    private var spreadRadius: ArrayList<Int> = arrayListOf() // 扩散圆层级数,元素为扩散的距离
    private var alphas : ArrayList<Int> = arrayListOf() // 对应每层圆的透明度

    init {
        initAttr(context, attrs, defStyleAttr)
    }

    private fun initAttr(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView, defStyleAttr, 0)
            radius = typeArray.getInt(R.styleable.RippleView_radius, radius)
            maxRadius = typeArray.getInt(R.styleable.RippleView_max_radius, maxRadius)
            val centerColor = typeArray.getColor(R.styleable.RippleView_center_color, ContextCompat.getColor(context, android.R.color.holo_red_dark))
            val spreadColor = typeArray.getColor(R.styleable.RippleView_spread_color, ContextCompat.getColor(context, android.R.color.holo_red_light))
            distance = typeArray.getInt(R.styleable.RippleView_distance, distance)
            typeArray.recycle()

            // 中心圆
            centerPaint.color = centerColor
            centerPaint.isAntiAlias = true

            // 水波纹扩散
            spreadPaint.color = spreadColor
            spreadPaint.isAntiAlias = true
            spreadPaint.style = Paint.Style.STROKE
            spreadPaint.alpha = 255
            spreadRadius.add(0)
            alphas.add(255)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2F
        centerY = h / 2F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (i in spreadRadius.indices) {
            // 透明度
            var alpha = alphas[i]
            // 半径
            val width = spreadRadius[i]
            spreadPaint.alpha = alpha
            // 绘制扩散的圆
            canvas?.drawCircle(centerX, centerY, (radius + width).toFloat(), spreadPaint)
            if (alpha > 0 && width < 255) {
                // 递减
                alpha = if((alpha - distance) > 0) (alpha - distance) else 1
                alphas[i] = alpha
                // 递增
                spreadRadius[i] = width + distance
            }
        }
        if (spreadRadius[spreadRadius.size - 1] > maxRadius) {
            spreadRadius.remove(0)
            alphas.add(255)
        }
        if (spreadRadius.size > 8) {
            spreadRadius.remove(0)
            alphas.remove(0)
        }
        // 中间的圆
        canvas?.drawCircle(centerX, centerY, radius.toFloat(), centerPaint)
        // 延迟更新, 达到扩散视觉差效果
        postInvalidateDelayed(delayMilliseconds.toLong())
    }
}