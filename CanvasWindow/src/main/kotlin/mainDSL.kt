import pt.isel.canvas.*

fun main() {
    window {
        canvas(600,400, background = 0x00FFFF) {
            onMouseDown { mouse ->
                drawCircle(mouse.x, mouse.y, 10, color = RED, thickness = 5)
            }
            onMouseMove { mouse ->
                if (mouse.down) drawCircle(mouse.x, mouse.y, 1, BLUE, 1)
            }
            onKeyPressed { key ->
                when (key.char) {
                    ' ' -> erase()
                    'Q','q' -> close()
                }
            }
            val tmCtrl = onTimeProgress(1000) { tm ->
                drawRect(0, 0, 30, 25, color = 0xAAAAAA)
                drawText(5, 17, (tm / 1000).toString(), fontSize = 18)
            }
            drawRect(50, 50, width - 100, height - 100, thickness = 10)
            drawLine(0, 0, width, height)
            drawLine(0, height, width, 0)
            onTime(10500) { tmCtrl.stop(); erase(); }
        }
    }
}