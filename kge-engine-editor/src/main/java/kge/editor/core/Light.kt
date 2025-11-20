package kge.editor.core

import kge.editor.component.LightComponent

class Light : GameObject("Light") {
    val light: LightComponent = this.requireComponent(LightComponent::class)
}
