package kge.editor.ui.dragndrop

import imgui.ImGui
import imgui.ImVec2
import kotlin.reflect.KClass

object EditorDragDropField {
    fun <T : Any> objectField(label: String, value: T?, type: KClass<T>, onChange: (T?) -> Unit) {
        ImGui.text(label)
        ImGui.sameLine()

        val size = ImVec2(ImGui.getContentRegionAvailX(), ImGui.getFrameHeight())
        val fieldId = "##dragField_$label"

        ImGui.invisibleButton(fieldId, size.x, size.y)

        val dl = ImGui.getWindowDrawList()
        val min = ImGui.getItemRectMin()
        val max = ImGui.getItemRectMax()
        if (EditorDragManager.isHoveringValidTarget(type, min, max)) {
            dl.addRect(min.x, min.y, max.x, max.y, ImGui.getColorU32(0.3f, 0.7f, 1f, 1f), 3f)
        } else {
            dl.addRect(min.x, min.y, max.x, max.y, ImGui.getColorU32(0.45f, 0.45f, 0.45f, 1f), 2f)
        }

        val displayName = value?.toString() ?: "(None)"
        val ts = ImGui.calcTextSize(displayName)
        val tx = min.x + 6f
        val ty = min.y + (size.y - ts.y) / 2f
        dl.addText(tx, ty, ImGui.getColorU32(1f, 1f, 1f, 1f), displayName)

        val dropped = EditorDragManager.handleDrop(type, min, max)
        if (dropped != null) {
            onChange(dropped)
        }

        if (ImGui.isItemClicked(1)) {
            onChange(null)
        }
    }
}
