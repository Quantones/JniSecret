package io.github.quantones.harpocrate.jnisecret.utils

object JniInterfaceUtils {

    const val packageNameHolder = "%package_name%"
    const val classNameHolder = "%class_name%"
    const val libraryNameHolder = "%library_name%"
    const val functionsHolder = "%functions%"
    const val keyHolder = "%key%"

    private val interfaceContent = """
        package $packageNameHolder

        public class $classNameHolder {
            init
            {
                System.loadLibrary("$libraryNameHolder")
            }
            $functionsHolder
        }
    """.trimIndent()

    private val jniFunction = """
        external fun $keyHolder(): String
    """.trimIndent()

    fun getJniFunction(key: String): String {
        return jniFunction.replace(keyHolder, key)
    }

    fun getJniInterface(pacakgeName: String, className: String, libraryName: String, functions: String): String {
        return interfaceContent
            .replace(packageNameHolder, pacakgeName)
            .replace(classNameHolder, className)
            .replace(libraryNameHolder, libraryName)
            .replace(functionsHolder, functions)
    }

}