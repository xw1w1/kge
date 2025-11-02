package com.craftware.editor.viewport

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

class ViewportFramebuffer {
    var buffer: Int = 0
    var colorTex: Int = 0

    private var frameWidth: Int = 0
    private var frameHeight: Int = 0
    private var depthRbo: Int = 0

    fun ensureFramebuffer(width: Int, height: Int) {
        if (buffer != 0 && frameWidth == width && frameHeight == height) return

        if (buffer != 0) {
            GL30.glDeleteFramebuffers(buffer)
            GL11.glDeleteTextures(colorTex)
            GL30.glDeleteRenderbuffers(depthRbo)
        }

        frameWidth = width
        frameHeight = height
        buffer = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer)

        colorTex = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTex)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB,
            width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0L
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER,
            GL30.GL_COLOR_ATTACHMENT0,
            GL11.GL_TEXTURE_2D,
            colorTex,
            0
        )

        depthRbo = GL30.glGenRenderbuffers()
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthRbo)
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height)
        GL30.glFramebufferRenderbuffer(
            GL30.GL_FRAMEBUFFER,
            GL30.GL_DEPTH_STENCIL_ATTACHMENT,
            GL30.GL_RENDERBUFFER,
            depthRbo
        )

        val status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            error("Framebuffer is not complete: $status")
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }
}
