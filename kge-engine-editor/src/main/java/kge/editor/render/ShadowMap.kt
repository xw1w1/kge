package kge.editor.render

import org.joml.Matrix4f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*

class ShadowMap(val size: Int = 2048) {
    var fbo: Int = 0
    var depthTex: Int = 0
    val lightSpace = Matrix4f()

    fun init() {
        fbo = glGenFramebuffers()
        depthTex = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, depthTex)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, size, size, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0L)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)

        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(1f, 1f, 1f, 1f))

        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTex, 0)

        glDrawBuffer(GL_NONE)
        glReadBuffer(GL_NONE)

        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("ShadowMap framebuffer incomplete: $status")
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun bindForWriting() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glViewport(0, 0, size, size)
        glClear(GL_DEPTH_BUFFER_BIT)
    }

    fun bindTexture(textureId: Int) {
        glBindTexture(GL_TEXTURE_2D, depthTex)
    }

    fun cleanup() {
        if (fbo != 0) glDeleteFramebuffers(fbo)
        if (depthTex != 0) glDeleteTextures(depthTex)
    }
}
