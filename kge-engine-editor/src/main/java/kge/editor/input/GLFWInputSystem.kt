package kge.editor.input

import kge.api.input.IInputSystem
import kge.editor.EditorApplication
import org.lwjgl.glfw.GLFW

class GLFWInputSystem : IInputSystem {
    var handle: Long = 0L
    private var registered: Boolean = false

    override fun register() {
        if (registered) return

        val handle = EditorApplication.getInstance().getWindowHandle()
        if (handle == 0L) {
            throw IllegalStateException("GL context is not set")
        }

        this.handle = handle

        // mouse
        GLFW.glfwSetCursorPosCallback(handle) { _, cx, cy ->
            mousePos[MOUSE_DX] = (cx - mousePos[MOUSE_X])
            mousePos[MOUSE_DY] = (cy - mousePos[MOUSE_Y])
            mousePos[MOUSE_X * 2] = mousePos[MOUSE_X]
            mousePos[MOUSE_Y * 2] = mousePos[MOUSE_Y]
            mousePos[MOUSE_X] = cx
            mousePos[MOUSE_Y] = cy
        }

        GLFW.glfwSetMouseButtonCallback(handle) { _, button, action, _ ->
            if (button in mouse.indices)
                mouse[button] = action != GLFW.GLFW_RELEASE
        }

        GLFW.glfwSetScrollCallback(handle) { _, _, offset ->
            mousePos[MOUSE_WHEEL] = offset
        }

        // keyboard
        GLFW.glfwSetKeyCallback(handle) { _, key, _, action, _ ->
            if (key in keyboard.indices)
                keyboard[key] = action != GLFW.GLFW_RELEASE
        }
        registered = true
    }

    fun registered(): Boolean = registered

    // region Mappings
    // маппинги кнопок. свободные слоты зарезервированы под прошлые значения

    // маппинг кнопок мыши
    // right  left  wheel
    // 0      1     2
    private val mouse: BooleanArray = BooleanArray(5)

    // маппинг позиций мыши
    //                                           x, y, scroll, dx, dy
    // 0      1      2           3       4       5  6  7       8   9
    val MOUSE_X: Int = 5
    val MOUSE_Y: Int = 6
    val MOUSE_WHEEL: Int = 7
    val MOUSE_DX: Int = 8
    val MOUSE_DY: Int = 9
    private val mousePos: DoubleArray = DoubleArray(24)

    // ну тут я хз даже, возможно разделить левую половину массива на prevStates а правую на current
    private val keyboard: BooleanArray = BooleanArray(1024)

    // endregion

    fun getMouseX(): Double {
        return mousePos[MOUSE_X]
    }

    fun getMouseY(): Double {
        return mousePos[MOUSE_Y]
    }

    fun getMouseScroll(): Double {
        return mousePos[MOUSE_WHEEL]
    }

    fun getMouseDX(): Double {
        return mousePos[MOUSE_DX]
    }

    fun getMouseDY(): Double {
        return mousePos[MOUSE_DY]
    }

    fun getMouseButtons(): BooleanArray {
        return mouse
    }

    fun getKeyboard(): BooleanArray {
        return keyboard
    }

    override fun cleanup() {
        mousePos[MOUSE_DX] = 0.0
        mousePos[MOUSE_DY] = 0.0
        mousePos[MOUSE_X] = 0.0
    }
}