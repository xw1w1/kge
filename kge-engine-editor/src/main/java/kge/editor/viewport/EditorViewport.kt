package kge.editor.viewport

import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiFocusedFlags
import imgui.flag.ImGuiHoveredFlags
import imgui.flag.ImGuiWindowFlags
import kge.api.editor.ViewportFramebuffer
import kge.api.editor.imgui.IRenderCallback
import kge.api.render.IPerspectiveViewCamera
import kge.api.std.IRenderable
import kge.editor.*
import kge.editor.component.MeshRenderer
import kge.editor.render.ShaderProgram
import kge.editor.render.ViewportAxisRenderer
import kge.editor.render.ViewportGridRenderer
import kge.editor.ui.EditorText
import kge.editor.ui.EditorUIPanel
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import kotlin.math.max
import kotlin.math.min

class EditorViewport : EditorUIPanel("Viewport"), IRenderable {
    // region Viewport
    private val editorCamera: EditorCamera? = EditorCamera()
    private val viewportFramebuffer = ViewportFramebuffer()
    private val viewportGizmoManager = ViewportGizmoManager()
    private val viewportRaycastManager = Raycast()
    // endregion

    private val viewportAxisRenderer: ViewportAxisRenderer = ViewportAxisRenderer()
    private val viewportGridRenderer: ViewportGridRenderer = ViewportGridRenderer()

    private lateinit var viewportSceneShader: ShaderProgram

    private var isActiveThisFrame: Boolean = false

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

    fun init() {
        viewportSceneShader = ResourceLoader.loadShader(
            "std/shaders/default.vert",
            "std/shaders/default.frag"
        )

        viewportGizmoManager.init()
        viewportAxisRenderer.init()
        viewportGridRenderer.init()
    }

    fun getEditorCamera(): IPerspectiveViewCamera? {
        return editorCamera
    }

    // region Render
    override fun render(delta: Float) {
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
            editorCamera?.updateProjectionMatrix(aspect)
            editorCamera?.updateViewMatrix()

            handleMouse(width, height, hasFocus)
            renderScene(delta, width, height, hasFocus, aspect)

            ImGui.image(
                viewportFramebuffer.colorTex.toLong(),
                width.toFloat(),
                contentHeight.toFloat(),
                0f, 1f, 1f, 0f
            )

            drawSelectionRect()
            drawFrameCounter()

            ImGui.endChild()
        }
        this.endUI()

    }

    private fun renderScene(delta: Float, width: Int, height: Int, hasFocus: Boolean, aspect: Float) {
        val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
        if (scene == null || editorCamera == null) return renderNoActiveSceneOrCamera(editorCamera == null).also {
            isActiveThisFrame = false
        }

        isActiveThisFrame = true
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, viewportFramebuffer.buffer)
        GL11.glViewport(0, 0, width, height)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glClearColor(0.09f, 0.1f, 0.12f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val viewProj = editorCamera.getViewProjection(aspect)

        viewportGridRenderer.render(viewProj)
        viewportAxisRenderer.render(viewProj)

        GL11.glEnable(GL11.GL_DEPTH_TEST)

        viewportSceneShader.bind()
        val selection = EditorApplication.getInstance().getEditorSelection()
        val selList = selection.getSelectedObjects()
        for (obj in scene.getAllObjects()) {
            val transform = obj.transform
            val renderer = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val model = transform.getWorldMatrix(obj.parent)
            viewportSceneShader.setUniformMat4("u_Model", model)
            viewportSceneShader.setUniformMat4("u_ViewProj", viewProj)
            viewportSceneShader.setUniform3f("u_Color", if (selList.contains(obj)) Vector3f(1f,1f,1f) else Vector3f(0.8f,0.4f,0.2f))

            renderer.render()
        }
        viewportSceneShader.unbind()

        val selected = selection.getSelectedObjects()
        if (selected.isNotEmpty()) {
            val center = Vector3f()
            for (obj in selected) {
                center.add(obj.transform.getWorldPosition(obj.parent))
            }
            center.div(selected.size.toFloat())
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_CULL_FACE)

            val suppressGizmoHighlight = !editorCamera.isRotating
            viewportGizmoManager.render(viewProj, center, editorCamera.position, suppressGizmoHighlight)
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

    private fun renderNoActiveSceneOrCamera(noCamera: Boolean) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, viewportFramebuffer.buffer)
        GL11.glViewport(0, 0, viewportFramebuffer.frameWidth, viewportFramebuffer.frameHeight)

        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glClearColor(0.12f, 0.12f, 0.12f, 1.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        val message = if (noCamera)
            "No active camera found"
        else
            "No active scene found"

        EditorText.info(message)
        isActiveThisFrame = false
    }

    private fun drawFrameCounter() {
        val drawList = ImGui.getWindowDrawList()
        if (lastUpdateTicks < 5) lastUpdateTicks++ else lastUpdateTicks = 0
        frameRate = 1f / max(1e-6f, ImGui.getIO().deltaTime).toDouble()
        val pos = ImGui.getWindowPos()

        drawList.addText(
            pos.x + 10f,
            pos.y + if (isActiveThisFrame) 10f else 30f,
            ImColor.rgba(255, 255, 255, 255),
            "FPS: %.0f".format(frameRate))
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
        val camera = editorCamera ?: return
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
            val renderer = obj.get<MeshRenderer>() ?: continue
            if (!obj.isActive) continue

            val boundsMin = renderer.mesh.boundsMin
            val boundsMax = renderer.mesh.boundsMax
            val model = obj.transform.getWorldMatrix(obj.parent)

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
    // endregion

    // region Input
    private fun handleMouse(width: Int, height: Int, hasFocus: Boolean) {
        if (!ImGui.isWindowHovered() || !hasFocus || getMouseInHeader() || editorCamera == null) return
        val childPos = ImGui.getCursorScreenPos()
        val mouseX = (ImGui.getMousePosX() - childPos.x).coerceIn(0f, viewportFramebuffer.frameWidth.toFloat())
        val mouseY = (ImGui.getMousePosY() - childPos.y).coerceIn(0f, viewportFramebuffer.frameHeight.toFloat())

        val winPos = ImGui.getWindowPos()
        val cursorPos = ImGui.getCursorPos()
        val relX = (mouseX - winPos.x - cursorPos.x).coerceIn(0f, width.toFloat())
        val relY = (mouseY - winPos.y - cursorPos.y).coerceIn(0f, height.toFloat())

        val (rayOrigin, rayDir) = viewportRaycastManager.getMouseRay(
            relX, relY, width, height,
            editorCamera.viewMatrix, editorCamera.projectionMatrix
        )

        val isLMBDown = (ImGui.isMouseDown(0) && !ImGui.isMouseDown(1))
        val isClicked = ImGui.isMouseClicked(0)
        val isReleased = ImGui.isMouseReleased(0)

        viewportGizmoManager.handleMouse(rayOrigin, rayDir, EditorApplication.getInstance().getEditorSelection().getSelectedObjects(), isLMBDown, isClicked, editorCamera.position)

        if (isClicked && !viewportGizmoManager.isDragging) {
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
                handleSingleClick(rayOrigin, rayDir)
            }
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
    // endregion

    override fun pushRenderCallback(cb: IRenderCallback) { }
}
