package io.github.quantones.harpocrate.jnisecret.utils

object Config {

    const val DEFAULT_CONFIG_NAME = "defaultConfig"
    const val SRC_DIR = "/src/main/"
    const val JAVA_DIR = "java/"
    const val ASSETS_DIR_NAME = "assets/"
    const val JNISECRET_DIR_NAME = "jni-secret/"
    const val CPP_FILENAME = "jni-secret.cpp"
    const val CPP_DIR_NAME = "cpp/"
    const val SO_LIB_NAME = "jni_secret"
    const val CMAKE_FILENAME = "CMakeLists.txt"

    const val CPP_DIR = "$CPP_DIR_NAME$JNISECRET_DIR_NAME"
    const val ASSETS_DIR = "$SRC_DIR$ASSETS_DIR_NAME"

}