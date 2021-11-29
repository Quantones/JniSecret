package com.harpocrate.jnisecret

import com.harpocrate.jnisecret.configuration.JniSecretConfiguration
import com.harpocrate.jnisecret.configuration.JniSecretEntries
import com.harpocrate.jnisecret.task.CreateCMakeListsTask
import com.harpocrate.jnisecret.task.CreateCppTask
import com.harpocrate.jnisecret.task.CreateJniInterfaceTask
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
            //var configuration = project.extensions.getByName(extensionName) as JniSecretConfiguration
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
            val flavorName = it.name
                .replace("Debug", "")
                .replace("Release", "")

            val preBuildTask =
                "pre${it.name[0].toUpperCase() + it.name.substring(1)}Build"
            val buildJniInterfaceTask =
                "buildJniInterface${flavorName[0].toUpperCase() + flavorName.substring(1)}"
            val buildCppFileTask =
                "buildCppFile${flavorName[0].toUpperCase() + flavorName.substring(1)}"
            val buildCmakeTask =
                "buildCMake${flavorName[0].toUpperCase() + flavorName.substring(1)}"

            if(configuration.generateCMake) {
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



        //
        // Modify project
        //
        if(configuration.generateCMake) {
            project.android().externalNativeBuild.cmake.path = File("${project.projectDir}/CMakeLists.txt")
        }

        // val srcSet = project.properties["sourceSets"]
        // println("SourceSet $srcSet")

    }
}