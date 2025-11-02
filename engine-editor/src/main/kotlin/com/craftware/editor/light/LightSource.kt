package com.craftware.editor.light

import org.joml.Vector4f

/**
 * Источник света.
 * Я еще не определился, должно ли это API оставаться таким же при сборке приекта, но
 * свою position и rotation из тега Transform, хотя по сути Light тоже будет тегом,
 * и это очень сильно усложняет ситуацию. Но и добавлять отдельный Transform чисто для этой штуки
 * тоже бред.
 *
 * скорее всего я просто сделаю кастомный тип ноды, как это было с камерой редактора
 */
interface LightSource {
    var color: Vector4f
    var intensity: Float
}