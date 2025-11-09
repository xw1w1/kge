package kge.editor

import kge.editor.render.ShaderProgram
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

    /**
     *  DRAW MODE
     *  VERTICES
     *  INDICES
     */
    fun loadMesh(path: String): GLMesh {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found: $path")

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
            throw IllegalArgumentException("No vertices found in $path")

        val strideFloats = when {
            vertices.size % 6 == 0 -> 6
            vertices.size % 3 == 0 -> 3
            else -> 3
        }

        val strideBytes = strideFloats * Float.SIZE_BYTES

        val boundsMin = Vector3f(Float.POSITIVE_INFINITY)
        val boundsMax = Vector3f(Float.NEGATIVE_INFINITY)
        for (i in vertices.indices step strideFloats) {
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

        // === Создаём и заполняем буферы ===
        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        val vbo = GL15.glGenBuffers()
        val vertexBuffer = MemoryUtil.memAllocFloat(vertices.size)
        vertexBuffer.put(vertices.toFloatArray()).flip()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, strideBytes, 0L)

        if (strideFloats >= 6) {
            GL20.glEnableVertexAttribArray(1)
            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
        }

        var ebo = 0
        if (indices.isNotEmpty()) {
            ebo = GL15.glGenBuffers()
            val indexBuffer = MemoryUtil.memAllocInt(indices.size)
            indexBuffer.put(indices.toIntArray()).flip()
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW)
            MemoryUtil.memFree(indexBuffer)
        }

        GL30.glBindVertexArray(0)
        MemoryUtil.memFree(vertexBuffer)

        return GLMesh(
            vertices = vertices.toFloatArray(),
            indices = if (indices.isEmpty()) null else indices.toIntArray(),
            drawMode = if (mode.equals("LINES", ignoreCase = true)) GL11.GL_LINES else GL11.GL_TRIANGLES,
            boundsMin = boundsMin,
            boundsMax = boundsMax
        ).apply {
            this.vao = vao
            this.vbo = vbo
            this.ebo = ebo
            this.uploaded = true
        }
    }

    private fun readTextResource(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found in path $path")
        return stream.bufferedReader().use { it.readText() }
    }
}