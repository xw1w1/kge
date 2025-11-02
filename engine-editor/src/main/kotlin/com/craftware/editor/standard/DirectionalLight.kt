package com.craftware.editor.standard

import com.craftware.editor.light.Light
import imgui.ImGui
import imgui.type.ImFloat
import org.joml.Vector4f

class DirectionalLight : Light() {
    override var color: Vector4f = Vector4f(1f)
    override var intensity: Float = 1f

    override val displayType: String
        get() = "Directional light"

    var range: Float = 200f
    var angle: Float = 50f

    init {
        this.addComponent(DirectionalLightComponent(this))
    }

    class DirectionalLightComponent(light: DirectionalLight) : LightComponent(light) {
        override fun onInspectorGUI() {
            super.onInspectorGUI()
            val light = this.light as DirectionalLight

            ImGui.textDisabled("Range")
            val fRange = ImFloat(light.range)
            if (ImGui.inputFloat("##Range", ImFloat(light.range))) {
                light.range = fRange.get()
            }

            ImGui.textDisabled("Angle")
            val fAngle = ImFloat(light.angle)
            if (ImGui.inputFloat("##Angle", fAngle)) {
                light.angle = fAngle.get()
            }
        }
    }
}