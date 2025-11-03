package com.craftware.editor.ui.impl

import imgui.flag.ImGuiWindowFlags

class UIPanelSettings {
    var doNotResize: Boolean = false
    var pinToDockspace: Boolean = false

    fun toMask(): Int {
        var flags = 0
        if (pinToDockspace) flags = flags or ImGuiWindowFlags.NoMove
        if (doNotResize) flags = flags or ImGuiWindowFlags.NoResize
        return flags
    }
}