package kge.editor.viewport

import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiFocusedFlags
import imgui.flag.ImGuiHoveredFlags
import imgui.flag.ImGuiWindowFlags
import kge.api.editor.ViewportFramebuffer
import kge.editor.EditorApplication
import kge.editor.core.GameObject
import kge.editor.Raycast
import kge.editor.component.MeshRenderer
import kge.editor.exclude
import kge.editor.render.RenderPipeline
import kge.editor.ui.EditorUIPanel
import kge.ui.toolkit.UIText
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import kotlin.math.max
import kotlin.math.min

class ViewportWindow : EditorUIPanel("Viewport") {
    private val editorCamera: ViewportCamera = ViewportCamera()

    private val viewportHotbar: ViewportHotbar = ViewportHotbar()

    private val viewportFramebuffer = ViewportFramebuffer()
    private val viewportRaycastManager = Raycast

    private val renderPipeline: RenderPipeline = RenderPipeline()

    private var isActiveThisFrame: Boolean = false

    private var isSelecting = false
    private val selectionStart = Vector2f()
    private val selectionEnd = Vector2f()
    private var potentialClick = false
    private val dragStartThreshold = 5f

    private var frameRate = 0.0
    private var lastUpdateTicks = 0

    fun init() {
        viewportHotbar.init()
        ViewportGizmoManager.init()

        renderPipeline.init()
    }

    fun getEditorCamera(): ViewportCamera {
        return editorCamera
    }

    fun render(delta: Float) {
        this.beginUI()
        content = {
            val contentWidth = ImGui.getContentRegionAvailX().toInt().coerceAtLeast(1)
            val contentHeight = ImGui.getContentRegionAvailY().toInt().coerceAtLeast(1)

            ImGui.beginChild(
                "ViewportImage",
                contentWidth.toFloat(),
                contentHeight.toFloat(),
                false,
                ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoScrollWithMouse
            )

            val width = ImGui.getContentRegionAvailX().toInt().coerceAtLeast(1)
            val height = ImGui.getContentRegionAvailY().toInt().coerceAtLeast(1)

            viewportFramebuffer.ensureFramebuffer(contentWidth, contentHeight)

            val focused = ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
            val hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows)
            val hasFocus = focused || hovered

            val aspect = width.toFloat() / max(1f, height.toFloat())

            editorCamera.updateProjectionMatrix(aspect)
            editorCamera.updateViewMatrix()

            handleMouse(hasFocus, delta)

            handleScene()

            ImGui.image(
                viewportFramebuffer.colorTex.toLong(),
                width.toFloat(),
                contentHeight.toFloat(),
                0f, 1f, 1f, 0f
            )

            viewportHotbar.render(isActiveThisFrame)

            drawSelectionRect()
            drawFrameCounter()

            ImGui.endChild()
        }
        this.endUI()
    }

    private fun handleScene() {
        val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
        if (scene == null || editorCamera.targetCamera == null) {
            renderNoActiveSceneOrCamera(editorCamera.targetCamera == null)
            isActiveThisFrame = false
            return
        }

        isActiveThisFrame = true
        val width = ImGui.getContentRegionAvailX().toInt().coerceAtLeast(1)
        val height = ImGui.getContentRegionAvailY().toInt().coerceAtLeast(1)

        viewportFramebuffer.ensureFramebuffer(width, height)
        renderPipeline.render(scene, editorCamera, viewportFramebuffer.buffer, width, height)
    }

    private fun renderNoActiveSceneOrCamera(noCamera: Boolean) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, viewportFramebuffer.buffer)
        GL11.glViewport(0, 0, viewportFramebuffer.frameWidth, viewportFramebuffer.frameHeight)

        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glClearColor(0.12f, 0.12f, 0.12f, 1.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        val message = if (noCamera) "No active camera found" else "No active scene found"
        UIText.info(message)
        isActiveThisFrame = false
    }

    private fun drawFrameCounter() {
        val drawList = ImGui.getWindowDrawList()
        if (lastUpdateTicks < 60) {
            lastUpdateTicks++
        } else {
            lastUpdateTicks = 0
            frameRate = 1f / max(1e-6f, ImGui.getIO().deltaTime).toDouble()
        }
        val pos = ImGui.getWindowPos()

        drawList.addText(
            pos.x + 10f,
            pos.y + if (isActiveThisFrame) 10f else 30f,
            ImColor.rgba(255, 255, 255, 255),
            "FPS: %.0f".format(frameRate)
        )
    }

    private fun drawSelectionRect() {
        if (!isSelecting) return

        val childPos = ImGui.getWindowPos()
        val drawList = ImGui.getWindowDrawList()

        val x0 = selectionStart.x + childPos.x
        val y0 = selectionStart.y + childPos.y
        val x1 = selectionEnd.x + childPos.x
        val y1 = selectionEnd.y + childPos.y

        drawList.addRect(x1, y1, x0, y0, ImColor.rgba(255, 255, 0, 255), 0f, 2f)
    }

    private fun performBoxSelection(width: Int, height: Int) {
        val camera = editorCamera.targetCamera ?: return
        val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return

        val minX = min(selectionStart.x, selectionEnd.x)
        val maxX = max(selectionStart.x, selectionEnd.x)
        val minY = min(selectionStart.y, selectionEnd.y)
        val maxY = max(selectionStart.y, selectionEnd.y)

        val viewProj = camera.getViewProjection(width.toFloat() / height)

        val selectedObjects = mutableListOf<GameObject>()
        val tmp = Vector3f()
        val clip = Vector4f()
        val screenCorners = Array(8) { Vector2f() }

        for (obj in scene.getAllObjects()) {
            val renderer = obj.getComponent<MeshRenderer>() ?: continue
            if (!obj.activeInHierarchy) continue

            val boundsMin = renderer.mesh.boundsMin
            val boundsMax = renderer.mesh.boundsMax
            val model = obj.transform.getWorldMatrix()

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

            var screenMinX = Float.POSITIVE_INFINITY
            var screenMinY = Float.POSITIVE_INFINITY
            var screenMaxX = Float.NEGATIVE_INFINITY
            var screenMaxY = Float.NEGATIVE_INFINITY

            for (i in corners.indices) {
                model.transformPosition(corners[i], tmp)
                clip.set(tmp, 1f)
                viewProj.transform(clip)
                if (clip.w != 0f) clip.div(clip.w)

                val screenX = (clip.x * 0.5f + 0.5f) * width
                val screenY = (1f - (clip.y * 0.5f + 0.5f)) * height

                screenCorners[i].set(screenX, screenY)

                screenMinX = min(screenMinX, screenX)
                screenMaxX = max(screenMaxX, screenX)
                screenMinY = min(screenMinY, screenY)
                screenMaxY = max(screenMaxY, screenY)
            }

            val overlaps = !(screenMaxX < minX || screenMinX > maxX || screenMaxY < minY || screenMinY > maxY)
            if (overlaps) selectedObjects.add(obj)
        }

        val selection = EditorApplication.getInstance().getEditorSelection()
        if (selectedObjects.isNotEmpty()) {
            selection.addSelection(*selectedObjects.toTypedArray())
        } else {
            selection.clearSelection()
        }
    }

    private fun handleMouse(hasFocus: Boolean, delta: Float) {
        if (!ImGui.isWindowHovered() || !hasFocus || getMouseInHeader() || editorCamera.targetCamera == null) return

        editorCamera.handleInput(
            delta,
            EditorApplication.getInstance().getMouse(),
            EditorApplication.getInstance().getKeyboard()
        )

        val childPos = ImGui.getCursorScreenPos()
        val mouseX = (ImGui.getMousePosX() - childPos.x).coerceIn(0f, viewportFramebuffer.frameWidth.toFloat())
        val mouseY = (ImGui.getMousePosY() - childPos.y).coerceIn(0f, viewportFramebuffer.frameHeight.toFloat())

        val (rayOrigin, rayDir) = viewportRaycastManager.getMouseRay(
            mouseX, mouseY,
            viewportFramebuffer.frameWidth, viewportFramebuffer.frameHeight,
            editorCamera.targetCamera!!.viewMatrix, editorCamera.targetCamera!!.projectionMatrix
        )

        val isLMBDown = (ImGui.isMouseDown(0) && !ImGui.isMouseDown(1))
        val isClicked = ImGui.isMouseClicked(0)
        val isReleased = ImGui.isMouseReleased(0)

        val selection = EditorApplication.getInstance().getEditorSelection().getSelectedObjects()
        ViewportGizmoManager.handleMouse(
            rayOrigin, rayDir,
            selection.exclude(editorCamera.targetCamera!!),
            isLMBDown, isClicked,
            editorCamera.targetCamera!!.transform.position
        )

        if (isClicked && !ViewportGizmoManager.isDragging) {
            selectionStart.set(mouseX, mouseY)
            potentialClick = true
            isSelecting = false
        }
        if (isLMBDown && potentialClick && !isSelecting) {
            val dx = mouseX - selectionStart.x
            val dy = mouseY - selectionStart.y
            if (dx * dx + dy * dy > dragStartThreshold * dragStartThreshold) {
                isSelecting = true
                potentialClick = false
            }
        }
        if (isSelecting) selectionEnd.set(mouseX, mouseY)

        if (isReleased) {
            if (isSelecting) {
                performBoxSelection(viewportFramebuffer.frameWidth, viewportFramebuffer.frameHeight)
            } else if (potentialClick) {
                handleMouseClick(mouseX, mouseY)
            }
            isSelecting = false
            potentialClick = false
        }
    }

    private fun handleMouseClick(mouseX: Float, mouseY: Float) {
        val camera = editorCamera.targetCamera ?: return
        val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return

        val (rayOrigin, rayDir) = viewportRaycastManager.getMouseRay(
            mouseX, mouseY,
            viewportFramebuffer.frameWidth,
            viewportFramebuffer.frameHeight,
            camera.viewMatrix,
            camera.projectionMatrix
        )

        var closest: Pair<Float, GameObject>? = null
        val selection = EditorApplication.getInstance().getEditorSelection()
        for (obj in scene.getAllObjects()) {
            val dist = viewportRaycastManager.intersectObject(rayOrigin, rayDir, obj)
            if (dist != null && (closest == null || dist < closest.first)) closest = dist to obj
        }
        closest?.second?.let { selection.select(it) } ?: selection.clearSelection()
    }
}
