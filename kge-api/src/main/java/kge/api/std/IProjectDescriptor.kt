package kge.api.std

@Deprecated("")
interface IProjectDescriptor {
    val name: String
    val version: String
    val scenes: List<String>
    val assetsRoot: String
    val engineVersion: String
    var description: String?

    fun onProjectLoad()

    fun onProjectUnload()
}