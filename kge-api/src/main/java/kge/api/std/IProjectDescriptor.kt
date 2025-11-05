package kge.api.std

interface IProjectDescriptor {
    val name: String
    val version: String
    var startupScene: IScene?
    val scenes: List<String>
    val assetsRoot: String
    val engineVersion: String
    var description: String?

    fun onProjectLoad()

    fun onProjectUnload()
}