package kge.editor

import kge.editor.render.ShaderProgram
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_LINEAR
import org.lwjgl.opengl.GL11.GL_RGBA
import org.lwjgl.opengl.GL11.GL_RGBA8
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import javax.imageio.ImageIO

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
        var attributesType: String? = null
        var mode = "TRIANGLES"
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        var parsingVertices = false
        var parsingIndices = false

        reader.forEachLine { raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEachLine

            if (line.startsWith("#ATTRIBUTES")) {
                attributesType = line.substringAfter("#ATTRIBUTES").trim()
                return@forEachLine
            }

            if (line.startsWith("#")) return@forEachLine
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

        val vertexAttributes = when (attributesType) {
            "POSITION_NORMAL" -> GLMesh.VertexAttributes.POSITION_NORMAL
            "POSITION_COLOR" -> GLMesh.VertexAttributes.POSITION_COLOR
            "POSITION_NORMAL_UV" -> GLMesh.VertexAttributes.POSITION_NORMAL_UV
            "POSITION_UV" -> GLMesh.VertexAttributes.POSITION_UV
            else -> {
                when (strideFloats) {
                    6 -> GLMesh.VertexAttributes.POSITION_NORMAL
                    3 -> GLMesh.VertexAttributes.POSITION
                    8 -> GLMesh.VertexAttributes.POSITION_NORMAL_UV
                    5 -> GLMesh.VertexAttributes.POSITION_UV
                    else -> GLMesh.VertexAttributes.POSITION
                }
            }
        }

        return GLMesh.create(
            vertices = vertices.toFloatArray(),
            vertexAttributes = vertexAttributes,
            indices = if (indices.isEmpty()) null else indices.toIntArray(),
            drawMode = if (mode.equals("LINES", ignoreCase = true)) GL11.GL_LINES else GL11.GL_TRIANGLES
        )
    }

    fun loadTextureID(path: String): Int {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found: $path")

        val image = ImageIO.read(stream)
        val w = image.width
        val h = image.height
        val pixels = IntArray(w * h)
        image.getRGB(0, 0, w, h, pixels, 0, w)

        val buffer = BufferUtils.createByteBuffer(w * h * 4)
        for (p in pixels) {
            buffer.put(((p shr 16) and 0xFF).toByte())
            buffer.put(((p shr 8) and 0xFF).toByte())
            buffer.put((p and 0xFF).toByte())
            buffer.put(((p shr 24) and 0xFF).toByte())
        }
        buffer.flip()

        val id = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, id)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

        return id
    }

    private fun readTextResource(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found in path $path")
        return stream.bufferedReader().use { it.readText() }
    }
}