package io.github.quantones.harpocrate.jnisecret.configuration

import groovy.lang.Closure
import io.github.quantones.harpocrate.jnisecret.utils.Config
import io.github.quantones.harpocrate.jnisecret.utils.StoringType
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

open class JniSecretConfiguration(
    @Nested val productFlavors: NamedDomainObjectContainer<JniSecretEntries>
) {

    @Input
    var packageName: String = "com.harpocrate.secret"

    @Input
    var className: String = "SecretKeys"

    @Input
    var generateCMake: Boolean = false

    @Nested
    var defaultConfig: JniSecretEntries = JniSecretEntries(Config.DEFAULT_CONFIG_NAME)

    @Input
    var storingType: StoringType = StoringType.OBFUSCATED

    fun productFlavors(configureClosure: Closure<*>) {
        this.productFlavors.configure(configureClosure)
    }

    fun defaultConfig(config: Action<JniSecretEntries>) {
        config.execute(this.defaultConfig)
    }


}