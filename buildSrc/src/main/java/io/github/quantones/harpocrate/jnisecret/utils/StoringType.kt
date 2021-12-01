package io.github.quantones.harpocrate.jnisecret.utils

import org.gradle.api.GradleException

enum class StoringType(val type: String) {


    BASIC("basic"),
    OBFUSCATED("obfuscated");

    companion object {
        fun getByValue(type: String): StoringType {
            values().forEach {
                if(it.type == type) {
                    return it
                }
            }
            throw GradleException("No StoringType found for $type")
        }
    }
}