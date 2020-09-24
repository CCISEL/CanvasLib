import pt.isel.canvas.*

fun main() {
    onStart {
        val cv = Canvas(600, 400, background = 0x00FFFF)
        cv.onMouseDown { mouse ->
            cv.drawCircle(mouse.x, mouse.y, 10, color = RED, thickness = 5)
            playSound("click")
        }
        cv.onMouseMove { mouse ->
            if (mouse.down) cv.drawCircle(mouse.x, mouse.y, 1, BLUE, 1)
        }
        cv.onKeyPressed { key ->
            when (key.char) {
                ' ' -> cv.erase()
                'Q','q' -> cv.close()
            }
        }
        val tmCtrl = cv.onTimeProgress(1000) { tm ->
            cv.drawRect(0, 0, 30, 25, color = 0xAAAAAA)
            cv.drawText(5, 17, (tm / 1000).toString(), fontSize = 18)
        }
        cv.drawRect(50, 50, cv.width - 100, cv.height - 100, thickness = 10)
        cv.drawLine(0, 0, cv.width, cv.height)
        cv.drawLine(0, cv.height, cv.width, 0)
        cv.onTime(10500) { tmCtrl.stop(); cv.erase() }
    }
}