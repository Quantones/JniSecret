package io.github.quantones.harpocrate.jnisecret.configuration

import io.github.quantones.harpocrate.jnisecret.utils.Config
import groovy.lang.Closure
import io.github.quantones.harpocrate.jnisecret.utils.StoringType
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

open class JniSecretConfiguration(
    val productFlavors: NamedDomainObjectContainer<JniSecretEntries>
) {

    var packagename: String = "com.harpocrate.secret"
    var className: String = "SecretKeys"
    var generateCMake: Boolean = false
    var defaultConfig: JniSecretEntries = JniSecretEntries(Config.DEFAULT_CONFIG_NAME)
    var cppStoringType: StoringType = StoringType.OBFUSCATED

    fun packageName(name: String) {
        this.packagename = name
    }

    fun getPackageName(): String {
        return this.packagename
    }

    fun getStoringType(): StoringType {
        return cppStoringType
    }

    fun className(name: String) {
        this.className = name
    }

    fun productFlavors(configureClosure: Closure<*>) {
        this.productFlavors.configure(configureClosure)
     }

    fun generateCMake(generate: Boolean) {
        this.generateCMake = generate
    }

    fun defaultConfig(config: Action<JniSecretEntries>) {
        config.execute(this.defaultConfig)
    }

    fun storingType(type: String) {
        this.cppStoringType = StoringType.getByValue(type)
    }


}