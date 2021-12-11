package io.github.quantones.harpocrate.jnisecret

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretEntries
import io.github.quantones.harpocrate.jnisecret.exceptions.NoExternalBuildException
import io.github.quantones.harpocrate.jnisecret.task.CreateCMakeListsTask
import io.github.quantones.harpocrate.jnisecret.task.CreateCppTask
import io.github.quantones.harpocrate.jnisecret.task.CreateJniInterfaceTask
import io.github.quantones.harpocrate.jnisecret.utils.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
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
            // Task
            //

            val checkCmakeTask = checkCMakeTask(
                project,
                configuration
            )

            project.android().applicationVariants().all { variant ->

                val flavorName = variant.name
                    .replace("Debug", "")
                    .replace("Release", "")

                val jniTask = jniTask(
                    project,
                    variant,
                    configuration,
                    flavorName
                )

                val cppTask = cppTask(
                    project,
                    variant,
                    configuration,
                    flavorName
                )

                val makeTask = makeTask(
                    project,
                    variant,
                    configuration
                )

                //
                // Task orchestration
                //

                val preBuildTaskName =
                    "pre${variant.name.capitalize()}Build"


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
            }
        }
    }

    private fun checkCMakeTask(project: Project,
                               configuration: JniSecretConfiguration): TaskProvider<Task> {
        return project.tasks.register(CHECK_EXTERNAL_NATIVE_TASK) { t ->
            t.group = EXTENSION_NAME
            t.doFirst {
                val cmakePath = project.android().externalNativeBuild.cmake.path
                if(configuration.generateCMake && cmakePath == null) {
                    throw NoExternalBuildException()
                }
            }
        }
    }

    private fun jniTask(project: Project,
                        variant: BaseVariant,
                        configuration: JniSecretConfiguration,
                        flavorName: String): TaskProvider<CreateJniInterfaceTask> {

        val outJniDir = File("${project.buildDir}/generated/source/JniSecret/${flavorName}/")

        val jniTask = project.tasks.register(
            "buildJniInterface${variant.name.capitalize()}",
            CreateJniInterfaceTask::class.java
        ) { t ->
            t.group = EXTENSION_NAME
            t.configuration = configuration
            t.flavor = flavorName
            t.outDir = outJniDir

            val ktFile = File("$outJniDir/${configuration.packageName.replace(".", "/")}/", "${configuration.className}.kt")
            if(!ktFile.exists()) {
                t.outputs.upToDateWhen { false }
            }

            t.doLast {
                variant.addJavaSourceFoldersToModel(outJniDir)
            }
        }

        variant.registerJavaGeneratingTask(
            jniTask,
            outJniDir
        )

        project.tasks.findByName("compile${variant.name.capitalize()}Kotlin")?.let { t ->
            val ktDir = File(outJniDir, configuration.packageName.replace(".", "/"))
            val srcSet = project.objects.sourceDirectorySet("JniSecret${variant.name}", "JniSecret${variant.name}").srcDir(ktDir)
            (t as? SourceTask)?.let {
                it.source(srcSet)
            }
        }

        return jniTask
    }

    private fun cppTask(
        project: Project,
        variant: BaseVariant,
        configuration: JniSecretConfiguration,
        flavorName: String
    ): TaskProvider<CreateCppTask> {

        val outCppDir = File("${project.projectDir}${Config.SRC_DIR}${Config.CPP_DIR}")

        return project.tasks.register(
            "buildCppFile${variant.name.capitalize()}",
            CreateCppTask::class.java
        ) { t ->

            t.group = EXTENSION_NAME
            t.configuration = configuration
            t.flavor = flavorName
            t.outDir = outCppDir

            if (!(File(outCppDir, Config.CPP_FILENAME).exists())) {
                t.outputs.upToDateWhen { false }
            }
        }
    }

    private fun makeTask(
        project: Project,
        variant: BaseVariant,
        configuration: JniSecretConfiguration
    ): TaskProvider<CreateCMakeListsTask> {
        val outCmakeDir = File("${project.projectDir}")

        return project.tasks.register(
            "buildCMake${variant.name.capitalize()}",
            CreateCMakeListsTask::class.java
        ) { t ->
            t.group = EXTENSION_NAME
            t.configuration = configuration
            t.outDir = outCmakeDir
        }
    }
}