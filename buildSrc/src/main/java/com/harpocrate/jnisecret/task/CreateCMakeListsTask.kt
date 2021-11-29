package com.harpocrate.jnisecret.task

import com.harpocrate.jnisecret.configuration.JniSecretConfiguration
import com.harpocrate.jnisecret.configuration.JniSecretEntries
import com.harpocrate.jnisecret.utils.CMakeListsUtils
import com.harpocrate.jnisecret.utils.Config
import com.harpocrate.jnisecret.utils.GitIgnoreUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateCMakeListsTask: DefaultTask() {

    @Input
    @Optional
    var configuration: JniSecretConfiguration? = null

    @TaskAction
    fun createCMakeLists() {
        val content = CMakeListsUtils
            .getFileContent(
                "${project.projectDir}${Config.SRC_DIR}${Config.CPP_DIR}",
                Config.CPP_FILENAME
            )

        val file = File("${project.projectDir}", Config.CMAKE_FILENAME)
        file.writeText(content)
        file.createNewFile()

        GitIgnoreUtils.setGitIgnore(
            "${project.projectDir}",
            GitIgnoreUtils.GITIGNORE_CMAKELISTS
        )
    }

}