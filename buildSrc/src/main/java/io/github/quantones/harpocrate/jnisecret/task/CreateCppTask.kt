package io.github.quantones.harpocrate.jnisecret.task

import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.utils.Config
import io.github.quantones.harpocrate.jnisecret.utils.CppUtils
import io.github.quantones.harpocrate.jnisecret.utils.GitIgnoreUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateCppTask: DefaultTask() {

    @Input
    @Optional
    var configuration: JniSecretConfiguration? = null

    @Input
    @Optional
    var flavor: String? = null

    @TaskAction
    fun createCppFile() {
        if(configuration == null) {
            throw GradleException("No configuration found")
        }
        configuration?.let {
            val content = buildCppContent(it)
            saveCppFile(content)
            setGitIgnore()
        }
    }

    private fun buildCppContent(configuration: JniSecretConfiguration): String {
        val packageName = CppUtils.transformPackageName(configuration.getPackageName())
        val className = CppUtils.transformClassName(configuration.className)
        val values = configuration
            .productFlavors.first { it.name == flavor }
            .getSecrets()
            .let { secretsFlavor ->
                val mutableSecret = secretsFlavor.toMutableMap()
                configuration.defaultConfig.getSecrets().forEach { secretDefault ->
                    if(!mutableSecret.containsKey(secretDefault.key)) {
                        mutableSecret[secretDefault.key] = secretDefault.value
                    }
                }
                mutableSecret
            }
            .map { Pair(it.key, it.value) }

        return CppUtils.getCppContent(packageName, className, values, configuration.getStoringType())
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