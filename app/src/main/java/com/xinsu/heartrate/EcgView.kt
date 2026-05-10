package com.xinsu.heartrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class EcgView @JvmOverloads constructor(

    context: Context,

    attrs: AttributeSet? = null

) : View(context, attrs) {

    private val linePaint = Paint().apply {

        color = Color.GREEN

        strokeWidth = 4f

        style = Paint.Style.STROKE

        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {

        color = Color.argb(40, 0, 255, 0)

        strokeWidth = 1f
    }

    private var phase = 0f

    private var heartRate = 72

    /**
     * 设置心率
     */
    fun setHeartRate(bpm: Int) {

        heartRate = bpm

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        val w = width.toFloat()

        val h = height.toFloat()

        // 背景
        canvas.drawColor(Color.BLACK)

        // 网格
        drawGrid(canvas, w, h)

        // ECG 波形
        drawWave(canvas, w, h)

        // 动画
        phase += heartRate * 0.015f

        postInvalidateDelayed()
    }

    /**
     * 绘制背景网格
     */
    private fun drawGrid(

        canvas: Canvas,

        w: Float,

        h: Float
    ) {

        val step = 40f

        var x = 0f

        while (x < w) {

            canvas.drawLine(

                x,
                0f,
                x,
                h,
                gridPaint
            )

            x += step
        }

        var y = 0f

        while (y < h) {

            canvas.drawLine(

                0f,
                y,
                w,
                y,
                gridPaint
            )

            y += step
        }
    }

    /**
     * 绘制 ECG 波形
     */
    private fun drawWave(

        canvas: Canvas,

        w: Float,

        h: Float
    ) {

        val centerY = h / 2

        var lastX = 0f

        var lastY = centerY

        val amplitude = 90f

        var x = 0f

        while (x < w) {

            val progress =
                (x / w) * 6.28f + phase

            var y = centerY

            // ECG 尖峰
            y -= (
                    sin(progress) *
                            amplitude *
                            0.15f
                    ).toFloat()

            if (progress % 6.28f in 2.8f..3.0f) {

                y -= amplitude
            }

            canvas.drawLine(

                lastX,
                lastY,
                x,
                y,
                linePaint
            )

            lastX = x

            lastY = y

            x += 6f
        }
    }
}
