package io.github.quantones.harpocrate.jnisecret.task

import com.squareup.kotlinpoet.*
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.work.InputChanges
import java.io.File

abstract class CreateJniInterfaceTask: DefaultTask() {

    @Nested
    @Optional
    var configuration: JniSecretConfiguration? = null

    @Input
    var flavor: String = ""

    @OutputDirectory
    @Optional
    var outDir: File? = null

    @TaskAction
    fun createJniInterface(inputChanged: InputChanges) {
        saveJniInterface(configuration!!)
    }

    private fun saveJniInterface(configuration: JniSecretConfiguration) {

        val flavors = configuration.productFlavors.first { it.name == flavor }

        val functions = flavors.secrets
            .let { secretsFlavor ->
                val mutableSecret = secretsFlavor.toMutableMap()
                configuration.defaultConfig.secrets.forEach { secretDefault ->
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
            FileSpec.builder(configuration.packageName, configuration.className)
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