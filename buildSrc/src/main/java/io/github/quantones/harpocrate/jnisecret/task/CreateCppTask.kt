package io.github.quantones.harpocrate.jnisecret.task

import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.exceptions.NoConfigurationException
import io.github.quantones.harpocrate.jnisecret.utils.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

open class CreateCppTask: DefaultTask() {

    @Nested
    @Optional
    var configuration: JniSecretConfiguration? = null

    @Input
    @Optional
    var flavor: String? = null

    @OutputDirectory
    @Optional
    var outDir: File? = null

    @TaskAction
    fun createCppFile() {

        if(configuration == null) {
            throw NoConfigurationException()
        }
        configuration?.let {
            val content = buildCppContent(it)
            saveCppFile(content)
            additionalFiles(it)
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
            .map {
                val value: String = it.second
                it.first to value
            }
            .toMap()
            .let {
                val mutableMap = it.toMutableMap()
                configuration.productFlavors.findByName(flavor)?.secrets?.forEach { entry ->
                    mutableMap[entry.key] = entry.value
                }
                mutableMap
            }
            .let {
                val mutableMap = it.toMutableMap()
                if(configuration.storingType == StoringType.PKCS12) {
                    mutableMap.forEach { entry ->
                        val value = EncryptUtils.encrypt(
                            assetPath = "${project.projectDir}${Config.ASSETS_DIR}${configuration.certificate.path}",
                            filename = configuration.certificate.filename,
                            alias = configuration.certificate.alias,
                            password = configuration.certificate.password,
                            text = entry.value
                        )
                        mutableMap[entry.key] = value
                    }
                }
                mutableMap
            }
            .toList()

        return CppUtils.getCppContent(
            packageName,
            className,
            values,
            configuration.storingType,
            configuration.certificate.filename,
            "${configuration.certificate.path}${configuration.certificate.password}")
    }

    private fun saveCppFile(content: String) {

        outDir?.let {
            if (!it.exists()) {
                it.mkdirs()
            }
            val cppFile = File(it, Config.CPP_FILENAME)

            if(!cppFile.exists() || (cppFile.exists() && cppFile.readText() != content)) {
                cppFile.delete()
                cppFile.writeText(content)
                cppFile.createNewFile()
            }
        }
    }

    private fun setGitIgnore() {
        GitIgnoreUtils.addToProjectGitIgnore(
            project,
            "${Config.SRC_DIR}${Config.CPP_DIR}")
    }

    private fun additionalFiles(configuration: JniSecretConfiguration) {
        if(configuration.storingType == StoringType.PKCS12) {
            outDir?.let {
                val b64Header = File(it, CPPBase64Utils.HEADER_NAME)
                val b64Cpp = File(it, CPPBase64Utils.CPP_NAME)

                if(!b64Header.exists()) {
                    b64Header.createNewFile()
                    b64Header.appendText(CPPBase64Utils.header)
                }

                if(!b64Cpp.exists()) {
                    b64Cpp.createNewFile()
                    b64Cpp.appendText(CPPBase64Utils.cpp)
                }
            }
        }
    }
}