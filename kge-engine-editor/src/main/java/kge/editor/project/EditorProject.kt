package kge.editor.project

import kge.api.std.IProjectDescriptor
import kge.api.std.IScene

class EditorProject(
    override var name: String,
    override val version: String,

    private val projectMeta: MutableMap<String, String>
) : IProjectDescriptor {
    override val scenes: List<String> = emptyList()
    override var startupScene: IScene? = null // post-load change only

    private var _currentScene: IScene? = startupScene // null if startup scene == null
    private val _loadedKnownScenes: MutableList<EditorSceneImpl> = mutableListOf()

    override val assetsRoot: String = projectMeta["assetsRoot"]!!
    override val engineVersion: String = projectMeta["engineVersion"]!!
    override var description: String? = projectMeta["description"]

    override fun onProjectLoad() {
        _currentScene?.onLoad()

        if (_currentScene == null) {
            val scene = EditorSceneImpl("untitled", "Untitled Scene")
            scene.onLoad()

            _loadedKnownScenes.add(scene)

            _currentScene = scene
        }
    }

    override fun onProjectUnload() { }

    fun getCurrentScene(): EditorSceneImpl? {
        return _currentScene as? EditorSceneImpl?
    }

    fun openScene(scene: EditorSceneImpl) {
        if (_currentScene == scene) return

        if (scenes.contains(scene.id)) {
            // TODO: добавление сцен в проект
            // хотя по сути это бесполезно
            // если через редактор как-то открыли сцену
            // значит она есть в проекте
        }

        if (!_loadedKnownScenes.contains(scene)) {
            scene.onLoad(scene)

            _loadedKnownScenes.add(scene)
            _currentScene = scene
            return
        }
    }

    fun rename(value: String) {
        name = value
        projectMeta["name"] = value
    }
}