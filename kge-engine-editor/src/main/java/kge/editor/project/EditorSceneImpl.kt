package kge.editor.project

import kge.api.render.IPerspectiveViewCamera
import kge.api.std.INode
import kge.api.std.INodeParent
import kge.api.std.IScene
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
        return this.root.children.filter { it is GameObject }.map { it as GameObject }
    }

    override fun clear() {
        this.root.clearChildren()
    }

    class EditorSceneRoot(sceneId: String) : INodeParent {
        override val children: MutableList<INode> = mutableListOf()

        override var name: String = sceneId
        override var isActive: Boolean = true
            set(_) { throw UnsupportedOperationException() }

        override var parent: INodeParent? = this
        override val displayType: String = "$sceneId#EditorSceneRoot"

        override fun addChild(child: INode) {
            this.children.add(child)
        }

        override fun removeChild(child: INode) {
            this.children.remove(child)
        }

        override fun clearChildren() {
            this.children.clear()
        }

        override fun hasChild(node: INode): Boolean {
            return this.children.contains(node)
        }

        override fun findChildByName(name: String): INode? {
            return this.children.firstOrNull { it.name == name }
        }

        override fun forEachChildRecursive(action: (INode) -> Unit) {
            this.children.forEach(action)
        }
    }
}