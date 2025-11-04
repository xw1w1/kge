package kge.editor

import kge.api.std.IGLWindow
import kge.api.std.KGEWindowFlags
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import kotlin.IntArray

class EditorWindow(private val width: Int, private val height: Int) : IGLWindow {
    override var handle: Long = 0L
    override var flags: KGEWindowFlags = KGEWindowFlags.None
    override var title: String? = "KGE Editor"

    override fun init() {
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        handle = GLFW.glfwCreateWindow(width, height, title ?: "KGE Editor", 0, 0)
        if (handle == 0L) throw RuntimeException("Failed to create GLFW window")

        GLFW.glfwMakeContextCurrent(handle)
        GLFW.glfwSwapInterval(1)

        GL.createCapabilities()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LESS)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glCullFace(GL11.GL_BACK)
    }

    fun show() {
        if (handle == 0L) throw IllegalStateException("GLFW is not initialized!")

        GLFW.glfwShowWindow(handle)
    }

    fun shouldClose(): Boolean = GLFW.glfwWindowShouldClose(handle)
    fun pollEvents() = GLFW.glfwPollEvents()
    fun swapBuffers() = GLFW.glfwSwapBuffers(handle)

    fun getHandle(): Long = handle

    override fun getSize(): Vector2i {
        val w = IntArray(1)
        val h = IntArray(1)
        GLFW.glfwGetWindowSize(handle, w, h)
        return Vector2i(w[0], h[0])
    }

    override fun dispose() {
        GLFW.glfwDestroyWindow(handle)
        GLFW.glfwTerminate()
    }

    override fun clear() {
        GL11.glClearColor(0.1f, 0.12f, 0.14f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }
}