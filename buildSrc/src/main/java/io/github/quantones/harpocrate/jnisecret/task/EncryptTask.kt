package io.github.quantones.harpocrate.jnisecret.task

import io.github.quantones.harpocrate.jnisecret.utils.Config
import io.github.quantones.harpocrate.jnisecret.utils.EncryptUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.security.Security

open class EncryptTask: DefaultTask() {

    @TaskAction
    fun encrypt() {

        Security.addProvider(BouncyCastleProvider())

        val encrypt = EncryptUtils.encrypt("${project.projectDir}${Config.ASSETS_DIR}", "client-identity.p12", "1", "jnisecret", "Hello jni secret")
        val alpha = encrypt.map { "c('$it')" }.joinToString(".")

        /*"jnisecret"*/
    }

}