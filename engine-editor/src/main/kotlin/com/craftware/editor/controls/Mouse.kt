package com.craftware.editor.controls

import org.lwjgl.glfw.GLFW

class Mouse(private val handle: Long) {
    var mouseX = 0.0
        private set
    var mouseY = 0.0
        private set

    var mouseDX = 0.0
    var mouseDY = 0.0

    var scroll = 0.0

    private val mouseButtons = BooleanArray(8)

    var cursorDisabled: Boolean = false
        set(value) {
            val mode = if (value) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL
            GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, mode)
            field = value
            mouseDX = 0.0
            mouseDY = 0.0
        }

    init {
        GLFW.glfwSetCursorPosCallback(handle) { _: Long, x: Double, y: Double ->
            mouseDX = x - mouseX
            mouseDY = y - mouseY
            mouseX = x
            mouseY = y
        }

        GLFW.glfwSetMouseButtonCallback(handle) { _, button, action, _ ->
            if (button in mouseButtons.indices) {
                mouseButtons[button] = action != GLFW.GLFW_RELEASE
                GLFW.GLFW_MOUSE_BUTTON_RIGHT
            }
        }

        GLFW.glfwSetScrollCallback(handle) { _, _, offset ->
            scroll = offset
        }
    }

    fun getKey(key: Int): Boolean? {
        return mouseButtons.getOrNull(key)
    }

    fun endFrame() {
        scroll = 0.0
        mouseDX = 0.0
        mouseDY = 0.0
    }
}