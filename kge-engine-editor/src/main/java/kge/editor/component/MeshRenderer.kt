package kge.editor.component

import imgui.ImGui
import kge.api.std.IGLMesh

class MeshRenderer(
    var mesh: IGLMesh
) : Component("Mesh Renderer") {
    var renderEnabled: Boolean = true

    override fun onInspectorUI() {
        ImGui.textColored(0.7f, 0.7f, 1f, 1f, "Mesh Renderer")

        if (ImGui.checkbox("Render", renderEnabled)) {
            renderEnabled = !renderEnabled
        }

        ImGui.separator()
        ImGui.textDisabled("Bounds:")
        ImGui.text("Min: (%.2f, %.2f, %.2f)".format(mesh.boundsMin.x, mesh.boundsMin.y, mesh.boundsMin.z))
        ImGui.text("Max: (%.2f, %.2f, %.2f)".format(mesh.boundsMax.x, mesh.boundsMax.y, mesh.boundsMax.z))
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
