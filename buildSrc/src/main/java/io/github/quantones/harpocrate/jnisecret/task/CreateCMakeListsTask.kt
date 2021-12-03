package io.github.quantones.harpocrate.jnisecret.task

import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.exceptions.NoConfigurationException
import io.github.quantones.harpocrate.jnisecret.utils.CMakeListsUtils
import io.github.quantones.harpocrate.jnisecret.utils.Config
import io.github.quantones.harpocrate.jnisecret.utils.GitIgnoreUtils
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
        val safeConfiguration = configuration ?: throw NoConfigurationException()
        val content = CMakeListsUtils
            .getFileContent(
                safeConfiguration.className,
                "${project.projectDir}${Config.SRC_DIR}${Config.CPP_DIR}",
                Config.CPP_FILENAME
            )

        val file = File("${project.projectDir}", Config.CMAKE_FILENAME)

        if(!file.exists()) {
            file.createNewFile()
        }

        if(!file.readText().contains(content)) {
            file.delete()
            file.createNewFile()
            file.appendText(content)
        }

        GitIgnoreUtils.addToProjectGitIgnore(
            project,
            GitIgnoreUtils.GITIGNORE_CMAKELISTS)
    }

}