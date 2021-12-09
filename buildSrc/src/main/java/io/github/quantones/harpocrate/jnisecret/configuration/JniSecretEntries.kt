package io.github.quantones.harpocrate.jnisecret.configuration

import org.gradle.api.tasks.Input

open class JniSecretEntries(@Input val name: String) {

    @Input
    var secrets: MutableMap<String, String> = mutableMapOf()

    fun secret(name: String, value: String) {
        secrets[name] = value
    }

}