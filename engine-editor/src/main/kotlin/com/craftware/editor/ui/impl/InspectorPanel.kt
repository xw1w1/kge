package com.craftware.editor.ui.impl

import com.craftware.editor.Selection
import com.craftware.editor.ui.UIPanel
import com.craftware.editor.GameObject
import com.craftware.editor.Node
import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiKey
import imgui.type.ImString

class InspectorPanel(private val selection: Selection) : UIPanel("Inspector", "Inspector") {
    private var renamingNode: Node? = null
    private val renameBuffer = ImString(256)

    fun render() = render {
        val selected = selection.selected
        if (selected == null) {
            ImGui.textDisabled("No selection")
            return@render
        }

        ImGui.pushID(System.identityHashCode(selected))

        val labelString = "${selected.name} (${selected.displayType})"
        val labelStart = ImGui.getCursorScreenPos()
        val lineHeight = ImGui.getTextLineHeightWithSpacing()

        if (renamingNode === selected) {
            val textSize = ImGui.calcTextSize(labelString)
            ImGui.setNextItemWidth(textSize.x)
            ImGui.setCursorScreenPos(labelStart.x, labelStart.y)
            ImGui.setKeyboardFocusHere()
            val enter = ImGui.inputText(
                "##rename_inspector_${System.identityHashCode(selected)}",
                renameBuffer,
                ImGuiInputTextFlags.EnterReturnsTrue or ImGuiInputTextFlags.AutoSelectAll
            )
            if (enter) {
                selected.name = renameBuffer.get()
                renamingNode = null
            }
            if (ImGui.isKeyPressed(ImGui.getKeyIndex(ImGuiKey.Escape))) {
                renamingNode = null
            }
        } else {
            ImGui.text(selected.name)
            ImGui.sameLine()
            ImGui.textDisabled(" (${selected.displayType})")

            val textSize = ImGui.calcTextSize(labelString)
            ImGui.setCursorScreenPos(labelStart.x, labelStart.y)
            ImGui.invisibleButton("##rename_zone_${System.identityHashCode(selected)}", textSize.x, lineHeight)
            if (ImGui.isItemClicked(0)) selection.select(selected)
            if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
                renamingNode = selected
                renameBuffer.set(selected.name)
            }
            ImGui.setCursorScreenPos(labelStart.x, labelStart.y + lineHeight)
        }

        // IsActive toggle
        val windowRight = ImGui.getWindowPosX() + ImGui.getWindowContentRegionMaxX()
        val checkBoxSize = ImGui.getFrameHeight()
        val checkBoxX = windowRight - checkBoxSize - 10f
        ImGui.setCursorScreenPos(checkBoxX, labelStart.y - checkBoxSize / 4)

        if (selected is GameObject) {
            val active = selected.isActive
            if (ImGui.checkbox("##active_${System.identityHashCode(selected)}", active)) {
                selected.isActive = !active
            }
        }

        ImGui.setCursorScreenPos(labelStart.x, labelStart.y + lineHeight)
        ImGui.separator()

        if (selected is GameObject) {
            for (comp in selected.components) {
                comp.onInspectorGUI()
                ImGui.separator()
            }
        }

        ImGui.popID()
    }
}
