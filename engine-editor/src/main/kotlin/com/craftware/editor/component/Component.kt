package com.craftware.editor.component

import imgui.ImGui

open class Component() {
    open fun onInspectorGUI() {
        ImGui.text(this::class.simpleName)
    }
}