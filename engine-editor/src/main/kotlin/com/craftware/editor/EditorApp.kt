package com.craftware.editor

import com.craftware.editor.controls.Keyboard
import com.craftware.editor.controls.Mouse
import com.craftware.editor.ui.EditorUI
import com.craftware.editor.ui.ImGuiContext
import org.lwjgl.glfw.GLFW

class EditorApp {
    private val window = Window(1600, 900)
    private lateinit var mouse: Mouse
    private lateinit var keyboard: Keyboard

    private val imguiLayer = ImGuiContext(window)
    private lateinit var editorUI: EditorUI

    private var _currentScene: Scene? = null

    var scene: Scene
        get() = _currentScene!!
        set(value) = openScene(value)

    val sceneNullable: Scene?
        get() = _currentScene

    fun run() {
        window.init()

        mouse = Mouse(window.getHandle())
        keyboard = Keyboard(window.getHandle())

        imguiLayer.initImGui()
        editorUI = EditorUI(window)
        _instance = this

        loop()

        imguiLayer.dispose()
        window.dispose()
    }

    fun openScene(scene: Scene) {
        _currentScene = scene
        editorUI.init(scene)
        window.setTitle(scene.name)
    }

    fun getHandle(): Long {
        return window.getHandle()
    }

    fun getMouse() = mouse
    fun getKeyboard() = keyboard

    private fun loop() {
        var lastTime = GLFW.glfwGetTime()

        while (!window.shouldClose()) {
            val currentTime = GLFW.glfwGetTime()
            val delta = (currentTime - lastTime).toFloat()
            lastTime = currentTime

            window.pollEvents()
            window.clear()

            imguiLayer.newFrame()
            editorUI.render(delta)
            imguiLayer.render()

            window.swapBuffers()
            mouse.endFrame()
        }
    }

    companion object {
        private var _instance: EditorApp? = null
        fun getInstance(): EditorApp {
            return this._instance!!
        }
    }
}
