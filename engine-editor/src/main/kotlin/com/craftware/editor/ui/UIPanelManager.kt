package com.craftware.editor.ui

//TODO: добавить докинг в будущем
object UIPanelManager {
    private val panels = mutableListOf<UIPanel>()

    fun register(p: UIPanel) {
        if (panels.none { it.id == p.id }) panels += p
    }
}
