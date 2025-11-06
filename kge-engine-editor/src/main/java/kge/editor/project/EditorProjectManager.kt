package kge.editor.project

import kge.editor.EditorApplication
import java.io.File

class EditorProjectManager {
    private var _project: EditorProject? = null

    fun getProject(): EditorProject? {
        return _project
    }

    fun getCurrentScene(): EditorSceneImpl? {
        return _project?.getCurrentScene()
    }

    fun openProject(path: String): EditorProject {
        //val folder = File(path)
        //if (!folder.exists() || !folder.isDirectory) {
        //    throw IllegalArgumentException("Path $path is not a valid project folder.")
        //}
        //
        //val meta = folder.resolve("project.kge")
        // TODO: parsing
        EditorApplication.getInstance().setTitle(path)
        val project = EditorProject(path, "1.0",
            mutableMapOf(
                "assetsRoot" to "none",
                "engineVersion" to "none",
                "description" to "none"
            ))

        _project = project
        _project!!.onProjectLoad()
        return project
    }

    fun rename(string: String) {
        _project?.rename(string)
        EditorApplication.getInstance().setTitle(string)
    }
}