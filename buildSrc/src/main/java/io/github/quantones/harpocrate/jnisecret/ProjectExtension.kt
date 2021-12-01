package io.github.quantones.harpocrate.jnisecret

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Project

fun Project.android(): BaseExtension {
    val android = project.extensions.findByType(BaseExtension::class.java)
    if (android != null) {
        return android
    } else {
        throw GradleException("Project $name is not an Android project")
    }
}

fun Project.androidPlugin(): BasePlugin {
    return project.androidPlugin()
}


fun BaseExtension.applicationVariants(): DomainObjectSet<out BaseVariant> {
    return when (this) {
        is AppExtension -> {
            applicationVariants
        }
        else -> throw GradleException("Unsupported BaseExtension type!")
    }
}