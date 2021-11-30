package io.github.harpocrate.jnisecret.utils

import java.io.File

object GitIgnoreUtils {

    const val GITIGNORE_FILENAME = ".gitignore"
    const val GITINGORE_CPP = "*.cpp"
    const val GITIGNORE_CMAKELISTS = "CMakeLists.txt"

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

}