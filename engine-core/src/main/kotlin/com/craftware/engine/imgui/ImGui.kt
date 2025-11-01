package com.craftware.engine.imgui

import imgui.ImGui

fun popup(label: String, content: () -> Unit) {
    if (ImGui.beginPopup(label)) {
        content()
        ImGui.endPopup()
    }
}

fun popupContext(label: String, triggerOnItem: Boolean = true, content: () -> Unit) {
    val opened = if (triggerOnItem)
        ImGui.beginPopupContextItem(label)
    else
        ImGui.beginPopupContextWindow(label)

    if (opened) {
        content()
        ImGui.endPopup()
    }
}

fun menu(label: String, content: () -> Unit) {
    if (ImGui.beginMenu(label)) {
        content()
        ImGui.endMenu()
    }
}

fun menuItem(label: String, shortcut: String? = null, enabled: Boolean = true, action: (() -> Unit)? = null) {
    if (ImGui.menuItem(label, shortcut ?: "", false, enabled)) {
        action?.invoke()
    }
}

fun checkbox(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    if (ImGui.checkbox(label, value)) {
        onToggle(value)
    }
}

fun separator() = ImGui.separator()

fun imguiPanel(title: String, init: () -> Unit) {
    ImGui.begin(title)
    init()
    ImGui.end()
}

fun textDisabled(text: String) {
    ImGui.textDisabled(text)
}

fun dragFloat(label: String, value: Float, speed: Float = 0.1f, min: Float = 0f, max: Float = 0f, onChange: (Float) -> Unit) {
    val arr = floatArrayOf(value)
    if (ImGui.dragFloat(label, arr, speed, min, max)) {
        onChange(arr[0])
    }
}
