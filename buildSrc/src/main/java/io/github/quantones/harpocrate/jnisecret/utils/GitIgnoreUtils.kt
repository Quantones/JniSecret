package io.github.quantones.harpocrate.jnisecret.utils

import org.gradle.api.Project
import java.io.File

object GitIgnoreUtils {

    const val GITIGNORE_FILENAME = ".gitignore"
    const val GITINGORE_CPP = "*.cpp"
    const val GITIGNORE_CMAKELISTS = Config.CMAKE_FILENAME

    fun setGitIgnore(path: String, content: String) {
        val file = File(path, GITIGNORE_FILENAME)
        if(file.exists()) {
            val fileContent = file.readText()
            if(!fileContent.contains(content)) {
                file.appendText("\n$content")
            }
        } else {
            file.writeText(content)
            file.createNewFile()
        }
    }

    fun addToProjectGitIgnore(project: Project, toAdd: String, create: Boolean = true) {
        println(project.projectDir)
        val file = File("${project.projectDir}", GITIGNORE_FILENAME)

        if(!file.exists() && create) {
            file.createNewFile()
        } else if(!file.exists() && !create) {
            return
        }

        val content = file.readText()

        if(!content.contains(toAdd)) {
            file.appendText("\n$toAdd")
        }
    }
}