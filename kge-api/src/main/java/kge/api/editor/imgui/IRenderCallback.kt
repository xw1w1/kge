package kge.api.editor.imgui

interface IRenderCallback {
    fun description(parameters: Array<Any>): String
    enum class DefaultRenderCallbackImpl : IRenderCallback {
        SUCCESS {
            override fun description(parameters: Array<Any>): String {
                return "Frame #[${parameters[0]}] of class ${parameters[1]} successfully rendered."
            }
        },
        FAILURE {
            override fun description(parameters: Array<Any>): String {
                return "Frame #[${parameters[0]}] of class ${parameters[1]} failed to render."
            }
        },
        EXCEPTION {
            override fun description(parameters: Array<Any>): String {
                return "Frame #[${parameters[0]}] of class ${parameters[1]} failed to render: ${parameters[2]}!"
            }
        }
    }
}