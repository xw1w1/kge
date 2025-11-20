package kge.ui.toolkit

import imgui.ImFont
import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiCol
import org.joml.Vector4f

object EditorText {
    fun text(text: String, font: ImFont, color: Vector4f? = null, imColorV: ImVec4? = null) {
        ImGui.pushFont(font)
        if (color != null) {
            ImGui.pushStyleColor(ImGuiCol.Text, color.x, color.y, color.z, color.w)
        }
        ImGui.text(text)
        if (color != null) {
            ImGui.popStyleColor()
        }
        ImGui.popFont()
    }

    fun header(text: String) {
        ImGui.pushFont(EditorFont.medium)
        ImGui.text(text)
        ImGui.popFont()
    }

    fun label(text: String) {
        ImGui.pushFont(EditorFont.regular)
        ImGui.text(text)
        ImGui.popFont()
    }

    fun info(text: String) {
        ImGui.pushFont(EditorFont.regular)
        ImGui.textWrapped(text)
        ImGui.popFont()
    }

    fun colored(text: String, color: Vector4f) {
        ImGui.pushFont(EditorFont.regular)
        ImGui.pushStyleColor(ImGuiCol.Text, color.x, color.y, color.z, color.w)
        ImGui.text(text)
        ImGui.popStyleColor()
        ImGui.popFont()
    }

    fun bold(text: String) {
        ImGui.pushFont(EditorFont.bold)
        ImGui.text(text)
        ImGui.popFont()
    }

    internal fun clippedLabel(text: String, availableWidth: Float): String {
        val fullSize = ImGui.calcTextSize(text)
        if (fullSize.x <= availableWidth) return text

        val ellipsis = "..."
        val ellipsisWidth = ImGui.calcTextSize(ellipsis).x
        if (ellipsisWidth > availableWidth) return ""

        var lo = 0
        var hi = text.length
        var best = 0
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val candidate = text.take(mid) + ellipsis
            val w = ImGui.calcTextSize(candidate).x
            if (w <= availableWidth) {
                best = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }

        return if (best <= 0) ellipsis else text.take(best) + ellipsis
    }

    internal fun renderLabelClipped(label: String, labelPadding: Float = 6f) {
        val avail = ImGui.getContentRegionAvailX()
        val availableForLabel = avail - labelPadding

        val toDraw = if (availableForLabel > 0f) {
            clippedLabel(label, availableForLabel)
        } else {
            "..."
        }

        ImGui.text(toDraw)
    }

}