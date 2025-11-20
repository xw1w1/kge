package kge.ui.toolkit.dragndrop

import kotlin.reflect.KClass

data class DragDropPayload<T : Any>(
    val type: KClass<T>,
    val data: T,
    val label: String
)