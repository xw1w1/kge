package kge.editor

import kge.api.std.IGLWindow
import kge.api.std.KGEWindowFlags
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import kotlin.IntArray

class EditorWindow(private val width: Int, private val height: Int) : IGLWindow {
    override var windowHandle: Long = 0L
    override var flags: KGEWindowFlags = KGEWindowFlags.None
    override var title: String? = "KGE Editor"
        set(value) {
            field = value
            GLFW.glfwSetWindowTitle(windowHandle, field ?: "KGE Editor")
        }

    override fun init() {
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        windowHandle = GLFW.glfwCreateWindow(width, height, title ?: "KGE Editor", 0, 0)
        if (windowHandle == 0L) throw RuntimeException("Failed to create GLFW window")

        GLFW.glfwMakeContextCurrent(windowHandle)
        GLFW.glfwSwapInterval(0)

        GL.createCapabilities()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LESS)
    }

    fun show() {
        if (windowHandle == 0L) throw IllegalStateException("GLFW is not initialized!")

        GLFW.glfwShowWindow(windowHandle)
    }

    fun shouldClose(): Boolean = GLFW.glfwWindowShouldClose(windowHandle)
    fun pollEvents() = GLFW.glfwPollEvents()
    fun swapBuffers() = GLFW.glfwSwapBuffers(windowHandle)

    fun getHandle(): Long = windowHandle

    fun setVSync(boolean: Boolean) {
        GLFW.glfwSwapInterval(if (boolean) 1 else 0)
    }

    override fun getSize(): Vector2i {
        val w = IntArray(1)
        val h = IntArray(1)
        GLFW.glfwGetWindowSize(windowHandle, w, h)
        return Vector2i(w[0], h[0])
    }

    override fun dispose() {
        GLFW.glfwDestroyWindow(windowHandle)
        GLFW.glfwTerminate()
    }

    override fun clear() {
        GL11.glClearColor(0.1f, 0.12f, 0.14f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }
}