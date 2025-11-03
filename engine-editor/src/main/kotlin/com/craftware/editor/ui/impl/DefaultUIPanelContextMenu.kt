package com.craftware.editor.ui.impl

import com.craftware.editor.ui.UIPanelContextMenu

class DefaultUIPanelContextMenu(
    private val settings: UIPanelSettings
) : UIPanelContextMenu("DefaultUIPanelContextMenu") {
    init {
        add("Lock resize", selected = settings.doNotResize) {
            settings.doNotResize = !settings.doNotResize
        }

        add("Pin to Dockspace", selected = settings.pinToDockspace) {
            settings.pinToDockspace = !settings.pinToDockspace
        }
    }
}
