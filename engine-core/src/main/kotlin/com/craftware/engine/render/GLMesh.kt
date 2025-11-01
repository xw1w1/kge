package com.craftware.engine.render

import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

data class GLMesh(
    val vao: Int,
    val vbo: Int,
    val ebo: Int = 0,
    val vertexCount: Int,
    val drawMode: Int = GL11.GL_TRIANGLES,
    val boundsMin: Vector3f = Vector3f(-0.5f, -0.5f, -0.5f),
    val boundsMax: Vector3f = Vector3f(0.5f, 0.5f, 0.5f)
) {
    fun render() {
        GL30.glBindVertexArray(vao)
        if (ebo != 0) {
            GL11.glDrawElements(drawMode, vertexCount, GL11.GL_UNSIGNED_INT, 0)
        } else {
            GL11.glDrawArrays(drawMode, 0, vertexCount)
        }
        GL30.glBindVertexArray(0)
    }

    companion object {
        fun create(
            vertices: FloatArray,
            strideFloats: Int = 0,
            indices: IntArray? = null,
            drawMode: Int = GL11.GL_TRIANGLES
        ): GLMesh {
            if (vertices.isEmpty()) throw IllegalArgumentException("vertices are empty")

            val stride = when {
                strideFloats > 0 -> strideFloats
                vertices.size % 6 == 0 -> 6
                vertices.size % 3 == 0 -> 3
                else -> 3
            }

            val boundsMin = Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            val boundsMax = Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

            var idx = 0
            while (idx < vertices.size) {
                val x = vertices[idx]
                val y = vertices.getOrElse(idx + 1) { 0f }
                val z = vertices.getOrElse(idx + 2) { 0f }

                boundsMin.x = minOf(boundsMin.x, x)
                boundsMin.y = minOf(boundsMin.y, y)
                boundsMin.z = minOf(boundsMin.z, z)

                boundsMax.x = maxOf(boundsMax.x, x)
                boundsMax.y = maxOf(boundsMax.y, y)
                boundsMax.z = maxOf(boundsMax.z, z)

                idx += stride
            }

            val vao = GL30.glGenVertexArrays()
            GL30.glBindVertexArray(vao)

            val vbo = GL15.glGenBuffers()
            val vBuf: FloatBuffer = MemoryUtil.memAllocFloat(vertices.size)
            vBuf.put(vertices).flip()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_STATIC_DRAW)

            val strideBytes = stride * Float.SIZE_BYTES
            GL20.glEnableVertexAttribArray(0)
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, strideBytes, 0L)

            if (stride >= 6) {
                GL20.glEnableVertexAttribArray(1)
                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
            }

            var ebo = 0
            val vertexCount: Int

            if (indices != null && indices.isNotEmpty()) {
                ebo = GL15.glGenBuffers()
                val iBuf: IntBuffer = MemoryUtil.memAllocInt(indices.size)
                iBuf.put(indices).flip()
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
                GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STATIC_DRAW)
                vertexCount = indices.size
                MemoryUtil.memFree(iBuf)
            } else {
                vertexCount = vertices.size / stride
            }

            GL30.glBindVertexArray(0)
            MemoryUtil.memFree(vBuf)

            return GLMesh(
                vao = vao,
                vbo = vbo,
                ebo = ebo,
                vertexCount = vertexCount,
                drawMode = drawMode,
                boundsMin = boundsMin,
                boundsMax = boundsMax
            )
        }

        fun fromTriangles(vertices: FloatArray, indices: IntArray? = null): GLMesh {
            return create(vertices, strideFloats = 3, indices = indices, drawMode = GL11.GL_TRIANGLES)
        }
    }
}
