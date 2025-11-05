package kge.editor.render

import org.lwjgl.opengl.GL20

enum class GLShaderType(val integer: Int) {
    VERTEX(GL20.GL_VERTEX_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER)
}