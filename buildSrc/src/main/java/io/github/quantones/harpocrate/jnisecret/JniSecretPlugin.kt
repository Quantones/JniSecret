package io.github.quantones.harpocrate.jnisecret

import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretConfiguration
import io.github.quantones.harpocrate.jnisecret.configuration.JniSecretEntries
import io.github.quantones.harpocrate.jnisecret.exceptions.NoExternalBuildException
import io.github.quantones.harpocrate.jnisecret.task.CreateCMakeListsTask
import io.github.quantones.harpocrate.jnisecret.task.CreateCppTask
import io.github.quantones.harpocrate.jnisecret.task.CreateJniInterfaceTask
import org.gradle.api.Plugin
import org.gradle.api.Project

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

        //
        // Task configuration
        //

        project.tasks.register(CHECK_EXTERNAL_NATIVE_TASK) { t ->
            t.group = EXTENSION_NAME
            t.doFirst {
                val cmakePath = project.android().externalNativeBuild.cmake.path
                if(configuration.generateCMake && cmakePath == null) {
                    throw NoExternalBuildException()
                }
            }
        }

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
                // buildCmake -> preBuild
                project.tasks.getByName(preBuildTask) { t ->
                    t.dependsOn(buildCmakeTask)
                }

                // buildJniInterface -> buildCMake
                project.tasks.getByName(buildCmakeTask) { t ->
                    t.dependsOn(buildJniInterfaceTask)
                }

            } else {
                // buildJniInterface -> preBuild
                project.tasks.getByName(preBuildTask) { t ->
                    t.dependsOn(buildJniInterfaceTask)
                }
            }

            //  buildCpp -> buildJniInterface
            project.tasks.getByName(buildJniInterfaceTask) { t ->
                t.dependsOn(buildCppFileTask)
            }

            project.tasks.getByName(buildCppFileTask) { t ->
                t.dependsOn(CHECK_EXTERNAL_NATIVE_TASK)
            }
        }
    }
}