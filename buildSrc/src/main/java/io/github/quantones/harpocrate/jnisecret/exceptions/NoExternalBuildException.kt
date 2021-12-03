package io.github.quantones.harpocrate.jnisecret.exceptions

class NoExternalBuildException: Exception("""
    No externalBuild cmake found in android gradle configuration. Please add in your project gradle:
    android {
        externalNativeBuild {
            cmake {
                path = file("CMakeLists.txt")
            }
        }
    }
""".trimIndent()) {
}