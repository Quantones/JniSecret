package io.github.quantones.harpocrate.jnisecret.task

import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.exceptions.NoConfigurationException
import io.github.quantones.harpocrate.jnisecret.utils.Config
import io.github.quantones.harpocrate.jnisecret.utils.CppUtils
import io.github.quantones.harpocrate.jnisecret.utils.GitIgnoreUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateCppTask: DefaultTask() {

    @Nested
    @Optional
    var configuration: JniSecretConfiguration? = null

    @Input
    @Optional
    var flavor: String? = null

    @TaskAction
    fun createCppFile() {
        if(configuration == null) {
            throw NoConfigurationException()
        }
        configuration?.let {
            val content = buildCppContent(it)
            saveCppFile(content)
            setGitIgnore()
        }
    }

    private fun buildCppContent(configuration: JniSecretConfiguration): String {
        val packageName = CppUtils.transformPackageName(configuration.packageName)
        val className = CppUtils.transformClassName(configuration.className)
        val values = configuration
            .defaultConfig
            .secrets
            .map {
                it.key to it.value
            }
            .toMap()
            .let {
                val mutableMap = it.toMutableMap()
                configuration.productFlavors.findByName(flavor)?.secrets?.forEach { entry ->
                    mutableMap[entry.key] = entry.value
                }
                mutableMap
            }
            .toList()

        return CppUtils.getCppContent(packageName, className, values, configuration.storingType)
    }

    private fun saveCppFile(content: String) {
        val destDir = File("${project.projectDir}${Config.SRC_DIR}${Config.CPP_DIR}")

        if(!destDir.exists()) {
            destDir.mkdirs()
        }

        val cppFile = File(destDir, Config.CPP_FILENAME)
        cppFile.writeText(content)
        cppFile.createNewFile()
    }

    private fun setGitIgnore() {
        GitIgnoreUtils.addToProjectGitIgnore(
            project,
            "${Config.SRC_DIR}${Config.CPP_DIR}")
        /*val file = File("${project.projectDir}", GitIgnoreUtils.GITIGNORE_FILENAME)
        file.writeText(GitIgnoreUtils.GITINGORE_CPP)
        file.createNewFile()*/
    }
}