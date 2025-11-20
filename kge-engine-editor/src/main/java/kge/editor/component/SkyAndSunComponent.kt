package kge.editor.component

import kge.api.std.LightType
import kge.editor.core.GameObject
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.boolField
import kge.ui.toolkit.delegates.floatField
import kge.ui.toolkit.delegates.vec3Field
import org.joml.Vector3f
import kotlin.math.absoluteValue

class SkyAndSunComponent(val node: GameObject) : Component(node) {
    @SerializeField("Sun Color")
    var sunColor: Vector3f by vec3Field(Vector3f(1.0f, 0.95f, 0.8f))

    @SerializeField("Sun Intensity")
    var sunIntensity: Float by floatField(1.0f)

    @SerializeField("Sun Direction")
    var sunDirection: Vector3f by vec3Field(Vector3f(0.3f, -1.0f, 0.2f).normalize())

    @SerializeField("Sky Top Color")
    var skyTopColor: Vector3f by vec3Field(Vector3f(0.3f, 0.5f, 0.9f))

    @SerializeField("Sky Horizon Color")
    var skyHorizonColor: Vector3f by vec3Field(Vector3f(0.8f, 0.9f, 1.0f))

    @SerializeField("Sky Bottom Color")
    var skyBottomColor: Vector3f by vec3Field(Vector3f(0.6f, 0.7f, 0.9f))

    @SerializeField("Sky Intensity")
    var skyIntensity: Float by floatField(0.3f)

    @SerializeField("Atmosphere Thickness")
    var atmosphereThickness: Float by floatField(1.0f, min = 0.1f, max = 5.0f)

    @SerializeField("Enable Sun Flare")
    var enableSunFlare: Boolean by boolField(false)

    @SerializeField("Time of Day")
    var timeOfDay: Float by floatField(0.5f, 0.001f, min = 0.0f, max = 1.0f)

    override val typeName: String = "Sky and Sun"

    override fun onUpdate() {
        super.onUpdate()
        updateFromTimeOfDay()
        syncWithSunLight()
    }

    private fun syncWithSunLight() {
        node.requireComponent(LightComponent::class).let { light ->
            if (light.color != sunColor || light.intensity != sunIntensity || light.direction != sunDirection) {
                sunColor = light.color
                sunIntensity = light.intensity
                sunDirection = light.direction
            }
        }
    }

    fun updateFromTimeOfDay() {
        val normalizedTime = (timeOfDay - 0.25f).coerceIn(0.0f, 0.5f) * 2.0f

        if (timeOfDay > 0.25f && timeOfDay < 0.75f) {
            val dayFactor = (normalizedTime - 0.5f).absoluteValue * 2.0f

            sunColor = Vector3f(1.0f, 0.95f - dayFactor * 0.2f, 0.8f - dayFactor * 0.3f)
            sunIntensity = 1.0f - dayFactor * 0.3f

            skyTopColor = Vector3f(
                0.3f + dayFactor * 0.2f,
                0.5f + dayFactor * 0.2f,
                0.9f + dayFactor * 0.1f
            )
            skyHorizonColor = Vector3f(
                0.8f - dayFactor * 0.1f,
                0.9f - dayFactor * 0.1f,
                1.0f - dayFactor * 0.1f
            )
        } else {
            sunColor = Vector3f(0.3f, 0.4f, 0.8f)
            sunIntensity = 0.1f
            skyTopColor = Vector3f(0.05f, 0.05f, 0.15f)
            skyHorizonColor = Vector3f(0.1f, 0.1f, 0.2f)
        }

        node.getComponent<LightComponent>()?.let { light ->
            light.color = sunColor
            light.intensity = sunIntensity
            light.direction = sunDirection.normalize()
            light.type = LightType.Directional
        }
    }
}