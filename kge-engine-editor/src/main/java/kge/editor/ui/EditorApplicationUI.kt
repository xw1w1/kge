package kge.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorWindow
import kge.editor.ui.window.ConsoleOutputWindow
import kge.editor.ui.window.EditorCameraSettingsWindow
import kge.editor.ui.window.EditorProjectFilesWindow
import kge.editor.ui.window.ObjectInspectorWindow
import kge.editor.ui.window.SceneHierarchyWindow
import kge.ui.toolkit.dragndrop.EditorDragManager

class EditorApplicationUI : IRenderable {
    private val imGuiGl3 = ImGuiImplGl3()
    private val imGuiGlfw = ImGuiImplGlfw()
    private val panels: MutableList<EditorUIPanel> = mutableListOf()

    private val editorMenuBar = EditorMenuBar()
    private val editorDockspace = EditorDockspace()

    private val hierarchyPanel: SceneHierarchyWindow = SceneHierarchyWindow()
    private val inspectorPanel: ObjectInspectorWindow = ObjectInspectorWindow()
    private val consoleOutputPanel: ConsoleOutputWindow = ConsoleOutputWindow()
    private val editorCameraPanel: EditorCameraSettingsWindow = EditorCameraSettingsWindow()
    private val projectFilesPanel: EditorProjectFilesWindow = EditorProjectFilesWindow()

    fun createImGuiContext(window: EditorWindow) {
        ImGui.createContext()
        val io = ImGui.getIO()
        io.configFlags = io.configFlags or ImGuiConfigFlags.NavEnableKeyboard
        io.configFlags = io.configFlags or ImGuiConfigFlags.DockingEnable

        imGuiGlfw.init(window.getHandle(), true)
        imGuiGl3.init("#version 150")

        KgeEditorStyle()
    }

    fun newFrame() {
        imGuiGlfw.newFrame()
        imGuiGl3.newFrame()
        ImGui.newFrame()
    }

    fun attach(panel: EditorUIPanel) {
        this.panels += panel
    }

    fun getEditorDockspace(): EditorDockspace {
        return editorDockspace
    }

    fun getEditorMenuBar(): EditorMenuBar {
        return editorMenuBar
    }

    fun getHierarchyPanel(): SceneHierarchyWindow {
        return hierarchyPanel
    }

    fun getInspectorPanel(): ObjectInspectorWindow {
        return inspectorPanel
    }

    fun getConsoleOutputPanel(): ConsoleOutputWindow {
        return consoleOutputPanel
    }

    fun getEditorCameraPanel(): EditorCameraSettingsWindow {
        return editorCameraPanel
    }

    fun getEditorProjectFilesPanel(): EditorProjectFilesWindow {
        return projectFilesPanel
    }

    override fun render(delta: Float) {
        EditorDragManager.processPendingAndRender()

        ImGui.render()
        imGuiGl3.renderDrawData(ImGui.getDrawData())
    }


    override fun pushRenderCallback(cb: IRenderCallback) { /** no logging huh? **/ }
}