package io.github.quantones.harpocrate.jnisecret.task

import com.squareup.kotlinpoet.*
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.exceptions.NoConfigurationException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateJniInterfaceTask: DefaultTask() {

    @Input
    @Optional
    var configuration: JniSecretConfiguration? = null

    @Input
    var flavor: String = ""

    @OutputDirectory
    @Optional
    var outDir: File? = null

    @TaskAction
    fun createJniInterface() {
        val safeConfiguration = configuration ?: throw NoConfigurationException()
        saveJniInterface(safeConfiguration)
    }

    private fun saveJniInterface(configuration: JniSecretConfiguration) {

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
            .map { FunSpec.builder(it.key)
                .addModifiers(KModifier.EXTERNAL)
                .returns(String::class)
                .build()
            }

        val kotlin =
            FileSpec.builder(configuration.packagename, configuration.className)
                .addType(TypeSpec.classBuilder(configuration.className)
                    .addInitializerBlock(CodeBlock.builder()
                        .addStatement("System.loadLibrary(%S)", configuration.className)
                        .build()
                    )
                    .addFunctions(functions).build()
                )
                .build()

        outDir?.let {
            kotlin.writeTo(it)
        }
    }
}