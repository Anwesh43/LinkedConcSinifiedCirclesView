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
    save()
    translate(w / 2, h / 2)
    for (j in 0..(parts - 1)) {
        val sfj : Float = sf.divideScale(j, parts)
        val deg : Float = finalDeg * sfj
        val currR : Float = gap * (j + 1)
        drawArc(RectF(-currR, -currR, currR, currR), finalDeg - deg, deg * 2, false, paint)
    }
    restore()
}

fun Canvas.drawCSCNode(i : Int, scale : Float, paint : Paint) {
    paint.color = colors[i]
    paint.style = Paint.Style.STROKE
    drawConcSinifiedCircles(scale, paint)
}

class ConcSinifiedCirclesView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN  -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CSCNode(var i : Int , val state : State = State()) {

        private var next : CSCNode? = null
        private var prev : CSCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = CSCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCSCNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb  : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CSCNode {
            var curr : CSCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ConcSinifiedCircle(var i : Int) {

        private var curr : CSCNode = CSCNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ConcSinifiedCirclesView) {

        private val animator : Animator = Animator(view)
        private val csc : ConcSinifiedCircle = ConcSinifiedCircle(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            csc.draw(canvas, paint)
            animator.animate {
                csc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            csc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ConcSinifiedCirclesView {
            val view : ConcSinifiedCirclesView = ConcSinifiedCirclesView(activity)
            activity.setContentView(view)
            return view
        }
    }
}