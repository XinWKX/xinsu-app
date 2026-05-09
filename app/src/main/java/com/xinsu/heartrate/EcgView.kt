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

    private val paint = Paint().apply {

        color = Color.GREEN

        strokeWidth = 5f

        style = Paint.Style.STROKE

        isAntiAlias = true
    }

    private var phase = 0f

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        val centerY = height / 2f

        val widthF = width.toFloat()

        val step = 8f

        var x = 0f

        while (x < widthF) {

            val normalized =
                (x + phase) / widthF

            val ecg =
                generateEcgWave(normalized)

            val y =
                centerY - ecg * 180f

            if (x > 0f) {

                val prevNorm =
                    (x - step + phase) / widthF

                val prevEcg =
                    generateEcgWave(prevNorm)

                val prevY =
                    centerY - prevEcg * 180f

                canvas.drawLine(

                    x - step,
                    prevY,

                    x,
                    y,

                    paint
                )
            }

            x += step
        }

        phase += 12f

        if (phase > widthF) {

            phase = 0f
        }

        postInvalidateDelayed(16)
    }

    private fun generateEcgWave(
        x: Float
    ): Float {

        val t = x % 1f

        return when {

            t < 0.05f ->
                0f

            t < 0.08f ->
                1.8f

            t < 0.12f ->
                -0.8f

            t < 0.16f ->
                0.4f

            else ->
                (
                        sin(t * 20f) * 0.05f
                        ).toFloat()
        }
    }
}
