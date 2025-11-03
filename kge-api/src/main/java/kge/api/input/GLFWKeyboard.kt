package kge.api.input

import org.lwjgl.glfw.GLFW

class GLFWKeyboard(windowHandle: Long) : IKeyboard {
    private val keys = BooleanArray(512)

    init {
        GLFW.glfwSetKeyCallback(windowHandle) { _, key, _, action, _ ->
            if (key in keys.indices)
                keys[key] = action != GLFW.GLFW_RELEASE
        }
    }

    override fun getKeys(): BooleanArray = keys

    override fun isDown(key: Int): Boolean =
        key in keys.indices && keys[key]

    override fun cleanup() {}
}
