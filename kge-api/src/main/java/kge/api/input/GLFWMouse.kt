package kge.api.input

import org.lwjgl.glfw.GLFW

class GLFWMouse(private val windowHandle: Long) : IMouse {
    private val buttons = BooleanArray(8)

    private var lastX = 0.0
    private var lastY = 0.0

    override var x = 0.0
        private set
    override var y = 0.0
        private set
    override var dx = 0.0
        private set
    override var dy = 0.0
        private set
    override var scroll = 0.0
        private set

    override var cursorDisabled: Boolean = false
        set(value) {
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR,
                if (value) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL)
            field = value
            dx = 0.0
            dy = 0.0
        }

    init {
        GLFW.glfwSetCursorPosCallback(windowHandle) { _, cx, cy ->
            dx = cx - x
            dy = cy - y
            lastX = x
            lastY = y
            x = cx
            y = cy
        }

        GLFW.glfwSetMouseButtonCallback(windowHandle) { _, button, action, _ ->
            if (button in buttons.indices)
                buttons[button] = action != GLFW.GLFW_RELEASE
        }

        GLFW.glfwSetScrollCallback(windowHandle) { _, _, offset ->
            scroll = offset
        }
    }

    override fun isDown(button: Int): Boolean =
        button in buttons.indices && buttons[button]

    override fun cleanup() {
        dx = 0.0
        dy = 0.0
        scroll = 0.0
    }
}