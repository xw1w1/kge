package kge.editor.ui.window

import imgui.ImGui
import imgui.type.ImBoolean
import imgui.type.ImString
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.core.GameObject
import kge.ui.toolkit.UIText
import kge.editor.ui.EditorUIPanel
import kge.editor.ui.UIIcon
import kge.editor.viewport.ViewportGizmoManager
import kge.ui.toolkit.UIToolkit

class ObjectInspectorWindow : EditorUIPanel("Inspector"), IRenderable {
    override fun render(delta: Float) {
        this.beginUI()
        content = {
            val selection = EditorApplication.getInstance().getEditorSelection().getSelectedObjects()
            val cursorPos = ImGui.getCursorPos()

            when {
                selection.size > 1 -> {
                    ImGui.setCursorPos(cursorPos.x + 20, cursorPos.y)
                    UIText.semiBold("Multiple selections (${selection.size})")
                }

                selection.isEmpty() -> {
                    ImGui.setCursorPos(cursorPos.x + 20, cursorPos.y)
                    UIText.semiBold("No selection.")
                }

                else -> {
                    val selected = selection.first()

                    drawTitleBar(selected)

                    UIToolkit.spacedSeparator()

                    selected.components.forEach { component ->
                        drawInspectorForComponent(component)
                    }
                }
            }

            UIToolkit.spacedSeparator()

            drawToolsSettings()
        }
        this.endUI()
    }

    private fun drawTitleBar(obj: GameObject) {
        val startX = ImGui.getCursorPosX()
        val startY = ImGui.getCursorPosY()
        val width = ImGui.getContentRegionAvailX()
        val height = 64f
        val padding = 8f

        val winX = ImGui.getWindowPosX()
        val winY = ImGui.getWindowPosY()

        val dl = ImGui.getWindowDrawList()

        dl.addRectFilled(
            winX + startX,
            winY + startY,
            winX + startX + width,
            winY + startY + height,
            ImGui.getColorU32(0.16f, 0.16f, 0.18f, 1f),
            6f
        )

        val contentStartX = startX + padding
        val contentStartY = startY + padding
        val contentHeight = height - padding * 2

        val iconSide = contentHeight - padding

        ImGui.setCursorPos(contentStartX + padding / 2, contentStartY + padding / 2)

        ImGui.image(
            UIIcon.loadIcon("gameobject", "std/icons/gameobject.png"),
            iconSide,
            iconSide
        )

        val textStartX = contentStartX + contentHeight + padding

        ImGui.setCursorPos(textStartX, contentStartY)

        val nameBuf = ImString(obj.name, 128)
        val textWidth = width - (textStartX - startX) - 90f - padding

        val fieldsY = contentStartY + (contentHeight - ImGui.getFrameHeight()) / 2f

        ImGui.setNextItemWidth(textWidth)
        ImGui.setCursorPos(startX + height + padding, fieldsY)
        if (ImGui.inputText("##objName", nameBuf)) {
            obj.name = nameBuf.toString()
        }

        ImGui.setCursorPos(startX + width - height - padding - (padding * 1.5f /** ~12 **/), fieldsY)

        val active = ImBoolean(obj.activeSelf)
        if (ImGui.checkbox("Active", active)) {
            obj.activeSelf = active.get()
        }

        ImGui.setCursorPos(startX, startY)
        ImGui.invisibleButton("##titleBarRegion", width, height)

        if (ImGui.beginPopupContextItem("titleBarContext")) {
            if (ImGui.menuItem("Delete")) {
                GameObject.destroy(obj)
            }
            ImGui.endPopup()
        }

        ImGui.setCursorPosY(startY + height + 6f)
    }

    private fun drawToolsSettings() {
        drawInspectorForFields(ViewportGizmoManager, "Tools & Settings")
    }

    override fun pushRenderCallback(cb: IRenderCallback) { }
}
