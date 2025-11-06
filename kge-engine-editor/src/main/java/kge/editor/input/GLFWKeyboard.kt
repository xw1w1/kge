package kge.editor.input

import kge.api.input.IKeyboard
import org.lwjgl.glfw.GLFW

class GLFWKeyboard(private val input: GLFWInputSystem) : IKeyboard {
    init {
        if (!input.registered()) {
            throw IllegalStateException("GLFWInputSystem is not initialized yet!")
        }
    }
    override fun getKeys(): BooleanArray = input.getKeyboard()

    override fun isDown(key: Int): Boolean =
        key in getKeys().indices && getKeys()[key]
}
