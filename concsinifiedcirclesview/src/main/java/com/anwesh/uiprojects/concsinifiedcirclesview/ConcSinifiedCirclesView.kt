package com.anwesh.uiprojects.concsinifiedcirclesview

/**
 * Created by anweshmishra on 16/09/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#009688",
        "#FF5722",
        "#4CAF50",
        "#3F51B5"
).map({Color.parseColor(it)}).toTypedArray()
val parts : Int = 3
val scGap : Float = 0.02f
val sizeFactor : Float = 4.1f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val strokeFactor : Float = 90f
val finalDeg : Float = 180f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()


fun Canvas.drawConcSinifiedCircles(scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    val sf : Float = scale.sinify()
    val gap : Float = Math.min(w, h) / (parts * sizeFactor)
    for (j in 0..(parts - 1)) {
        val sfj : Float = sf.divideScale(j, parts)
        val deg : Float = finalDeg * sfj
        val currR : Float = gap * (j + 1)
        drawArc(RectF(-currR, -currR, currR, currR), finalDeg - deg, deg * 2, false, paint)
    }
}

fun Canvas.drawCSCNode(i : Int, scale : Float, paint : Paint) {
    paint.color = colors[i]
    paint.style = Paint.Style.STROKE
    drawConcSinifiedCircles(scale, paint)
}

class ConcSinifiedCirclesView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN  -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}