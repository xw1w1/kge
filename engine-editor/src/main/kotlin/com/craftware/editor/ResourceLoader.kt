package com.craftware.editor

import com.craftware.engine.render.GLMesh
import com.craftware.engine.render.ShaderProgram
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer
import java.nio.IntBuffer

object ResourceLoader {
    fun loadShader(vertexPath: String, fragmentPath: String): ShaderProgram {
        val vertexSrc = readTextResource(vertexPath)
        val fragmentSrc = readTextResource(fragmentPath)
        return ShaderProgram(vertexSrc, fragmentSrc)
    }

    private fun readTextResource(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found in path $path")
        return stream.bufferedReader().use { it.readText() }
    }

    /**
     *  DRAW MODE
     *  VERTICES
     *  INDICES
     */
    fun loadMesh(path: String): GLMesh {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("not found $path")

        val reader = BufferedReader(InputStreamReader(stream))
        var mode = "TRIANGLES"
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        var parsingVertices = false
        var parsingIndices = false

        reader.forEachLine { raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachLine

            when {
                line.equals("LINES", ignoreCase = true) -> {
                    mode = "LINES"
                    parsingVertices = false
                    parsingIndices = false
                }
                line.equals("TRIANGLES", ignoreCase = true) -> {
                    mode = "TRIANGLES"
                    parsingVertices = false
                    parsingIndices = false
                }
                line.equals("VERTICES", ignoreCase = true) -> {
                    parsingVertices = true
                    parsingIndices = false
                }
                line.equals("INDICES", ignoreCase = true) -> {
                    parsingVertices = false
                    parsingIndices = true
                }
                else -> {
                    val parts = line.split(" ").filter { it.isNotEmpty() }
                    if (parsingVertices) {
                        for (p in parts) vertices.add(p.toFloat())
                    } else if (parsingIndices) {
                        for (p in parts) indices.add(p.toInt())
                    }
                }
            }
        }

        if (vertices.isEmpty())
            throw IllegalArgumentException("no vertices $path")

        val boundsMin = Vector3f(Float.POSITIVE_INFINITY)
        val boundsMax = Vector3f(Float.NEGATIVE_INFINITY)
        for (i in vertices.indices step 6) {
            val x = vertices[i]
            val y = vertices[i + 1]
            val z = vertices[i + 2]
            if (x < boundsMin.x) boundsMin.x = x
            if (y < boundsMin.y) boundsMin.y = y
            if (z < boundsMin.z) boundsMin.z = z
            if (x > boundsMax.x) boundsMax.x = x
            if (y > boundsMax.y) boundsMax.y = y
            if (z > boundsMax.z) boundsMax.z = z
        }

        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        val vbo = GL15.glGenBuffers()
        val vertexBuffer: FloatBuffer = MemoryUtil.memAllocFloat(vertices.size)
        vertexBuffer.put(vertices.toFloatArray()).flip()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)

        val stride = 6 * 4
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0L)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, (3 * 4).toLong())

        var ebo = 0
        var indexCount = 0
        if (indices.isNotEmpty()) {
            ebo = GL15.glGenBuffers()
            val indexBuffer: IntBuffer = MemoryUtil.memAllocInt(indices.size)
            indexBuffer.put(indices.toIntArray()).flip()
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW)
            indexCount = indices.size
            MemoryUtil.memFree(indexBuffer)
        }

        GL30.glBindVertexArray(0)
        MemoryUtil.memFree(vertexBuffer)

        for (i in vertices.indices step 6) {
            vertices[i + 3] *= -1f
            vertices[i + 4] *= -1f
            vertices[i + 5] *= -1f
        }

        return GLMesh(
            vao = vao,
            vbo = vbo,
            ebo = if (indices.isNotEmpty()) ebo else 0,
            vertexCount = if (indices.isNotEmpty()) indexCount else vertices.size / 6,
            drawMode = if (mode == "LINES") GL11.GL_LINES else GL11.GL_TRIANGLES,
            boundsMin = boundsMin,
            boundsMax = boundsMax
        )
    }
}
