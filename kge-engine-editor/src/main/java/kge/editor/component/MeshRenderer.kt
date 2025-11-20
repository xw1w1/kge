package kge.editor.component

import kge.api.std.IGLMesh
import kge.editor.Primitives
import kge.editor.core.GameObject
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.boolField

class MeshRenderer(
    node: GameObject,
    var mesh: IGLMesh = Primitives.cube()
) : Component(node) {
    @SerializeField("Render Mesh")
    var renderEnabled: Boolean by boolField(true)

    override val typeName: String = "Mesh Renderer"

    fun render() {
        if (!renderEnabled) return
        mesh.bind()
        mesh.render()
        mesh.unbind()
    }
}
