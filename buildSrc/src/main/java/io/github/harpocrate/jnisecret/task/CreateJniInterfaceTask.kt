package io.github.harpocrate.jnisecret.task

import io.github.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.harpocrate.jnisecret.utils.Config
import io.github.harpocrate.jnisecret.utils.JniInterfaceUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateJniInterfaceTask: DefaultTask() {

    @Input
    @Optional
    var configuration: JniSecretConfiguration? = null

    @Input
    var flavor: String = ""

    @TaskAction
    fun createJniInterface() {
        val safeConfiguration = configuration ?: throw GradleException("No configuration found")
        mkdirGeneratedSouuceDir()
        val jni = buildJniInterface(safeConfiguration)
        saveJniInterface(safeConfiguration, jni)
    }

    private fun mkdirGeneratedSouuceDir() {
        val projBuildDir = project.buildDir
        val generatedDir = "/generated/source/jniSecret"

        val dir = File("$projBuildDir$generatedDir")

        if(!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun buildJniInterface(configuration: JniSecretConfiguration): String {

        val flavors = configuration.productFlavors.first { it.name == flavor }
        val functions = flavors.getSecrets()
            .let { secretsFlavor ->
                val mutableSecret = secretsFlavor.toMutableMap()
                configuration.defaultConfig.getSecrets().forEach { secretDefault ->
                    if(!mutableSecret.containsKey(secretDefault.key)) {
                        mutableSecret[secretDefault.key] = secretDefault.value
                    }
                }
                mutableSecret
            }
            .map { JniInterfaceUtils.getJniFunction(it.key) }

            .joinToString("\n\t")

        val jni = JniInterfaceUtils.getJniInterface(configuration.packagename, configuration.className, Config.SO_LIB_NAME, functions)

        return jni
    }

    private fun saveJniInterface(configuration: JniSecretConfiguration, content: String) {
        val packageDir = configuration.getPackageName().replace('.', '/').plus("/")
        val dir = File("${project.projectDir}${Config.SRC_DIR}${Config.JAVA_DIR}/$packageDir")

        if(!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${configuration.className}.kt")
        file.writeText(content)
        file.createNewFile()
    }
}