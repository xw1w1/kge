package kge.editor.input

import kge.api.input.IMouse
import org.lwjgl.glfw.GLFW

class GLFWMouse(private val input: GLFWInputSystem) : IMouse {
    init {
        if (!input.registered()) {
            throw IllegalStateException("GLFWInputSystem is not initialized yet!")
        }
    }
    override var cursorDisabled: Boolean = false
        set(value) {
            GLFW.glfwSetInputMode(input.handle, GLFW.GLFW_CURSOR,
                if (value) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL)
            field = value
            input.cleanup()
        }

    override val x: Double = input.getMouseX()
    override val y: Double = input.getMouseY()
    override val dx: Double = input.getMouseDX()
    override val dy: Double = input.getMouseDY()
    override val scroll: Double = input.getMouseScroll()

    override fun isDown(button: Int): Boolean =
        button in input.getMouseButtons().indices && input.getMouseButtons()[button]
}