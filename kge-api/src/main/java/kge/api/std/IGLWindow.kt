package kge.api.std

import org.joml.Vector2i

interface IGLWindow {
    var handle: Long
    var flags: KGEWindowFlags

    var title: String?

    fun init()

    fun getSize(): Vector2i

    fun dispose()
    fun clear()
}