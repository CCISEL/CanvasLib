package pt.isel.canvas

import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.*
import javax.sound.sampled.*
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.Timer

private val window = JFrame("CanvasJVM")
private val all = mutableListOf<Canvas>()
private var closing = false
private var onFinishFx: (()->Unit)? = null

/**
 * Defines the [fx] function to be called when creating the window.
 * This function will be called in the UI context, so it cannot block execution.
 * The [fx] function should create a [Canvas] and start it.
 */
actual fun onStart(fx: () -> Unit) {
    window.layout = FlowLayout(FlowLayout.CENTER)
    window.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
    //window.setLocation(0, 0)
    window.background = Color.LIGHT_GRAY
    SwingUtilities.invokeLater { fx() }
}

/**
 * Defines the [fx] function to be called when the window is closed.
 * This function will be called in the UI context, so it cannot block execution.
 * The window is closed when the last canvas is closed or when X button is clicked.
 * The X button of window has no activity if onFinish is not called
 */
actual fun onFinish(fx: () -> Unit) {
    window.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    onFinishFx = fx
    window.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            window.dispose()
            fx()
            closing = true
            all.forEach { it.close() }
            all.clear()
        }
    })
}

actual class Canvas
/**
 * Construct a canvas in the window created by onStart.
 * The canvas keeps the [width], [height] and [background] color defined.
 * These features are immutable properties of the canvas constructed
 */
actual constructor(
    actual val width: Int, actual val height: Int, actual val background: Int
) {
    private val bkColor = Color(background)
    private val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val graph = img.createGraphics()
    private val area = object : JComponent() {
        override fun paint(g: Graphics) { g.drawImage(img, 0, 0, null) }
        override fun update(g: Graphics) { paint(g) }
    }
    init {
        all.add(this)
        area.background = Color(background)
        area.preferredSize = Dimension(width, height)
        window.add(area)
        window.pack()
        if (window.defaultCloseOperation == JFrame.HIDE_ON_CLOSE) onStart {  }
        window.isVisible = true
        graph.setRenderingHints( RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON))
        graph.setStroke( BasicStroke(25.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND))
        graph.font = Font("verdana", Font.PLAIN, 32)
        erase()
    }

    /**
     * Erase the entire canvas area by drawing a solid rectangle with the background color
     */
    actual fun erase() {
        graph.color=bkColor
        graph.fillRect(0, 0, width, height)
        area.repaint()
    }

    /**
     * Draw a circle or a circumference centered on [xCenter] and [yCenter] with the [radius] indicated.
     * If the [thickness] is zero (by default), draw a solid circle with the background [color],
     * otherwise draw the circumference with these [thickness] and [color].
     */
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
    actual fun drawArc(xCenter: Int, yCenter: Int, radius: Int, startAng: Int, endAng: Int, color: Int, thickness: Int) {
        graph.color= Color(color)
        val x = xCenter-radius
        val y = yCenter-radius
        val side = radius*2
        val arc = endAng - startAng
        if (thickness==0)
            graph.fillArc(x, y, side, side, startAng, arc)
        else {
            graph.stroke = BasicStroke(thickness.toFloat())
            graph.drawArc(x, y, side, side, startAng, arc)
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
                    handler(KeyEvent(e.keyChar, e.keyCode, java.awt.event.KeyEvent.getKeyText(e.keyCode)))
                }
            }
            area.addKeyListener(keyPressedHandler)
            area.requestFocusInWindow()
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
                if (all.isEmpty()) {
                    window.dispose()
                    onFinishFx?.invoke()
                }
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

private val sounds = mutableMapOf<String, Clip?>()

private fun String.toSoundName() = if (lastIndexOf('.')>0) this else "$this.wav"

actual fun loadSounds(vararg names: String) {
    for (s in names) loadClip(s.toSoundName())
}

actual fun playSound(sound: String) {
    val fileName = sound.toSoundName()
    val clip =
        if (sounds.contains(fileName)) sounds[fileName]
        else loadClip(fileName)
    if (clip!=null) {
        clip.stop()
        clip.framePosition = 0
        clip.start()
    }
}

private fun loadClip(fileName: String) :Clip {
    val audio = AudioSystem.getAudioInputStream( BufferedInputStream(
        try {
            FileInputStream(fileName)
        } catch (ex: FileNotFoundException) {
            val input = Canvas::class.java.getResourceAsStream("/$fileName")
            if (input == null) {
                println("Cant open file $fileName in working directory or in resources.")
                sounds[fileName] = null;
                throw FileNotFoundException(fileName)
             }
            input
        }
    ))
    return AudioSystem.getClip().apply {
        open(audio)
        sounds[fileName] = this
    }
}
