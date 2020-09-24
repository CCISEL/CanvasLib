package pt.isel.canvas

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import kotlin.js.Date
import kotlin.math.PI

private val all = mutableListOf<Canvas>()
private var closing = false

actual fun onStart(fx: () -> Unit) {
    window.onclose = {
        closing = true
        all.forEach { it.close() }
        all.clear()
    }
    window.onload = {
        fx()
    }
}

actual class Canvas actual constructor(
    actual val width: Int, actual val height: Int, actual val background: Int,
) {
    private val context: CanvasRenderingContext2D
    init {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = width
        canvas.height = height
        canvas.setAttribute("class", "canvas")
        document.body?.appendChild(canvas)
        context = canvas.getContext("2d") as CanvasRenderingContext2D
        all.add(this)
        context.font = "32px verdana"
        erase()
    }

    private fun Int.toRGB() : String {
        val str = toString(16)
        return "#"+"0".repeat(6-str.length)+str
    }
    actual fun erase() {
        drawRect(0,0,width,height,background)
    }
    actual fun drawCircle(xCenter: Int, yCenter: Int, radius: Int, color: Int, thickness: Int) {
        if (thickness==0) {
            context.fillStyle = color.toRGB()
            context.beginPath()
            context.arc(xCenter.toDouble(),yCenter.toDouble(),radius.toDouble(),0.0, 2* PI)
            context.fill()
        } else {
            context.strokeStyle = color.toRGB()
            context.lineWidth = thickness.toDouble()
            context.beginPath()
            context.arc(xCenter.toDouble(),yCenter.toDouble(),radius.toDouble(),0.0, 2* PI)
            context.stroke()
        }
    }
    actual fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int, thickness: Int) {
        if (thickness==0) {
            context.fillStyle = color.toRGB()
            context.fillRect(x.toDouble(),y.toDouble(), width.toDouble(),height.toDouble())
        } else {
            context.beginPath()
            context.lineWidth = thickness.toDouble()
            context.strokeStyle = color.toRGB()
            context.moveTo(x.toDouble(),y.toDouble())
            context.lineTo((x+width).toDouble(),y.toDouble())
            context.lineTo((x+width).toDouble(),(y+height).toDouble())
            context.lineTo(x.toDouble(),(y+height).toDouble())
            context.lineTo(x.toDouble(),y.toDouble())
            context.stroke()
        }
    }
    actual fun drawText(x: Int, y: Int, txt: String, color: Int, fontSize: Int?) {
        context.fillStyle = color.toRGB()
        if (fontSize!=null)
            context.font = "${fontSize}px verdana"
        context.textAlign = CanvasTextAlign.LEFT
        context.fillText(txt, x.toDouble(), y.toDouble(), 400.0)
    }
    actual fun drawLine(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int, color: Int, thickness: Int) {
        context.beginPath()
        context.lineWidth = thickness.toDouble()
        context.strokeStyle = color.toRGB()
        context.moveTo(xFrom.toDouble(),yFrom.toDouble())
        context.lineTo(xTo.toDouble(),yTo.toDouble())
        context.stroke()
    }

    actual fun onMouseDown(handler: (MouseEvent) -> Unit) {
        context.canvas.onmousedown = {
            handler(MouseEvent(it.offsetX.toInt(), it.offsetY.toInt(), true))
        }
    }

    actual fun onMouseMove(handler: (MouseEvent) -> Unit) {
        context.canvas.onmousemove = {
            handler(MouseEvent(it.offsetX.toInt(), it.offsetY.toInt(), it.buttons == 1.toShort()))
        }
    }

    actual fun onKeyPressed(handler: ((KeyEvent) -> Unit)?) {
        window.onkeypress = if (handler==null) null
        else { ke ->
            handler(KeyEvent(ke.charCode.toChar()))
        }
    }

    private val timers = mutableListOf<Int>()
    actual fun onTimeProgress(period: Int, handler: (Long) -> Unit) : TimerCtrl {
        val startTm = Date().getTime()
        val timer = window.setInterval( {
            val tm = Date().getTime()-startTm
            handler(tm.toLong())
        }, period )
        timers.add(timer)
        return TimerCtrl(timers, timer)
    }
    actual fun onTime(delay: Int, handler: () -> Unit) {
        window.setTimeout( { handler() }, delay )
    }

    actual fun close() {
        if (all.contains(this)) {
            timers.forEach { window.clearInterval(it) }
            if (!closing) {
                all.remove(this)
                context.canvas.remove()
                if (all.isEmpty()) window.close()
            }
        }
    }
}

actual class TimerCtrl(private val tms :MutableList<Int>, private val tm :Int) {
    actual fun stop() {
        window.clearInterval(tm)
        tms.remove(tm)
    }
}

private val sounds = mutableMapOf<String, Audio>()

actual fun playSound(sound: String) {
    val fileName = if (sound.lastIndexOf('.')>0) sound else "$sound.wav"
    val clip =
        if (sounds.contains(fileName)) sounds[fileName]
        else Audio(fileName)
    clip?.play()
}