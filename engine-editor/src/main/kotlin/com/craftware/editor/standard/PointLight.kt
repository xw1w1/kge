package com.craftware.editor.standard

import com.craftware.editor.light.Light
import imgui.ImGui
import imgui.type.ImFloat
import org.joml.Vector4f

class PointLight : Light() {
    override var color: Vector4f = Vector4f(1f)
    override var intensity: Float = 1f

    override val displayType: String
        get() = "Point light"

    var constant: Float = 1f
    var linear: Float = 0f
    var quadratic: Float = 0f

    var range: Float = 200f

    init {
        this.addComponent(PointLightComponent(this))
    }

    class PointLightComponent(light: PointLight) : LightComponent(light) {
        override fun onInspectorGUI() {
            super.onInspectorGUI()
            val light = this.light as PointLight

            ImGui.textDisabled("Range")
            val float = ImFloat(light.range)
            if (ImGui.inputFloat("##Range", ImFloat(light.range))) {
                light.range = float.get()
            }
        }
    }
}