package io.github.quantones.harpocrate.jnisecret.configuration

import org.gradle.api.tasks.Input

open class JniSecretCertificate(
    @Input var path: String = "",
    @Input var filename: String = "",
    @Input var alias: String = "",
    @Input var password: String = "",
) {

    fun path(path: String) {
        this.path = path
    }

    fun filename(filename: String) {
        this.filename = filename
    }

    fun alias(alias: String) {
        this.alias = alias
    }

    fun password(password: String) {
        this.password = password
    }
}