package com.craftware.editor.ui

import com.craftware.editor.EditorApp
import com.craftware.editor.EditorCamera
import com.craftware.editor.Selection
import com.craftware.editor.Window
import com.craftware.editor.viewport.Viewport
import com.craftware.editor.ui.impl.*
import com.craftware.engine.ExceptionFactory
import com.craftware.editor.Scene
import com.craftware.editor.GameObject
import com.craftware.editor.Primitives
import com.craftware.editor.component.MeshRenderer
import com.craftware.engine.imgui.menu
import com.craftware.engine.imgui.menuItem
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
            components += EditorCamera.EditorCameraTransform(viewport.camera)
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
            if (ImGui.beginMenu("File")) {
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

            if (ImGui.beginMenu("Create")) {
                val scene = EditorApp.getInstance().sceneNullable
                if (scene == null) {
                    ExceptionFactory.createErrorWindow("Unable to access Create", "No active scene found")
                    return
                }
                if (ImGui.menuItem("GameObject")) scene.createEmpty("GameObject")
                if (ImGui.menuItem("Cube")) scene.createCube("Cube")
                if (ImGui.menuItem("Plane")) scene.create("Plane", scene) {
                    val renderer = MeshRenderer(Primitives.plane())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Sphere")) scene.create("Sphere", scene) {
                    val renderer = MeshRenderer(Primitives.sphere())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Cylinder")) scene.create("Cylinder", scene) {
                    val renderer = MeshRenderer(Primitives.cylinder())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Pyramid")) scene.create("Pyramid", scene) {
                    val renderer = MeshRenderer(Primitives.pyramid())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Octagon")) scene.create("Octagon", scene) {
                    val renderer = MeshRenderer(Primitives.octagon())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("DebugGrid")) scene.create("DebugGrid", scene) {
                    val renderer = MeshRenderer(Primitives.d_Grid())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("DebugAxis")) scene.create("DebugAxis", scene) {
                    val renderer = MeshRenderer(Primitives.d_Axis())
                    it.addComponent(renderer)
                }

                ImGui.endMenu()
            }
            ImGui.endMenuBar()
        }
    }

    private fun handleEditorShortcuts() {
        if (EditorApp.getInstance().getKeyboard().isDown(GLFW.GLFW_KEY_E)) {
            selection.selectEditorCamera(editorCameraObject)
        }
        if (EditorApp.getInstance().getKeyboard().isDown(GLFW.GLFW_KEY_DELETE)) {
            val selected = selection.selected
            selected?.parent?.removeChild(selected)
        }
        if (EditorApp.getInstance().getKeyboard().isDown(GLFW.GLFW_KEY_ESCAPE)) {
            selection.clear()
        }
    }
}
