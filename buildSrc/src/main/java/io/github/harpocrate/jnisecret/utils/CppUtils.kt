package io.github.harpocrate.jnisecret.utils

object CppUtils {

    private const val packageNameHolder = "%package_name%"
    private const val classNameHolder = "%class_name%"
    private const val keyHolder = "%key%"
    private const val valueHolder = "%value%"

    private val cppHeaders = """
        #include <jni.h>
        #include <string>
    """.trimIndent()

    private val cppFunction = """
        extern "C" JNIEXPORT jstring
        JNICALL
        Java_${packageNameHolder}_${classNameHolder}_$keyHolder(JNIEnv *env, jobject object) {
            std::string api_key = "$valueHolder";
            return env->NewStringUTF(api_key.c_str());
        }
    """.trimIndent()

    fun transformPackageName(packageName: String): String {
        return packageName.replace('.', '_').toLowerCase()
    }

    fun transformClassName(className: String): String {
        return className[0].toUpperCase()+className.substring(1)
    }

    fun getCppFunction(packageName: String, className: String, key: String, value: String): String {
        return cppFunction
            .replace(packageNameHolder, packageName)
            .replace(classNameHolder, className)
            .replace(keyHolder, key)
            .replace(valueHolder, value)
    }

    fun getCppContent(packageName: String, className: String, keyValues: List<Pair<String, String>>): String {
        return """
            $cppHeaders
            ${
            keyValues.joinToString("\n") {
                getCppFunction(packageName, className, it.first, it.second)
            }
        }
        """.trimIndent()
    }

}