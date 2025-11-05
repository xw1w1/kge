package kge.editor.render.viewport

import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiFocusedFlags
import imgui.flag.ImGuiHoveredFlags
import kge.api.editor.ViewportFramebuffer
import kge.editor.EditorApplication
import kge.editor.EditorCamera
import kge.editor.GameObject
import kge.editor.Raycast
import kge.editor.ResourceLoader
import kge.editor.component.MeshRenderer
import kge.editor.project.EditorSceneImpl
import kge.editor.render.ShaderProgram
import kge.editor.ui.EditorUIPanel
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import kotlin.math.max
import kotlin.math.min

class EditorViewport() : EditorUIPanel("Viewport") {
    // region Viewport
    private val editorCamera = EditorCamera()
    private val viewportFramebuffer = ViewportFramebuffer()
    private val viewportGizmoManager = ViewportGizmoManager()
    private val viewportRaycastManager = Raycast(editorCamera)
    // endregion

    private var viewportSceneShader: ShaderProgram = ResourceLoader.loadShader(
        "std/shaders/default.vert",
        "std/shaders/default.frag"
    )

    private val viewportAxisRenderer: ViewportAxisRenderer = ViewportAxisRenderer()
    private val viewportGridRenderer: ViewportGridRenderer = ViewportGridRenderer()

    // region Selection
    private var isSelecting = false
    private val selectionStart = Vector2f()
    private val selectionEnd = Vector2f()
    private var potentialClick = false
    private val dragStartThreshold = 5f
    // endregion

    // region FPS
    private var frameRate = 0.0
    private var lastUpdateTicks = 0
    // endregion FPS

    init {
        viewportGizmoManager.init()
        content = { renderUI(this.getDelta()) }
        viewportAxisRenderer.init()
        viewportGridRenderer.init()
    }

    fun getEditorCamera(): EditorCamera {
        return editorCamera
    }

    private fun renderUI(delta: Float) {
        val width = ImGui.getContentRegionAvailX().toInt().coerceAtLeast(1)
        val height = ImGui.getContentRegionAvailY().toInt().coerceAtLeast(1)

        viewportFramebuffer.ensureFramebuffer(width, height)

        val focused = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
        val hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows)
        val hasFocus = focused || hovered

        handleMouse(width, height, hasFocus)
        renderScene(delta, width, height, hasFocus)

        ImGui.image(viewportFramebuffer.colorTex.toLong(), width.toFloat(), height.toFloat(), 0f, 1f, 1f, 0f)

        drawSelectionRect()
        drawFPS()
    }

    private fun drawFPS() {
        val drawList = ImGui.getWindowDrawList()
        if (lastUpdateTicks < 5) lastUpdateTicks++ else lastUpdateTicks = 0
        frameRate = 1f / max(1e-6f, ImGui.getIO().deltaTime.toFloat()).toDouble()
        val pos = ImGui.getWindowPos()
        drawList.addText(pos.x + 10f, pos.y + 30f, ImColor.rgba(255, 255, 255, 255), "FPS: %.2f".format(frameRate))
    }

    private fun drawSelectionRect() {
        if (!isSelecting) return
        val drawList = ImGui.getWindowDrawList()
        drawList.addRect(selectionStart.x, selectionStart.y, selectionEnd.x, selectionEnd.y, ImColor.rgba(255, 255, 0, 255))
    }

    private fun handleMouse(width: Int, height: Int, hasFocus: Boolean) {
        if (!ImGui.isWindowHovered() || !hasFocus) return

        val mouseX = ImGui.getMousePosX()
        val mouseY = ImGui.getMousePosY()
        val winPos = ImGui.getWindowPos()
        val cursorPos = ImGui.getCursorPos()
        val relX = (mouseX - winPos.x - cursorPos.x).coerceIn(0f, width.toFloat())
        val relY = (mouseY - winPos.y - cursorPos.y).coerceIn(0f, height.toFloat())

        val (rayOrigin, rayDir) = viewportRaycastManager.getMouseRay(relX, relY, width, height)

        val isDown = ImGui.isMouseDown(0)
        val isClicked = ImGui.isMouseClicked(0)
        val isReleased = ImGui.isMouseReleased(0)

        viewportGizmoManager.handleMouse(rayOrigin, rayDir, EditorApplication.getInstance().getEditorSelection().getSelectedObjects(), isDown, isClicked, editorCamera.position)

        if (isClicked && !viewportGizmoManager.isDragging) {
            selectionStart.set(mouseX, mouseY)
            potentialClick = true
            isSelecting = false
        }
        if (isDown && potentialClick && !isSelecting) {
            val dx = mouseX - selectionStart.x
            val dy = mouseY - selectionStart.y
            if (dx * dx + dy * dy > dragStartThreshold * dragStartThreshold) {
                isSelecting = true
                potentialClick = false
            }
        }
        if (isSelecting) selectionEnd.set(mouseX, mouseY)

        if (isReleased) {
            if (isSelecting) performBoxSelection(width, height) else if (potentialClick) handleSingleClick(rayOrigin, rayDir)
            isSelecting = false
            potentialClick = false
        }
    }

    private fun handleSingleClick(rayOrigin: Vector3f, rayDir: Vector3f) {
        var closest: Pair<Float, GameObject>? = null
        val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return

        val selection = EditorApplication.getInstance().getEditorSelection()
        for (obj in scene.getAllObjects()) {
            val dist = viewportRaycastManager.intersectObject(rayOrigin, rayDir, obj)
            if (dist != null && (closest == null || dist < closest.first)) closest = dist to obj
        }
        closest?.second?.let { selection.select(it) } ?: selection.clearSelection()
    }

    private fun renderScene(delta: Float, width: Int, height: Int, hasFocus: Boolean) {
        val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, viewportFramebuffer.buffer)
        GL11.glViewport(0, 0, width, height)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glClearColor(0.09f, 0.1f, 0.12f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val aspect = width.toFloat() / max(1f, height.toFloat())
        editorCamera.updateProjectionMatrix(aspect)
        editorCamera.updateViewMatrix()
        val viewProj = editorCamera.getViewProjection(aspect)

        viewportGridRenderer.render(viewProj)
        viewportAxisRenderer.render(viewProj)

        viewportSceneShader.bind()
        val selection = EditorApplication.getInstance().getEditorSelection()
        val selList = selection.getSelectedObjects()
        for (obj in scene.getAllObjects()) {
            val transform = obj.transform
            val renderer = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val model = transform.getWorldMatrix(obj)
            viewportSceneShader.setUniformMat4("u_Model", model)
            viewportSceneShader.setUniformMat4("u_ViewProj", viewProj)
            viewportSceneShader.setUniform3f("u_Color", if (selList.contains(obj)) Vector3f(1f,1f,1f) else Vector3f(0.8f,0.4f,0.2f))

            renderer.render()
        }
        viewportSceneShader.unbind()

        val sel = selection.getSelectedObjects()
        if (sel.isNotEmpty()) {
            val gizmoPos = Vector3f()
            sel.forEach { it.transform.let { t -> gizmoPos.add(t.position) } }
            gizmoPos.div(sel.size.toFloat())
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_CULL_FACE)
            viewportGizmoManager.render(viewProj, gizmoPos, editorCamera.position)
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        if (hasFocus) editorCamera.handleInput(
            delta,
            EditorApplication.getInstance().getMouse(),
            EditorApplication.getInstance().getKeyboard()
        )
    }

    private fun performBoxSelection(width: Int, height: Int) {
        val minX = min(selectionStart.x, selectionEnd.x)
        val maxX = max(selectionStart.x, selectionEnd.x)
        val minY = min(selectionStart.y, selectionEnd.y)
        val maxY = max(selectionStart.y, selectionEnd.y)

        val selectedObjects = mutableListOf<GameObject>()
        val viewProj = editorCamera.getViewProjection(width.toFloat() / height)

        val scene: EditorSceneImpl = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return

        for (obj in scene.getAllObjects()) {
            val t = obj.transform
            val r = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val boundsMin = r.mesh.boundsMin
            val boundsMax = r.mesh.boundsMax
            val corners = arrayOf(
                Vector3f(boundsMin.x, boundsMin.y, boundsMin.z),
                Vector3f(boundsMin.x, boundsMin.y, boundsMax.z),
                Vector3f(boundsMin.x, boundsMax.y, boundsMin.z),
                Vector3f(boundsMin.x, boundsMax.y, boundsMax.z),
                Vector3f(boundsMax.x, boundsMin.y, boundsMin.z),
                Vector3f(boundsMax.x, boundsMin.y, boundsMax.z),
                Vector3f(boundsMax.x, boundsMax.y, boundsMin.z),
                Vector3f(boundsMax.x, boundsMax.y, boundsMax.z)
            )

            val model = t.getWorldMatrix(obj)
            var intersects = false
            for (corner in corners) {
                val worldPos = model.transformPosition(Vector3f(corner), Vector3f())
                val clip = Vector4f(worldPos, 1f)
                viewProj.transform(clip)
                if (clip.w != 0f) clip.div(clip.w)

                val screenX = (clip.x * 0.5f + 0.5f) * width
                val screenY = (1f - (clip.y * 0.5f + 0.5f)) * height
                if (screenX in minX..maxX && screenY in minY..maxY) {
                    intersects = true
                    break
                }
            }

            if (intersects) selectedObjects.add(obj)
        }
        val selection = EditorApplication.getInstance().getEditorSelection()
        if (selectedObjects.isNotEmpty()) selection.addSelection(*selectedObjects.toTypedArray()) else selection.clearSelection()
    }

    private fun getDelta(): Float {
        return EditorApplication.getInstance().getDelta()
    }
}
