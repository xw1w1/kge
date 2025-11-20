package kge.api.render

interface IRenderPipeline {
    fun initialize()

    fun resize(width: Int, height: Int)

    fun beginFrame()

    fun endFrame()

    fun destroy()
}