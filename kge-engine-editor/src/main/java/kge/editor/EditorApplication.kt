package kge.editor

import kge.api.input.GLFWKeyboard
import kge.api.input.GLFWMouse
import kge.api.std.IProjectDescriptor
import kge.editor.project.EditorProjectManager
import kge.editor.render.DefaultEditorRenderPipeline
import kge.editor.render.EditorApplicationUI
import kge.editor.render.viewport.EditorViewport
import org.lwjgl.glfw.GLFW

class EditorApplication {
    private val window: EditorWindow = EditorWindow(
        1280, 720
    )

    private val editorViewport: EditorViewport = EditorViewport()
    private val editorApplicationUI: EditorApplicationUI = EditorApplicationUI()
    private val editorRenderPipeline: DefaultEditorRenderPipeline = DefaultEditorRenderPipeline()
    private val editorSelectionManager: EditorSelection = EditorSelection()
    private val projectManager: EditorProjectManager = EditorProjectManager()

    private lateinit var mouse: GLFWMouse
    private lateinit var keyboard: GLFWKeyboard

    private var _currentProject: IProjectDescriptor? = null
    private var _delta: Float = 0f

    fun run() {
        window.init()
        window.show()
        editorApplicationUI.createImGuiContext(window)

        keyboard = GLFWKeyboard(window.handle)
        mouse = GLFWMouse(window.handle)

        _instance = this

        mainLoop()

        dispose()
    }

    fun openProject(project: IProjectDescriptor) {
        _currentProject?.onProjectUnload()
        _currentProject = project

        window.title = "KGE Editor — ${project.name}"
        _currentProject?.onProjectLoad()
    }

    fun getViewport() = editorViewport
    fun getRenderPipeline() = editorRenderPipeline
    fun getEditorSelection() = editorSelectionManager

    fun getProjectManager() = projectManager

    fun getMouse() = mouse
    fun getKeyboard() = keyboard

    fun setTitle(title: String) {
        window.title = "KGE Editor — $title "
    }

    private fun mainLoop() {
        var lastTime = GLFW.glfwGetTime()

        while (!window.shouldClose()) {
            val currentTime = GLFW.glfwGetTime()
            _delta = (currentTime - lastTime).toFloat()
            lastTime = currentTime

            window.pollEvents()
            window.clear()
            editorApplicationUI.newFrame()

            update()
            render(_delta)

            window.swapBuffers()
            mouse.cleanup()
        }
    }

    private fun render(delta: Float) {
        editorApplicationUI.render(delta)
    }

    fun getDelta() = this._delta

    private fun update() {
        editorApplicationUI.getEditorDockspace().beginUI()

        editorApplicationUI.getEditorDockspace().endUI()
    }

    private fun dispose() {
        window.dispose()
    }

    companion object {
        private var _instance: EditorApplication? = null

        fun getInstance(): EditorApplication =
            _instance ?: error("EditorApplication has not been initialized.")
    }
}