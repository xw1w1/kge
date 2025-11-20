package kge.editor.project

import kge.editor.EditorApplication
import kge.editor.core.Camera
import kge.editor.core.GameObject

class Scene(
    var id: String,
    var displayName: String?,
) {
    val root: GameObject = EditorSceneRoot(this)
    val activeCamera: Camera?
        get() = EditorApplication.getInstance().getViewport().getEditorCamera().targetCamera

    var onLoad: (Scene) -> Unit = { this.onLoad() }
    var onUpdate: (Scene, Float) -> Unit = { _, delta -> this.onUpdate(delta) }

    fun onLoad() {}

    fun onUpdate(deltaTime: Float) {
        activeCamera?.cameraComponent?.onUpdate()
        this.getAllObjects().filter { it.activeInHierarchy }
            .forEach { it.onUpdate() }
    }

    fun onRender() {}

    fun onUnload() {}

    fun getAllObjects(): List<GameObject> {
        val result = mutableListOf<GameObject>()
        root.forEachChildRecursive { node ->
            result += node
        }
        return result
    }

    fun getSelection() = EditorApplication.getInstance().getEditorSelection()

    class EditorSceneRoot(val scene: Scene, name: String = scene.displayName ?: "Untitled Scene") : GameObject(name) {
        override var name: String
            get() = scene.displayName ?: "Untitled Scene"
            set(_) {}
    }
}