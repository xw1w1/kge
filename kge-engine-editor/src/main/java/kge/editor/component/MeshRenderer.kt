package kge.editor.component

import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import kge.api.std.IGLMesh
import kge.editor.Primitives
import kge.editor.ui.EditorFont
import kge.editor.ui.EditorText
import org.joml.Vector4f

class MeshRenderer(
    var mesh: IGLMesh = Primitives.cube()
) : Component("Mesh Renderer") {
    var renderEnabled: Boolean = true

    override fun onInspectorUI() {
        val flags = ImGuiTreeNodeFlags.DefaultOpen or
                ImGuiTreeNodeFlags.FramePadding or
                ImGuiTreeNodeFlags.SpanAvailWidth

        ImGui.dummy(0f, 2f)

        ImGui.setWindowFontScale(0.9f)
        ImGui.pushFont(EditorFont.semiBold)
        val isOpen = ImGui.treeNodeEx("Mesh Renderer##${System.identityHashCode(this)}", flags)
        ImGui.popFont()
        ImGui.setWindowFontScale(1f)

        if (isOpen) {
            val cursorPosX = ImGui.getCursorScreenPosX()
            val cursorPosY = ImGui.getCursorScreenPosY()
            val drawList = ImGui.getWindowDrawList()
            drawList.addLine(
                cursorPosX,
                cursorPosY,
                cursorPosX + ImGui.getContentRegionAvailX(),
                cursorPosY,
                ImColor.rgba(70, 70, 70, 255)
            )
            ImGui.dummy(0f, 4f)

            if (ImGui.checkbox("Render Enabled", renderEnabled)) {
                renderEnabled = !renderEnabled
            }

            ImGui.separator()
            EditorText.colored("Mesh Bounds:", Vector4f(0.8f, 0.8f, 0.8f, 1f))

            ImGui.text(
                "Min: [%.2fx, %.2fy, %.2fz]".format(
                    mesh.boundsMin.x,
                    mesh.boundsMin.y,
                    mesh.boundsMin.z
                )
            )
            ImGui.text(
                "Max: [%.2fx, %.2fy, %.2fz]".format(
                    mesh.boundsMax.x,
                    mesh.boundsMax.y,
                    mesh.boundsMax.z
                )
            )

            ImGui.treePop()
        }
    }

    fun render() {
        if (!renderEnabled) return
        mesh.bind()
        mesh.render()
        mesh.unbind()
    }

    override fun onDetach() {
        mesh.destroy()
    }
}
