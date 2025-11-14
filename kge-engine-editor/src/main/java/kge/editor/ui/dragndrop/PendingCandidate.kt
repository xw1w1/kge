package kge.editor.ui.dragndrop

import imgui.ImVec2
import kotlin.reflect.KClass

data class PendingCandidate(
    val type: KClass<*>,
    val data: Any,
    val label: String,
    val rectMin: ImVec2,
    val rectMax: ImVec2
)