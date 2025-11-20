package kge.editor.component

import kge.editor.core.GameObject
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.floatField

class CameraComponent(target: GameObject) : Component(target) {
    @SerializeField("Field of view")
    var fov by floatField(60f)

    @SerializeField("Clip Plane (Near)")
    var zNear by floatField(0.01f, 0.0001f, min = 0.0f, max = 1.0f)

    @SerializeField("Clip Plane (Far)")
    var zFar by floatField(1000f, 1f, min = 0.1f)

    override val typeName: String = "Camera"
}

