package io.github.quantones.harpocrate.jnisecret.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.KeyStore
import java.security.Security
import java.util.*
import javax.crypto.Cipher

object EncryptUtils {

    fun encrypt(assetPath: String, filename: String, alias: String, password: String, text: String): String {
        Security.addProvider(BouncyCastleProvider())

        val certificateFile = File(assetPath, filename)

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(certificateFile.inputStream(), password.toCharArray())

        val certificate = keyStore.getCertificate(alias)

        val publicKey = certificate.publicKey

        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypt = cipher.doFinal(text.toByteArray())

        return Base64.getEncoder().encodeToString( encrypt )
    }

}