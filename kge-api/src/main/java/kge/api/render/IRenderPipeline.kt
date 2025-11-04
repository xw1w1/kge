package kge.api.render

import kge.api.std.IScene

interface IRenderPipeline {
    fun initialize()

    fun render(scene: IScene, camera: ICamera)

    fun resize(width: Int, height: Int)

    fun beginFrame()

    fun endFrame()

    fun destroy()
}