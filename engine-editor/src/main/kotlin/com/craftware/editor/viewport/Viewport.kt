package com.craftware.editor.viewport

import com.craftware.editor.EditorCamera
import com.craftware.editor.ResourceLoader
import com.craftware.editor.Selection
import com.craftware.editor.GameObject
import com.craftware.editor.Scene
import com.craftware.editor.component.MeshRenderer
import com.craftware.editor.component.Transform
import com.craftware.engine.render.ShaderProgram
import imgui.ImGui
import imgui.flag.ImGuiFocusedFlags
import imgui.flag.ImGuiHoveredFlags
import imgui.flag.ImGuiWindowFlags
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import kotlin.math.max

class Viewport(
    private val scene: Scene,
    private val selection: Selection
) {
    val camera = EditorCamera()
    private val raycast = Raycast(camera)
    private val gizmoManager = GizmoManager()
    private val sceneShader: ShaderProgram = ResourceLoader.loadShader(
        "standard/shaders/default.vert",
        "standard/shaders/default.frag"
    )

    private val framebuffer: ViewportFramebuffer = ViewportFramebuffer()

    init {
        gizmoManager.init()
        ViewportGrid.init()
        ViewportAxis.init()
    }

    fun renderUI(delta: Float) {
        ImGui.begin(
            "Scene Viewport",
            ImGuiWindowFlags.NoScrollbar or
                    ImGuiWindowFlags.NoScrollWithMouse or
                    ImGuiWindowFlags.NoMove
        )

        val width = ImGui.getContentRegionAvailX().toInt().coerceAtLeast(1)
        val height = ImGui.getContentRegionAvailY().toInt().coerceAtLeast(1)

        framebuffer.ensureFramebuffer(width, height)

        val focused = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
        val hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows)
        val hasFocus = focused || hovered

        handleMouse(width, height, hasFocus)
        renderScene(delta, width, height, hasFocus)

        ImGui.image(framebuffer.colorTex, width.toFloat(), height.toFloat(), 0f, 1f, 1f, 0f)
        ImGui.end()
    }

    private fun handleMouse(width: Int, height: Int, hasFocus: Boolean) {
        if (!ImGui.isWindowHovered()) return
        if (!hasFocus) return
        val mouse = ImGui.getMousePos()
        val win = ImGui.getWindowPos()
        val cur = ImGui.getCursorPos()

        val relX = (mouse.x - win.x - cur.x).coerceIn(0f, width.toFloat())
        val relY = (mouse.y - win.y - cur.y).coerceIn(0f, height.toFloat())

        val (rayOrigin, rayDir) = raycast.getMouseRay(relX, relY, width, height)

        val selected = selection.selected as? GameObject
        val isDown = ImGui.isMouseDown(0)
        val isClicked = ImGui.isMouseClicked(0)

        gizmoManager.handleMouse(
            rayOrigin = rayOrigin,
            rayDir = rayDir,
            obj = selected,
            isDown = isDown,
            isClicked = isClicked,
            camera.position
        )

        if (isClicked && !gizmoManager.isDragging) {
            var closest: Pair<Float, GameObject>? = null
            for (obj in scene.getAllObjects()) {
                val dist = raycast.intersectObject(rayOrigin, rayDir, obj)
                if (dist != null && (closest == null || dist < closest.first)) {
                    closest = dist to obj
                }
            }
            if (closest != null) selection.select(closest.second)
            else selection.clear()
        }
    }

    private fun renderScene(delta: Float, width: Int, height: Int, hasFocus: Boolean) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer.buffer)
        GL11.glViewport(0, 0, width, height)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glClearColor(0.09f, 0.1f, 0.12f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val aspect = width.toFloat() / max(1f, height.toFloat())
        camera.updateProjection(aspect)
        camera.updateView()
        val viewProj = camera.getViewProjection(aspect)

        ViewportGrid.render(viewProj)
        ViewportAxis.render(viewProj)

        val shader = sceneShader
        shader.bind()

        for (obj in scene.getAllObjects()) {
            val t = obj.get<Transform>() ?: continue
            val r = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val model = t.getWorldMatrix(obj)
            shader.setUniformMat4("u_Model", model)
            shader.setUniformMat4("u_ViewProj", viewProj)
            shader.setUniform3f(
                "u_Color",
                if (selection.selected === obj) Vector3f(1f, 1f, 1f)
                else Vector3f(0.8f, 0.4f, 0.2f)
            )
            r.render()
        }

        shader.unbind()
        selection.selected?.let { if (it is GameObject) gizmoManager.render(viewProj, it, camera.position) }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        if (hasFocus) {
            camera.handleInput(delta)
        }
    }
}
