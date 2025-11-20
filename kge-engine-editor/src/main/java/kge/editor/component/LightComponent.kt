package kge.editor.component

import kge.api.std.LightType
import kge.editor.core.GameObject
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.enumField
import kge.ui.toolkit.delegates.floatField
import kge.ui.toolkit.delegates.vec3Field
import org.joml.Vector3f

class LightComponent(node: GameObject) : Component(node) {
    @SerializeField("Type")
    var type: LightType by enumField(LightType.Point, LightType::class)

    @SerializeField("Color")
    var color: Vector3f by vec3Field(Vector3f(1f, 1f, 1f))

    @SerializeField("Intensity")
    var intensity: Float by floatField(1f)

    @SerializeField("Range")
    var range: Float by floatField(10f,
        visibleWhen = { this.type != LightType.Directional })

    @SerializeField("Direction")
    var direction: Vector3f by vec3Field(Vector3f(0f, -1f, 0f),
        visibleWhen = { this.type != LightType.Point })

    @SerializeField("Falloff Angle (Inner)")
    var innerAngle: Float by floatField(15f, min = 0F, max = Float.MAX_VALUE,
        visibleWhen = { this.type == LightType.Spot })

    @SerializeField("Falloff Angle (Outer)")
    var outerAngle: Float by floatField(50f, min = 0f, max = Float.MAX_VALUE,
        visibleWhen = { this.type == LightType.Spot })

    override val typeName: String = "Light"
}
