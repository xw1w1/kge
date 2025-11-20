package kge.editor.core

import kge.api.std.LightType
import kge.editor.component.SkyAndSunComponent
import kge.editor.component.LightComponent

class SkyAndSun : GameObject("Sky and Sun") {
    val skyAndSun: SkyAndSunComponent = this.requireComponent(SkyAndSunComponent::class)
    val sunLight: LightComponent = this.requireComponent(LightComponent::class)

    init {
        sunLight.type = LightType.Directional
        sunLight.color = skyAndSun.sunColor
        sunLight.intensity = skyAndSun.sunIntensity
        sunLight.direction = skyAndSun.sunDirection

        skyAndSun.sunColor = sunLight.color
        skyAndSun.sunIntensity = sunLight.intensity
        skyAndSun.sunDirection = sunLight.direction
    }
}