package com.craftware.editor

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallbackI
import org.lwjgl.glfw.GLFWCursorPosCallbackI
import org.lwjgl.glfw.GLFWScrollCallbackI
import org.lwjgl.glfw.GLFWMouseButtonCallbackI
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11

class Window(private val width: Int, private val height: Int) {
    private var windowHandle: Long = 0

    fun init() {
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        windowHandle = GLFW.glfwCreateWindow(width, height, "KGE", 0, 0)
        if (windowHandle == 0L) throw RuntimeException("Failed to create GLFW window")

        GLFW.glfwMakeContextCurrent(windowHandle)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(windowHandle)

        GL.createCapabilities()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }
    fun setTitle(str: String) {
        GLFW.glfwSetWindowTitle(windowHandle, "KDE - $str")
    }
    fun shouldClose(): Boolean = GLFW.glfwWindowShouldClose(windowHandle)
    fun pollEvents() = GLFW.glfwPollEvents()
    fun swapBuffers() = GLFW.glfwSwapBuffers(windowHandle)

    fun clear() {
        GL11.glClearColor(0.1f, 0.12f, 0.14f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun dispose() {
        GLFW.glfwDestroyWindow(windowHandle)
        GLFW.glfwTerminate()
    }

    fun getSize(): Pair<Int, Int> {
        val w = IntArray(1)
        val h = IntArray(1)
        GLFW.glfwGetWindowSize(windowHandle, w, h)
        return w[0] to h[0]
    }

    fun getHandle(): Long = windowHandle
}
