package com.craftware.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiWindowFlags
import org.joml.Vector2f
import kotlin.math.max

open class UIPanel(val id: String, private val title: String) {
    val pos = Vector2f(0F)
    val size = Vector2f(300f, 200f)
    private val minSize = Vector2f(120f, 80f)

    private var prevPos = Vector2f()
    private var prevSize = Vector2f()
    private var dragging = false
    private var resizing = false
    private var resizeEdge: ResizeEdge? = null
    var onDrop: (() -> Unit)? = null
    var onResizeStart: (() -> Unit)? = null
    var onResizeEnd: (() -> Unit)? = null

    private var headerRightClickListener: (() -> Unit)? = null
    private var headerHoldStart = 0.0
    private var headerHeld = false
    private var attachedContextMenu: UIPanelContextMenu? = null

    init {
        UIPanelManager.register(this)
    }

    fun attachContextMenu(menu: UIPanelContextMenu) {
        attachedContextMenu = menu
    }

    fun render(content: () -> Unit) {
        ImGui.begin(title, ImGuiWindowFlags.None)

        val curX = ImGui.getWindowPosX()
        val curY = ImGui.getWindowPosY()
        val w = ImGui.getWindowWidth()
        val h = ImGui.getWindowHeight()
        val curPos = Vector2f(curX, curY)
        val curSize = Vector2f(w, h)

        val mouseDown = ImGui.isMouseDown(0)

        if (dragging && !mouseDown) {
            dragging = false
            onDrop?.invoke()
        }

        if (!resizing && curSize != prevSize && mouseDown) {
            resizing = true
            onResizeStart?.invoke()
            resizeEdge = detectResizeEdge()
        }
        if (resizing && !mouseDown) {
            resizing = false
            resizeEdge = null
            onResizeEnd?.invoke()
        }

        prevPos.set(curPos)
        prevSize.set(curSize)
        pos.set(curPos)
        size.set(max(minSize.x, curSize.x), max(minSize.y, curSize.y))

        UIPanelManager.register(this)

        content()

        checkHeaderContext()

        attachedContextMenu?.render()

        ImGui.end()
    }


    fun onHeaderRightClick(listener: () -> Unit) {
        headerRightClickListener = listener
    }

    private fun detectResizeEdge(): ResizeEdge? {
        val mx = ImGui.getMousePosX()
        val my = ImGui.getMousePosY()
        val left = pos.x
        val top = pos.y
        val right = pos.x + size.x
        val bottom = pos.y + size.y
        val border = 8f
        if (mx >= right - border && mx <= right + border) return ResizeEdge.RIGHT
        if (mx >= left - border && mx <= left + border) return ResizeEdge.LEFT
        if (my >= bottom - border && my <= bottom + border) return ResizeEdge.BOTTOM
        if (my >= top - border && my <= top + border) return ResizeEdge.TOP
        return null
    }

    private fun checkHeaderContext() {
        val mouseX = ImGui.getMousePosX()
        val mouseY = ImGui.getMousePosY()
        val winX = ImGui.getWindowPosX()
        val winY = ImGui.getWindowPosY()
        val winW = ImGui.getWindowWidth()
        val headerH = ImGui.getFrameHeight()

        val inHeader = mouseX in winX..(winX + winW) && mouseY in winY..(winY + headerH)

        if (inHeader) {
            if (ImGui.isMouseClicked(1)) {
                headerRightClickListener?.invoke()
                attachedContextMenu?.let { ImGui.openPopup(it.id) }
            }

            if (ImGui.isMouseDown(1)) {
                if (!headerHeld) {
                    headerHeld = true
                    headerHoldStart = ImGui.getTime()
                } else if (ImGui.getTime() - headerHoldStart > 0.3) {
                    attachedContextMenu?.let { ImGui.openPopup(it.id) }
                }
            } else {
                headerHeld = false
            }
        }
    }
}
