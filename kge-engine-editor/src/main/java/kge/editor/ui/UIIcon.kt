package kge.editor.ui

import kge.editor.ResourceLoader

object UIIcon {
    private val loaded = mutableMapOf<String, Long>()

    init {
        this.loadIcon("translate", "std/icons/translate_icon.png")
        this.loadIcon("rotate", "std/icons/rotate_icon.png")
        this.loadIcon("scale", "std/icons/scale_icon.png")
        this.loadIcon("gameobject", "std/icons/gameobject.png")
        this.loadIcon("go_sillouette", "std/icons/go_thin.png")
    }

    fun loadIcon(name: String, path: String): Long {
        val textureId = ResourceLoader.loadTextureID(path).toLong()
        loaded[name] = textureId
        return textureId
    }

    fun getIcon(name: String): Long? = loaded[name]
}