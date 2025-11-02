package com.craftware.editor.light

import com.craftware.editor.component.Component
import com.craftware.editor.standard.GameObject
import imgui.ImGui
import imgui.type.ImFloat

abstract class Light : GameObject(), LightSource {
    override val displayType: String
        get() = "Light"

    abstract class LightComponent(val light: Light) : Component() {
        override fun onInspectorGUI() {
            ImGui.text("Light")
            val cArr = floatArrayOf(light.color.x, light.color.y, light.color.z, light.color.w)
            if (ImGui.inputFloat4("##LightColor", cArr)) {
                light.color.set(cArr[0], cArr[1], cArr[2], cArr[3])
            }

            ImGui.textDisabled("Intensity")
            val int = ImFloat(light.intensity)
            if (ImGui.inputFloat("##Intensity", int)) {
                light.intensity = int.get()
            }
        }
    }
}