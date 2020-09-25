package pt.isel.canvas

import kotlin.math.PI

const val BLACK = 0x000000
const val WHITE = 0xFFFFFF
const val RED   = 0xFF0000
const val GREEN = 0x00FF00
const val BLUE  = 0x0000FF

expect fun onStart(fx: ()->Unit)

expect class Canvas(width: Int, height: Int, background: Int = WHITE) {
    val width: Int
    val height: Int
    val background: Int

    fun erase()
    fun drawCircle(xCenter: Int, yCenter: Int, radius: Int, color: Int = BLACK, thickness: Int = 0)
    fun drawArc(xCenter: Int, yCenter: Int, radius: Int, startAng: Int, endAng: Int = 360, color: Int = BLACK, thickness: Int = 0)
    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int = BLACK, thickness: Int = 0)
    fun drawText(x: Int, y: Int, txt: String, color: Int = BLACK, fontSize: Int? = null)
    fun drawLine(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int, color: Int = BLACK, thickness: Int =3)

    fun onMouseDown(handler: (MouseEvent) -> Unit)
    fun onMouseMove(handler: (MouseEvent) -> Unit)
    fun onKeyPressed(handler: ((KeyEvent) -> Unit)?)
    fun onTimeProgress(period: Int, handler: (Long) -> Unit) : TimerCtrl
    fun onTime(delay: Int, handler: () -> Unit)

    fun close()
}

fun erase(cv: Canvas) { cv.erase() }

data class KeyEvent(val char: Char)

data class MouseEvent(val x: Int, val y: Int, val down: Boolean = false)

expect class TimerCtrl {
    fun stop()
}

expect fun playSound( sound: String )

//----------------------- DSL -----------------------

class Window {
    fun canvas(width: Int, height: Int, background: Int= WHITE, init: (Canvas.()->Unit)? =null): Canvas {
        val cv = Canvas(width,height,background)
        if (init!=null) cv.init()
        return cv
    }
}

fun window( init: Window.() -> Unit ): Window {
    val win = Window()
    onStart {
        win.init()
    }
    return win
}