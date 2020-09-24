package pt.isel.canvas

import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import javax.sound.sampled.*
import javax.swing.JFrame
import javax.swing.Timer

private val window = JFrame("CanvasJVM")
private val all = mutableListOf<Canvas>()
private var closing = false

actual fun onStart(fx: () -> Unit) {
    window.layout = FlowLayout(FlowLayout.CENTER)
    window.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            window.dispose()
            closing = true
            all.forEach { it.close() }
            all.clear()
        }
    })
    window.setLocation(0, 0)
    window.background = Color.LIGHT_GRAY
    fx()
}

actual class Canvas actual constructor(
    actual val width: Int, actual val height: Int, actual val background: Int
) {
    private val bkColor = Color(background)
    private val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val graph = img.createGraphics()
    private val area = object : java.awt.Canvas() {
        override fun paint(g: Graphics) { g.drawImage(img, 0, 0, null) }
        override fun update(g: Graphics) { paint(g) }
    }
    init {
        all.add(this)
        area.background = Color(background)
        area.preferredSize = Dimension(width, height)
        window.add(area)
        window.pack()
        window.isVisible = true
        area.requestFocus()
        graph.setRenderingHints(
                RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON))
        graph.setRenderingHints( RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )
        )
        graph.font = Font("verdana", Font.PLAIN, 32)
        erase()
    }

    actual fun erase() {
        graph.color=bkColor
        graph.fillRect(0, 0, width, height)
        area.repaint()
    }
    actual fun drawCircle(xCenter: Int, yCenter: Int, radius: Int, color: Int, thickness: Int) {
        graph.color= Color(color)
        val x = xCenter-radius
        val y = yCenter-radius
        val side = radius*2
        if (thickness==0)
            graph.fillOval(x, y, side, side)
        else {
            graph.stroke = BasicStroke(thickness.toFloat())
            graph.drawOval(x, y, side, side)
        }
        area.repaint()
    }
    actual fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int, thickness: Int) {
        graph.color= Color(color)
        if (thickness==0)
            graph.fillRect(x, y, width, height)
        else {
            graph.stroke = BasicStroke(thickness.toFloat())
            graph.drawRect(x, y, width, height)
        }
        area.repaint()
    }
    actual fun drawText(x: Int, y: Int, txt: String, color: Int, fontSize: Int?) {
        graph.color= Color(color)
        val font = graph.font
        if (fontSize!=null && font.size!=fontSize)
            graph.font = font.deriveFont(fontSize.toFloat())
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graph.drawString(txt, x, y)
        area.repaint()
    }
    actual fun drawLine(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int, color: Int, thickness: Int) {
        graph.color= Color(color)
        graph.stroke = BasicStroke(thickness.toFloat())
        graph.drawLine(xFrom, yFrom, xTo, yTo)
        area.repaint()
    }

    private var mouseDownHandler : MouseListener? = null
    actual fun onMouseDown(handler: (MouseEvent) -> Unit) {
        if (mouseDownHandler!=null) area.removeMouseListener(mouseDownHandler)
        mouseDownHandler = object : MouseAdapter() {
            override fun mousePressed(e: java.awt.event.MouseEvent) {
                handler(MouseEvent(e.x, e.y, true))
            }
        }
        area.addMouseListener(mouseDownHandler)
    }

    private var mouseMoveHandler : MouseMotionListener? = null
    actual fun onMouseMove(handler: (MouseEvent) -> Unit) {
        if (mouseMoveHandler!=null) area.removeMouseMotionListener(mouseMoveHandler)
        mouseMoveHandler = object : MouseMotionAdapter() {
            override fun mouseMoved(e: java.awt.event.MouseEvent) {
                handler(MouseEvent(e.x, e.y, false))
            }
            override fun mouseDragged(e: java.awt.event.MouseEvent) {
                handler(MouseEvent(e.x, e.y, true))
            }
        }
        area.addMouseMotionListener(mouseMoveHandler)
    }

    private var keyPressedHandler : KeyListener? = null
    actual fun onKeyPressed(handler: ((KeyEvent) -> Unit)?) {
        if (keyPressedHandler!=null) area.removeKeyListener(keyPressedHandler)
        if (handler!=null) {
            keyPressedHandler = object : KeyAdapter() {
                override fun keyPressed(e: java.awt.event.KeyEvent) {
                    handler(KeyEvent(e.keyChar))
                }
            }
            area.addKeyListener(keyPressedHandler)
        } else
            keyPressedHandler = null
    }

    private val timers = mutableListOf<Timer>()
    actual fun onTimeProgress(period: Int, handler: (Long) -> Unit) : TimerCtrl {
        val tm = System.currentTimeMillis()
        val timer = Timer(period) { handler(System.currentTimeMillis() - tm) }
        timers.add(timer)
        timer.start()
        return TimerCtrl(timers, timer)
    }
    actual fun onTime(delay: Int, handler: () -> Unit) {
        val timer = Timer(delay, null)
        timer.addActionListener { handler(); timer.stop(); timers.remove(timer) }
        timers.add(timer)
        timer.start()
    }

    actual fun close() {
        if (all.contains(this)) {
            timers.forEach { it.stop() }
            if (!closing) {
                all.remove(this)
                window.remove(area)
                if (all.isEmpty()) window.dispose()
            }
        }
    }
}

actual class TimerCtrl(private val tms: MutableList<Timer>, private val tm: Timer) {
    actual fun stop() {
        tm.stop()
        tms.remove(tm)
    }
}

private val sounds = mutableMapOf<String, Clip>()

actual fun playSound(sound: String) {
    val fileName = if (sound.lastIndexOf('.')>0) sound else "$sound.wav"
    val clip =
        if (sounds.contains(fileName)) sounds[fileName]
        else {
            val file = File(fileName).canonicalFile
            val audioInputStream = AudioSystem.getAudioInputStream(file)
            val clp = AudioSystem.getClip()
            clp.open(audioInputStream)
            sounds[fileName] = clp
            clp
        }
    if (clip!=null) {
        clip.stop()
        clip.framePosition = 0
        clip.start()
    }
}