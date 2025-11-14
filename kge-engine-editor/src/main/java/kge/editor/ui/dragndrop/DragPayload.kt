package kge.editor.ui.dragndrop

import kotlin.reflect.KClass

data class DragPayload<T : Any>(
    val type: KClass<T>,
    val data: T,
    val label: String
)