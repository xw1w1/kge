package kge.api.std

interface IScene {
    var name: String

    val root: INodeParent
    val activeCamera: IPerspectiveViewCamera?

    var onLoad: (IScene) -> Unit
    var onUpdate: (IScene, Float) -> Unit

    fun onLoad()
    fun onUpdate(deltaTime: Float)

    fun onRender()

    fun onUnload()

    fun findByName(name: String): INode?

    fun getAllObjects(): List<INode>

    fun clear()
}