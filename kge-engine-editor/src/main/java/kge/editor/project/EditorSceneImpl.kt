package kge.editor.project

import kge.api.render.IPerspectiveViewCamera
import kge.api.std.INode
import kge.api.std.INodeParent
import kge.api.std.IScene
import kge.api.std.Node
import kge.editor.EditorApplication
import kge.editor.GameObject

class EditorSceneImpl(
    override var id: String,
    override var displayName: String?,
) : IScene{
    override val root: INodeParent = EditorSceneRoot(id)
    override val activeCamera: IPerspectiveViewCamera? = EditorApplication.getInstance().getViewport().getEditorCamera()

    override var onLoad: (IScene) -> Unit = { this.onLoad() }
    override var onUpdate: (IScene, Float) -> Unit = { _, delta -> this.onUpdate(delta) }

    override fun onLoad() {}
    override fun onUpdate(deltaTime: Float) {}

    override fun onRender() {}

    override fun onUnload() {}

    override fun findByName(name: String): INode? {
        return this.root.findChildByName(name)
    }

    override fun getAllObjects(): List<GameObject> {
        val result = mutableListOf<GameObject>()
        root.forEachChildRecursive { node ->
            if (node is GameObject) result += node
        }
        return result
    }

    fun getSelection() = EditorApplication.getInstance().getEditorSelection()

    override fun clear() {
        this.root.clearChildren()
    }

    class EditorSceneRoot(sceneId: String) : Node(sceneId), INodeParent {
        init { name = sceneId }

        override var isActive: Boolean = true
            set(_) {
                throw UnsupportedOperationException("EditorSceneRoot cannot change active state")
            }

        override var parent: INodeParent? = null
            set(_) {
                throw UnsupportedOperationException("EditorSceneRoot cannot have a parent")
            }

        override val displayType: String
            get() = "$name#EditorSceneRoot"
    }

}