package kge.editor.render

import kge.editor.EditorApplication
import kge.editor.component.LightComponent
import kge.editor.component.MeshRenderer
import kge.editor.component.SkyAndSunComponent
import kge.editor.project.Scene
import kge.editor.ResourceLoader
import kge.editor.viewport.ViewportGizmoManager
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.joml.Math.max

class RenderPipeline {
    private lateinit var skyShader: ShaderProgram
    private lateinit var lightingShader: ShaderProgram

    private var viewportGrid: ViewportGridRenderer = ViewportGridRenderer()
    private var viewportAxis: ViewportAxisRenderer = ViewportAxisRenderer()

    private var skyVAO = 0
    private var skyVBO = 0

    fun init() {
        viewportGrid.init()
        viewportAxis.init()
        lightingShader = ResourceLoader.loadShader("std/shaders/default_lit.vert", "std/shaders/default_lit.frag")
        skyShader = ResourceLoader.loadShader("std/shaders/sky.vert", "std/shaders/sky.frag")
        setupSkyVAO()
    }

    fun render(scene: Scene, camera: kge.editor.viewport.ViewportCamera, targetFramebuffer: Int, targetW: Int, targetH: Int) {
        val viewProj = camera.getViewProjection(targetW.toFloat() / max(1f, targetH.toFloat()))
        val lights = scene.getAllObjects().mapNotNull { it.getComponent<LightComponent>() }
        val targetCamera = camera.targetCamera

        val skyAndSun = findSkyAndSun(scene)
        skyAndSun?.updateFromTimeOfDay()

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, targetFramebuffer)
        GL11.glViewport(0, 0, targetW, targetH)

        GL11.glClearColor(0.09f, 0.1f, 0.12f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        viewportGrid.render(viewProj)
        viewportAxis.render(viewProj)

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LESS)

        skyAndSun?.let {
            renderSky(it)
        }

        lightingShader.bind()
        lightingShader.setUniformMat4("u_ViewProj", viewProj)
        lightingShader.setUniform3f("u_CameraPosition", camera.targetCamera!!.transform.position)
        lightingShader.setUniform3f("u_CameraForward", camera.targetCamera!!.transform.forward)

        val validLights = lights.take(32)
        lightingShader.setUniform1i("u_LightCount", validLights.size)
        for ((i, comp) in validLights.withIndex()) {
            val t = comp.gameObject.transform
            lightingShader.setUniform1i("u_Lights[$i].type", comp.type.ordinal)
            lightingShader.setUniform3f("u_Lights[$i].color", comp.color)
            lightingShader.setUniform1f("u_Lights[$i].intensity", comp.intensity)
            lightingShader.setUniform1f("u_Lights[$i].range", comp.range)
            lightingShader.setUniform3f("u_Lights[$i].position", t.position)
            lightingShader.setUniform3f("u_Lights[$i].direction", comp.direction)
            val innerC = kotlin.math.cos(Math.toRadians(comp.innerAngle.toDouble())).toFloat()
            val outerC = kotlin.math.cos(Math.toRadians(comp.outerAngle.toDouble())).toFloat()
            lightingShader.setUniform1f("u_Lights[$i].innerCutoff", innerC)
            lightingShader.setUniform1f("u_Lights[$i].outerCutoff", outerC)
        }

        val selection = EditorApplication.getInstance().getEditorSelection()
        val selList = selection.getSelectedObjects()
        for (obj in scene.getAllObjects()) {
            val renderer = obj.getComponent<MeshRenderer>() ?: continue
            if (!obj.activeInHierarchy) continue
            val model = obj.transform.getWorldMatrix()
            lightingShader.setUniformMat4("u_Model", model)
            lightingShader.setUniform3f("u_Color", if(selList.contains(obj)) Vector3f(1f,1f,1f) else Vector3f(0.8f,0.4f,0.2f))
            renderer.render()
        }

        lightingShader.unbind()

        if (selList.isNotEmpty()) {
            val center = Vector3f()
            for (obj in selList) center.add(obj.transform.getWorldPosition())
            center.div(selList.size.toFloat())

            val suppressGizmoHighlight = !camera.isRotating
            ViewportGizmoManager.render(viewProj, center, targetCamera!!.transform.position, suppressGizmoHighlight)
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    private fun findSkyAndSun(scene: Scene): SkyAndSunComponent? {
        return scene.getAllObjects().firstNotNullOfOrNull {
            it.getComponent<SkyAndSunComponent>()
        }
    }

    private fun setupSkyVAO() {
        skyVAO = GL30.glGenVertexArrays()
        skyVBO = GL30.glGenBuffers()

        val vertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f,  1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f
        )

        GL30.glBindVertexArray(skyVAO)
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, skyVBO)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW)
        GL30.glEnableVertexAttribArray(0)
        GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
        GL30.glBindVertexArray(0)
    }

    private fun renderSky(skyAndSun: SkyAndSunComponent) {
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        skyShader.bind()

        skyShader.setUniform3f("u_SkyTopColor", skyAndSun.skyTopColor)
        skyShader.setUniform3f("u_SkyHorizonColor", skyAndSun.skyHorizonColor)
        skyShader.setUniform3f("u_SkyBottomColor", skyAndSun.skyBottomColor)
        skyShader.setUniform3f("u_SunDirection", skyAndSun.sunDirection.normalize())
        skyShader.setUniform3f("u_SunColor", skyAndSun.sunColor)
        skyShader.setUniform1f("u_SkyIntensity", skyAndSun.skyIntensity)
        skyShader.setUniform1f("u_AtmosphereThickness", skyAndSun.atmosphereThickness)
        skyShader.setUniform1f("u_TimeOfDay", skyAndSun.timeOfDay)

        GL30.glBindVertexArray(skyVAO)
        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4)
        GL30.glBindVertexArray(0)

        skyShader.unbind()

        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }
}