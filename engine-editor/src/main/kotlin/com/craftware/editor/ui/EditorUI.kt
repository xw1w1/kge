package com.craftware.editor.ui

import com.craftware.editor.EditorApp
import com.craftware.editor.EditorCamera
import com.craftware.editor.Selection
import com.craftware.editor.Window
import com.craftware.editor.viewport.Viewport
import com.craftware.editor.ui.impl.*
import com.craftware.engine.ExceptionFactory
import com.craftware.editor.Scene
import com.craftware.editor.standard.GameObject
import imgui.ImGui
import imgui.flag.ImGuiDockNodeFlags
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import org.lwjgl.glfw.GLFW

class EditorUI(private val window: Window) {
    private var initialized = false
    private lateinit var viewport: Viewport
    private lateinit var selection: Selection
    private lateinit var hierarchyPanel: HierarchyPanel
    private lateinit var inspectorPanel: InspectorPanel
    private lateinit var projectFolderPanel: ProjectFolderPanel
    private lateinit var editorCameraObject: GameObject

    private val viewportSettings = ViewportSettings()

    fun init(scene: Scene) {
        selection = Selection()
        viewport = Viewport(scene, selection)
        hierarchyPanel = HierarchyPanel(scene, selection)
        inspectorPanel = InspectorPanel(selection)
        projectFolderPanel = ProjectFolderPanel()

        editorCameraObject = GameObject("Editor Camera").apply {
            components += EditorCamera.EditorCameraTransform(viewport.editorCamera)
        }

        initialized = true
    }

    fun render(delta: Float) {
        setupDockspace()
        renderMenuBar()
        if (initialized) {
            viewport.renderUI(delta)
            hierarchyPanel.render()
            inspectorPanel.render()
            viewportSettings.render()
            projectFolderPanel.render()
            handleEditorShortcuts()
        }
        ImGui.end()
    }

    private fun setupDockspace() {
        ImGui.setNextWindowPos(0f, 0f)
        val (w, h) = window.getSize()
        ImGui.setNextWindowSize(w.toFloat(), h.toFloat())

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)

        val flags = ImGuiWindowFlags.NoDocking or
                ImGuiWindowFlags.NoTitleBar or
                ImGuiWindowFlags.NoCollapse or
                ImGuiWindowFlags.NoResize or
                ImGuiWindowFlags.NoMove or
                ImGuiWindowFlags.NoBringToFrontOnFocus or
                ImGuiWindowFlags.NoNavFocus or
                ImGuiWindowFlags.MenuBar

        ImGui.begin("MainDockspace", ImBoolean(true), flags)
        ImGui.popStyleVar(3)

        val dockspaceId = ImGui.getID("MainDockspaceID")
        ImGui.dockSpace(dockspaceId, 0f, 0f, ImGuiDockNodeFlags.PassthruCentralNode)
    }

    private fun renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File"))  {
                if (ImGui.menuItem("New Scene")) {
                    EditorApp.getInstance().openScene(Scene("New Scene"))
                }
                if (ImGui.menuItem("Open Scene...")) {
                    ExceptionFactory.createInfoWindow("In development", "Sorry, this section is not ready yet.")
                }
                if (ImGui.menuItem("Save Scene")) {
                    ExceptionFactory.createInfoWindow("In development", "Sorry, this section is not ready yet.")
                }
                if (ImGui.menuItem("Exit")) {
                    GLFW.glfwSetWindowShouldClose(window.getHandle(), true)
                }
                ImGui.endMenu()
            }

            CreateMenu.render()
            ImGui.endMenuBar()
        }
    }

    private fun handleEditorShortcuts() {
        if (EditorApp.getInstance().getKeyboard().isDown(GLFW.GLFW_KEY_E)) {
            selection.selectEditorCamera(editorCameraObject)
        }
        if (EditorApp.getInstance().getKeyboard().isDown(GLFW.GLFW_KEY_DELETE)) {
            selection.getSelectedObjects().forEach { selected ->
                selected.parent?.removeChild(selected)
                selection.clear()
            }
        }
        if (EditorApp.getInstance().getKeyboard().isDown(GLFW.GLFW_KEY_ESCAPE)) {
            selection.clear()
        }
    }
}
