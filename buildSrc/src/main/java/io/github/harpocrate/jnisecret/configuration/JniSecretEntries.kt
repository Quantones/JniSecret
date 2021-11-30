package io.github.harpocrate.jnisecret.configuration

open class JniSecretEntries(val name: String) {

    private var secrets: MutableMap<String, String> = mutableMapOf()

    fun secret(name: String, value: String) {
        secrets[name] = value
    }

    fun getSecrets(): Map<String, String> {
        return secrets
    }

}