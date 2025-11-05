package kge.editor.render

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryStack
import kotlin.use

class ShaderProgram(vertexSrc: String, fragSrc: String) {
    var programId: Int
        private set

    init {
        val vertexId = compileShader(vertexSrc, GLShaderType.VERTEX)
        val fragId = compileShader(fragSrc, GLShaderType.FRAGMENT)

        programId = GL20.glCreateProgram()
        GL20.glAttachShader(programId, vertexId)
        GL20.glAttachShader(programId, fragId)
        GL20.glLinkProgram(programId)

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("Shader link error: ${GL20.glGetProgramInfoLog(programId)}")
        }

        GL20.glDetachShader(programId, vertexId)
        GL20.glDetachShader(programId, fragId)
        GL20.glDeleteShader(vertexId)
        GL20.glDeleteShader(fragId)
    }

    fun bind() = GL20.glUseProgram(programId)
    fun unbind() = GL20.glUseProgram(0)

    fun setUniformMat4(name: String, mat: Matrix4f) {
        val loc = GL20.glGetUniformLocation(programId, name)
        MemoryStack.stackPush().use {
            val buf = it.mallocFloat(16)
            mat.get(buf)
            GL20.glUniformMatrix4fv(loc, false, buf)
        }
    }

    fun setUniform3f(name: String, vec: Vector3f) {
        val loc = GL20.glGetUniformLocation(programId, name)
        GL20.glUniform3f(loc, vec.x, vec.y, vec.z)
    }

    private fun compileShader(src: String, type: GLShaderType): Int {
        val id = GL20.glCreateShader(type.integer)
        GL20.glShaderSource(id, src)
        GL20.glCompileShader(id)
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("Shader compile error: ${GL20.glGetShaderInfoLog(id)}")
        }
        return id
    }
}