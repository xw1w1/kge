package com.craftware.editor.controls

import org.lwjgl.glfw.GLFW

class Keyboard(handle: Long) {
    private var keys = BooleanArray(1024)

    init {
        GLFW.glfwSetKeyCallback(handle) { _, key, _, action, _ ->
            if (key in keys.indices) {
                keys[key] = action != GLFW.GLFW_RELEASE
            }
        }
    }

    fun getKeys() = keys

    fun isDown(key: Int): Boolean {
        return (this.isKey(key))
    }

    private fun isKey(key: Int) = key in keys.indices && keys[key]
}