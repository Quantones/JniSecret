package io.github.quantones.harpocrate.jnisecret

import com.android.build.gradle.internal.dsl.ExternalNativeBuild
import com.android.build.gradle.internal.dsl.ExternalNativeCmakeOptions
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretEntries
import io.github.quantones.harpocrate.jnisecret.task.CreateCMakeListsTask
import io.github.quantones.harpocrate.jnisecret.task.CreateCppTask
import io.github.quantones.harpocrate.jnisecret.task.CreateJniInterfaceTask
import io.github.quantones.harpocrate.jnisecret.utils.CMakeListsUtils
import io.github.quantones.harpocrate.jnisecret.utils.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class JniSecretPlugin : Plugin<Project> {

    private val EXTENSION_NAME = "jniSecret"

    override fun apply(project: Project) {
        //
        // Plugin configuration
        //

        val secrets = project.container(JniSecretEntries::class.java)
        val configuration = project.extensions.create(EXTENSION_NAME, JniSecretConfiguration::class.java, secrets)

        //
        // Task creation
        //
        project.android().productFlavors.all { pf ->
            project.tasks.register("buildJniInterface${pf.name[0].toUpperCase()+pf.name.substring(1)}", CreateJniInterfaceTask::class.java) { t ->
                t.group = EXTENSION_NAME
                t.doFirst {
                    t.configuration = configuration
                    t.flavor = pf.name
                }
            }
            project.tasks.register("buildCppFile${pf.name[0].toUpperCase()+pf.name.substring(1)}", CreateCppTask::class.java) { t ->
                t.group = EXTENSION_NAME
                t.doFirst {
                    t.configuration = configuration
                    t.flavor = pf.name
                }
            }

            project.tasks.register(
                "buildCMake${pf.name[0].toUpperCase() + pf.name.substring(1)}",
                CreateCMakeListsTask::class.java
            ) { t ->
                t.group = EXTENSION_NAME
                t.doFirst {
                    t.configuration = configuration
                }
            }
        }

        //
        // Task orchestration
        //

        project.android().applicationVariants().all {
            var flavorName = it.name
                .replace("Debug", "")
                .replace("Release", "")
            flavorName = flavorName[0].toUpperCase() + flavorName.substring(1)

            val preBuildTask =
                "pre${it.name[0].toUpperCase() + it.name.substring(1)}Build"
            val buildJniInterfaceTask =
                "buildJniInterface$flavorName"
            val buildCppFileTask =
                "buildCppFile$flavorName"
            val buildCmakeTask =
                "buildCMake$flavorName"


            if (configuration.generateCMake) {

                project.tasks.getByName(preBuildTask) { t ->
                    t.dependsOn(buildCmakeTask)
                }

                project.tasks.getByName(buildCmakeTask) { t ->
                    t.dependsOn(buildJniInterfaceTask)
                }
            } else {
                project.tasks.getByName(preBuildTask) { t ->
                    t.dependsOn(buildJniInterfaceTask)
                }
            }

            project.tasks.getByName(buildJniInterfaceTask) { t ->
                t.dependsOn(buildCppFileTask)
            }
        }

        val file = File("${project.projectDir}/${Config.CMAKE_FILENAME}")
        if(file.exists()) {
            project.android().externalNativeBuild.cmake.path = file
        }
    }
}