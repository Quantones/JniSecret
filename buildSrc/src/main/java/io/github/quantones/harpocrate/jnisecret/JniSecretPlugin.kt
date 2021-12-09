package io.github.quantones.harpocrate.jnisecret

import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretEntries
import io.github.quantones.harpocrate.jnisecret.exceptions.NoExternalBuildException
import io.github.quantones.harpocrate.jnisecret.task.CreateCMakeListsTask
import io.github.quantones.harpocrate.jnisecret.task.CreateCppTask
import io.github.quantones.harpocrate.jnisecret.task.CreateJniInterfaceTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class JniSecretPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "jniSecret"
        const val CHECK_EXTERNAL_NATIVE_TASK = "verifyExternalNativeBuild"
    }

    override fun apply(project: Project) {


        //
        // Plugin configuration
        //

        val secrets = project.container(JniSecretEntries::class.java)
        val configuration = project.extensions.create(EXTENSION_NAME, JniSecretConfiguration::class.java, secrets)

        project.afterEvaluate {
            //
            // Task configuration
            //

            val checkCmakeTask = project.tasks.register(CHECK_EXTERNAL_NATIVE_TASK) { t ->
                t.group = EXTENSION_NAME
                t.doFirst {
                    val cmakePath = project.android().externalNativeBuild.cmake.path
                    if(configuration.generateCMake && cmakePath == null) {
                        throw NoExternalBuildException()
                    }
                }
            }

            //
            // Task
            //

            project.android().applicationVariants().all { variant ->

                val flavorName = variant.name
                    .replace("Debug", "")
                    .replace("Release", "")

                val outJniDir = File("${project.buildDir}/generated/source/JniSecret/${flavorName}/")

                val jniTask = project.tasks.register(
                    "buildJniInterface${variant.name.capitalize()}",
                    CreateJniInterfaceTask::class.java
                ) { t ->
                    t.group = EXTENSION_NAME
                    t.configuration = configuration
                    t.doFirst {
                        //t.configuration
                        t.flavor = flavorName
                        t.outDir = outJniDir
                    }
                    t.doLast {
                        variant.addJavaSourceFoldersToModel(outJniDir)
                    }
                }

                val cppTask = project.tasks.register(
                    "buildCppFile${variant.name.capitalize()}",
                    CreateCppTask::class.java
                ) { t ->
                    t.group = EXTENSION_NAME
                    t.doFirst {
                        t.configuration = configuration
                        t.flavor = flavorName
                    }
                }

                val makeTask = project.tasks.register(
                    "buildCMake${variant.name.capitalize()}",
                    CreateCMakeListsTask::class.java
                ) { t ->
                    t.group = EXTENSION_NAME
                    t.doFirst {
                        t.configuration = configuration
                    }
                }

                val preBuildTaskName =
                    "pre${variant.name.capitalize()}Build"
                val buildJniInterfaceTask =
                    "buildJniInterface$flavorName"
                val buildCppFileTask =
                    "buildCppFile$flavorName"
                val buildCmakeTask =
                    "buildCMake$flavorName"


                val preBuildTask = project.tasks.getByName(preBuildTaskName)

                if (configuration.generateCMake) {
                    // buildCmake -> preBuild
                    preBuildTask.dependsOn(makeTask)
                    // buildJniInterface -> buildCMake
                    makeTask.dependsOn(cppTask)

                } else {
                    // buildJniInterface -> preBuild
                   preBuildTask.dependsOn(cppTask)
                }
                // verifyExternalNativeBuild -> buildCpp
                cppTask.dependsOn(checkCmakeTask)

                jniTask.dependsOn(cppTask)


                // Create JNI interface into build/generated dir

                variant.registerJavaGeneratingTask(
                    jniTask,
                    outJniDir
                )
                /*
                val kotlinCompileTask =
                    project.tasks.findByName("compile${variant.name.capitalize()}Kotlin") as? SourceTask
                if (kotlinCompileTask != null) {
                    kotlinCompileTask.dependsOn(jniTask)
                    println("${project.buildDir}/generated/source/jniSecret/dev/com/harpocrate/sample/")

                    val srcSet = project.objects.sourceDirectorySet("jnisecret", "jnisecret")
                        .srcDir(File("${project.buildDir}/generated/source/jniSecret/dev/com/harpocrate/sample/"))
                    kotlinCompileTask.source = srcSet

                    project.android().sourceSets.create("jniSecret${variant.name}") {
                        val set =
                            setOf(File("${project.buildDir}/generated/source/jniSecret/dev/com/harpocrate/sample/"))
                        it.java.setSrcDirs(set)
                        it.java.include("*.kt")
                    }
                    project.android().sourceSets.getByName("main") {
                        it.java.srcDir(File("${project.buildDir}/generated/source/jniSecret/dev/com/harpocrate/sample/"))
                        it.java.include("*.kt")

                        it.java.srcDirs.forEach {
                            println(it.path)
                        }
                    }
                }*/
            }
        }
    }
}