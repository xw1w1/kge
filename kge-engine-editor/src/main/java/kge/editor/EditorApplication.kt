package kge.editor

import kge.api.input.GLFWKeyboard
import kge.api.input.GLFWMouse
import kge.api.std.IProjectDescriptor
import kge.api.std.IScene
import org.lwjgl.glfw.GLFW

class EditorApplication {
    private val window: EditorWindow = EditorWindow(
        1280, 720
    )

    private lateinit var mouse: GLFWMouse
    private lateinit var keyboard: GLFWKeyboard

    private var _currentProject: IProjectDescriptor? = null
    private var _currentScene: IScene? = null

    var scene: IScene
        get() = _currentScene ?: error("No active scene loaded")
        set(value) = openScene(value)

    val sceneNullable: IScene?
        get() = _currentScene

    var project: IProjectDescriptor?
        get() = _currentProject
        set(value) { _currentProject = value }

    fun run() {
        window.init()
        window.show()

        keyboard = GLFWKeyboard(window.handle)
        mouse = GLFWMouse(window.handle)
        editorUI = EditorUI(window)

        _instance = this

        mainLoop()

        dispose()
    }

    fun openProject(project: IProjectDescriptor) {
        _currentProject?.onProjectUnload()

        _currentProject = project

        window.title = "KGE Editor — ${project.name}"
        _currentProject?.onProjectLoad()

        editorUI.bindScene(scene)
    }

    private fun mainLoop() {
        var lastTime = GLFW.glfwGetTime()

        while (!window.shouldClose()) {
            val currentTime = GLFW.glfwGetTime()
            val delta = (currentTime - lastTime).toFloat()
            lastTime = currentTime

            window.pollEvents()
            window.clear()

            update(delta)
            render(delta)

            window.swapBuffers()
            mouse.endFrame()
        }
    }

    private fun update(delta: Float) {
        editorUI.update(delta)
    }

    private fun dispose() {
        editorUI.dispose()
        window.dispose()
    }

    companion object {
        private var _instance: EditorApplication? = null

        fun getInstance(): EditorApplication =
            _instance ?: error("EditorApplication has not been initialized.")
    }
}