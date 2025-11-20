package kge.ui.toolkit.dragndrop

import imgui.ImGui
import imgui.ImVec2
import kotlin.reflect.KClass

object EditorDragManager {
    private const val DRAG_THRESHOLD = 4f

    private var startX = 0f
    private var startY = 0f
    private var mousePos = ImVec2()

    private var pending: DragDropPendingCandidate? = null
    private var active: DragDropPayload<*>? = null
    private var dragActive: Boolean = false

    fun <T : Any> beginDragCandidate(type: KClass<T>, data: T, label: String) {
        if (dragActive || pending != null) return

        val min = ImGui.getItemRectMin()
        val max = ImGui.getItemRectMax()
        val mp = ImGui.getMousePos()
        startX = mp.x
        startY = mp.y
        mousePos.set(mp.x, mp.y)

        pending = DragDropPendingCandidate(type, data as Any, label, ImVec2(min.x, min.y), ImVec2(max.x, max.y))
    }

    fun <T : Any> getPayload(): DragDropPayload<T>? {
        @Suppress("UNCHECKED_CAST")
        return active as? DragDropPayload<T>?
    }

    fun <T: Any> handleDrop(type: KClass<T>, targetMin: ImVec2, targetMax: ImVec2): T? {
        val p = active ?: return null
        if (!dragActive || !type.isInstance(p.data)) return null

        val mp = ImGui.getMousePos()
        val inside = mp.x in targetMin.x..targetMax.x && mp.y in targetMin.y..targetMax.y

        if (!ImGui.isMouseDown(0)) {
            return if (inside) {
                @Suppress("UNCHECKED_CAST")
                val result = p.data as T
                result
            } else {
                null
            }
        }
        return null
    }


    fun <T : Any> isHoveringValidTarget(type: KClass<T>, rectMin: ImVec2? = null, rectMax: ImVec2? = null): Boolean {
        val p = active ?: return false
        if (!dragActive || !type.isInstance(p.data)) return false

        val min = rectMin ?: ImGui.getItemRectMin()
        val max = rectMax ?: ImGui.getItemRectMax()
        val mp = ImGui.getMousePos()

        return mp.x in min.x..max.x && mp.y in min.y..max.y
    }

    fun processPendingAndRender() {
        val mp = ImGui.getMousePos()
        mousePos.set(mp.x, mp.y)

        if (!dragActive && pending != null) {
            val dx = mp.x - startX
            val dy = mp.y - startY
            if (dx * dx + dy * dy > DRAG_THRESHOLD * DRAG_THRESHOLD && ImGui.isMouseDown(0)) {
                val s = pending!!
                @Suppress("UNCHECKED_CAST")
                active = DragDropPayload(s.type as KClass<Any>, s.data, s.label)
                dragActive = true
                pending = null
            } else if (!ImGui.isMouseDown(0)) {
                pending = null
            }
        }

        renderPreview()
    }

    private fun renderPreview() {
        val p = active ?: return
        if (!dragActive) return
        if (!ImGui.isMouseDown(0)) {
            cancelAll()
            return
        }

        val dl = ImGui.getForegroundDrawList()
        val mp = mousePos
        val text = p.label
        val ts = ImGui.calcTextSize(text)
        val pad = 6f
        val x0 = mp.x + 10f
        val y0 = mp.y + 10f
        dl.addRectFilled(x0, y0, x0 + ts.x + pad, y0 + ts.y + pad, ImGui.getColorU32(0f, 0f, 0f, 0.6f), 4f)
        dl.addText(x0 + 3f, y0 + 3f, ImGui.getColorU32(1f, 1f, 1f, 1f), text)

        if (!ImGui.isMouseDown(0) && dragActive) {
            cancelAll()
        }
    }

    private fun cancelAll() {
        active = null
        dragActive = false
        pending = null
    }
}