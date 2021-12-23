package io.github.quantones.harpocrate.jnisecret.task

import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.exceptions.NoConfigurationException
import io.github.quantones.harpocrate.jnisecret.utils.CMakeListsUtils
import io.github.quantones.harpocrate.jnisecret.utils.Config
import io.github.quantones.harpocrate.jnisecret.utils.GitIgnoreUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateCMakeListsTask: DefaultTask() {

    @Nested
    @Optional
    var configuration: JniSecretConfiguration? = null

    @OutputDirectory
    @Optional
    var outDir: File? = null

    @TaskAction
    fun createCMakeLists() {
        val safeConfiguration = configuration ?: throw NoConfigurationException()
        val content = CMakeListsUtils
            .getFileContent(
                safeConfiguration.storingType,
                safeConfiguration.className,
                "${project.projectDir}${Config.SRC_DIR}${Config.CPP_DIR}",
                Config.CPP_FILENAME
            )

        val file = File(outDir, Config.CMAKE_FILENAME)

        if(!file.exists() || (file.exists() && file.readText().contains(content)) ) {
            file.delete()
            file.appendText(content)
            file.createNewFile()

            GitIgnoreUtils.addToProjectGitIgnore(
                project,
                GitIgnoreUtils.GITIGNORE_CMAKELISTS)
        }

    }

}