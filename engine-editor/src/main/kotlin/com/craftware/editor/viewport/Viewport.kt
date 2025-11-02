package com.craftware.editor.viewport

import com.craftware.editor.EditorCamera
import com.craftware.editor.ResourceLoader
import com.craftware.editor.Selection
import com.craftware.editor.standard.GameObject
import com.craftware.editor.Scene
import com.craftware.editor.component.MeshRenderer
import com.craftware.editor.component.Transform
import com.craftware.engine.render.ShaderProgram
import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiFocusedFlags
import imgui.flag.ImGuiHoveredFlags
import imgui.flag.ImGuiWindowFlags
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import kotlin.math.max
import kotlin.math.min

class Viewport(
    private val scene: Scene,
    private val selection: Selection
) {
    val editorCamera = EditorCamera()
    private val raycaster = Raycaster(editorCamera)
    private val gizmoManager = GizmoManager()
    private val sceneShader: ShaderProgram = ResourceLoader.loadShader(
        "standard/shaders/default.vert",
        "standard/shaders/default.frag"
    )

    private val framebuffer: ViewportFramebuffer = ViewportFramebuffer()

    private var isSelecting: Boolean = false
    private val selectionStart: Vector2f = Vector2f()
    private val selectionEnd: Vector2f = Vector2f()

    private val dragStartThreshold = 5f
    private var potentialClick = false

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

        ImGui.image(framebuffer.colorTex.toLong(), width.toFloat(), height.toFloat(), 0f, 1f, 1f, 0f)
        if (isSelecting) {
            val drawList = ImGui.getWindowDrawList()
            drawList.addRect(
                selectionStart.x, selectionStart.y,
                selectionEnd.x, selectionEnd.y,
                ImColor.rgba(255, 255, 0, 255)
            )
        }
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

        val (rayOrigin, rayDir) = raycaster.getMouseRay(relX, relY, width, height)

        val isDown = ImGui.isMouseDown(0)
        val isClicked = ImGui.isMouseClicked(0)
        val isReleased = ImGui.isMouseReleased(0)

        val selectedObjs = selection.getSelectedObjects().map { it as GameObject }

        gizmoManager.handleMouse(
            rayOrigin = rayOrigin,
            rayDir = rayDir,
            selectedObjects = selectedObjs,
            isDown = isDown,
            isClicked = isClicked,
            cameraPos = editorCamera.position
        )

        if (isClicked && !gizmoManager.isDragging) {
            selectionStart.set(ImGui.getMousePosX(), ImGui.getMousePosY())
            potentialClick = true
            isSelecting = false
        }

        if (isDown && potentialClick && !isSelecting) {
            val dx = ImGui.getMousePosX() - selectionStart.x
            val dy = ImGui.getMousePosY() - selectionStart.y
            if (dx * dx + dy * dy > dragStartThreshold * dragStartThreshold) {
                isSelecting = true
                potentialClick = false
            }
        }

        if (isSelecting) {
            selectionEnd.set(ImGui.getMousePosX(), ImGui.getMousePosY())
        }

        if (isReleased) {
            if (isSelecting) {
                performBoxSelection(width, height)
            } else if (potentialClick) {
                handleSingleClick(rayOrigin, rayDir)
            }
            isSelecting = false
            potentialClick = false
        }
    }

    private fun handleSingleClick(rayOrigin: Vector3f, rayDir: Vector3f) {
        var closest: Pair<Float, GameObject>? = null
        for (obj in scene.getAllObjects()) {
            val dist = raycaster.intersectObject(rayOrigin, rayDir, obj)
            if (dist != null && (closest == null || dist < closest.first)) {
                closest = dist to obj
            }
        }
        if (closest != null) {
            selection.select(closest.second)
        } else {
            selection.clear()
        }
    }

    private fun renderScene(delta: Float, width: Int, height: Int, hasFocus: Boolean) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer.buffer)
        GL11.glViewport(0, 0, width, height)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glClearColor(0.09f, 0.1f, 0.12f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val aspect = width.toFloat() / max(1f, height.toFloat())
        editorCamera.updateProjection(aspect)
        editorCamera.updateView()
        val viewProj = editorCamera.getViewProjection(aspect)

        ViewportGrid.render(viewProj)
        ViewportAxis.render(viewProj)

        val shader = sceneShader
        shader.bind()

        val selList = selection.getSelectedObjects()

        for (obj in scene.getAllObjects()) {
            val t = obj.get<Transform>() ?: continue
            val r = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val model = t.getWorldMatrix(obj)
            shader.setUniformMat4("u_Model", model)
            shader.setUniformMat4("u_ViewProj", viewProj)
            shader.setUniform3f(
                "u_Color",
                if (selList.contains(obj)) Vector3f(1f, 1f, 1f)
                else Vector3f(0.8f, 0.4f, 0.2f)
            )
            r.render()
        }

        shader.unbind()

        val sel = selection.getSelectedObjects().map { it as GameObject }
        if (sel.isNotEmpty()) {
            val gizmoPos = Vector3f()
            var count = 0
            for (obj in sel) {
                val t = obj.get<Transform>() ?: continue
                gizmoPos.add(t.position)
                count++
            }
            if (count > 0) {
                gizmoPos.div(count.toFloat())
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_CULL_FACE)
                gizmoManager.render(viewProj, gizmoPos, editorCamera.position)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
            }
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        if (hasFocus) {
            editorCamera.handleInput(delta)
        }
    }

    private fun performBoxSelection(width: Int, height: Int) {
        val minX = min(selectionStart.x, selectionEnd.x)
        val maxX = max(selectionStart.x, selectionEnd.x)
        val minY = min(selectionStart.y, selectionEnd.y)
        val maxY = max(selectionStart.y, selectionEnd.y)

        val selectedObjects = mutableListOf<GameObject>()

        for (obj in scene.getAllObjects()) {
            val transform = obj.get<Transform>() ?: continue
            val renderer = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val center = Vector3f(
                (renderer.mesh.boundsMin.x + renderer.mesh.boundsMax.x) / 2f,
                (renderer.mesh.boundsMin.y + renderer.mesh.boundsMax.y) / 2f,
                (renderer.mesh.boundsMin.z + renderer.mesh.boundsMax.z) / 2f
            )
            val worldPos = transform.getWorldMatrix(obj).transformPosition(center, Vector3f())
            val clip = Vector4f(worldPos, 1f)
            editorCamera.getViewProjection(width.toFloat() / height).transform(clip)
            if (clip.w != 0f) clip.div(clip.w)

            val screenX = (clip.x * 0.5f + 0.5f) * width
            val screenY = (1f - (clip.y * 0.5f + 0.5f)) * height

            if (screenX in minX..maxX && screenY in minY..maxY) {
                selectedObjects.add(obj)
            }
        }

        if (selectedObjects.isNotEmpty()) {
            selection.selectMultiple(selectedObjects)
        } else {
            selection.clear()
        }
    }
}
