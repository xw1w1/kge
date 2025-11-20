package kge.editor.project

import kge.editor.EditorApplication

class EditorProject(
    var name: String,
    val version: String,

    private val projectMeta: MutableMap<String, String>
)  {
    val scenes: List<String> = emptyList()
    var startupScene: Scene? = null // post-load change only

    private var _currentScene: Scene? = startupScene // null if startup scene == null
    private val _loadedKnownScenes: MutableList<Scene> = mutableListOf()

    val assetsRoot: String = projectMeta["assetsRoot"]!!
    val engineVersion: String = projectMeta["engineVersion"]!!
    var description: String? = projectMeta["description"]

    fun onProjectLoad() {
        _currentScene?.onLoad()

        if (_currentScene == null) {
            val scene = Scene("untitled", "Untitled Scene")

            this.openScene(scene)
        }
    }

    fun onProjectUnload() { }

    fun getCurrentScene(): Scene? {
        return _currentScene
    }

    fun openScene(scene: Scene) {
        if (_currentScene == scene) return

        if (scenes.contains(scene.id)) {
            // TODO: добавление сцен в проект
            // хотя по сути это бесполезно
            // если через редактор как-то открыли сцену
            // значит она есть в проекте
        }

        if (!_loadedKnownScenes.contains(scene)) {
            _loadedKnownScenes.add(scene)
            _currentScene = scene

            scene.onLoad(scene)
            EditorApplication.getInstance().setTitle(scene.displayName ?: "Untitled")
            return
        }
    }

    fun rename(value: String) {
        name = value
        projectMeta["name"] = value
    }
}