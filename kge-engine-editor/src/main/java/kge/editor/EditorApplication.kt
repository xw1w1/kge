package kge.editor

import imgui.ImGui
import kge.api.editor.imgui.IRenderCallback
import kge.editor.input.GLFWInputSystem
import kge.editor.input.GLFWKeyboard
import kge.editor.input.GLFWMouse
import kge.editor.project.EditorProjectManager
import kge.editor.render.DefaultEditorRenderPipeline
import kge.editor.ui.EditorApplicationUI
import kge.editor.viewport.EditorViewport
import kge.editor.ui.EditorFont
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

    private lateinit var inputSystem: GLFWInputSystem
    private lateinit var mouse: GLFWMouse
    private lateinit var keyboard: GLFWKeyboard
    private var _delta: Float = 0f

    fun run() {
        window.init()
        window.show()
        inputSystem = GLFWInputSystem()
        _instance = this

        inputSystem.register()

        keyboard = GLFWKeyboard(inputSystem)
        mouse = GLFWMouse(inputSystem)

        editorApplicationUI.createImGuiContext(window)
        EditorFont.load(ImGui.getIO())

        editorApplicationUI.attach(editorViewport)
        editorViewport.init()

        mainLoop()

        dispose()
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

    fun getWindowHandle() = window.windowHandle

    fun appendConsole(string: String) {
        this.editorApplicationUI.getConsoleOutputPanel().log(string)
    }

    fun appendConsole(callback: IRenderCallback) {
        this.editorApplicationUI.getConsoleOutputPanel().pushRenderCallback(callback)
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

            val dockspace = editorApplicationUI.getEditorDockspace()
            dockspace.beginUI()
            editorApplicationUI.getEditorMenuBar().render(_delta)
            editorApplicationUI.getHierarchyPanel().render(_delta)
            editorApplicationUI.getInspectorPanel().render(_delta)
            editorApplicationUI.getConsoleOutputPanel().render(_delta)
            editorApplicationUI.getEditorCameraPanel().render(_delta)
            editorViewport.render(_delta)
            dockspace.endUI()

            editorApplicationUI.render(_delta)

            window.swapBuffers()
            inputSystem.cleanup()
        }
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