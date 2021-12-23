package io.github.quantones.harpocrate.jnisecret.utils

object CMakeListsUtils {

    private const val cppDirPathHolder = "%cpp_dir_path_holder%"
    private const val cppFilenameHolder = "%cpp_file_name_holder%"
    private const val libNameHolder = "%lib_name%"
    private const val base64HeaderHolder = "%base_64_h%"
    private const val base64CppHolder = "%base_64_cpp%"

    private val cMakeListsContentSimple = """
        cmake_minimum_required(VERSION 3.4.1)
        add_library($libNameHolder SHARED $cppDirPathHolder$cppFilenameHolder)
        find_library(log-lib log)
        target_link_libraries($libNameHolder ${"$"}{log-lib} )
    """.trimIndent()

    private val cMaleListsContentOpenSSL = """
        cmake_minimum_required(VERSION 3.4.1)
        project(include_openssl_crypto)
        find_package(openssl REQUIRED CONFIG)
        add_library($libNameHolder SHARED
                $cppDirPathHolder$base64HeaderHolder
                $cppDirPathHolder$base64CppHolder
                $cppDirPathHolder$cppFilenameHolder
                )
        find_library(log-lib log)
        find_library(android-lib android)
        target_link_libraries(
                $libNameHolder
                PUBLIC         ${"$"}{log-lib}
                PUBLIC         ${"$"}{android-lib}
                PRIVATE openssl::crypto
        )
    """.trimIndent()

    private fun getFileContentSimple(libName: String, cppDirPath: String, cppFilename: String): String {
        return cMakeListsContentSimple
            .replace(libNameHolder, libName)
            .replace(cppDirPathHolder, cppDirPath)
            .replace(cppFilenameHolder, cppFilename)
    }

    private fun getFileContentOpenSSL(libName: String, cppDirPath: String, cppFilename: String): String {
        return cMaleListsContentOpenSSL
            .replace(libNameHolder, libName)
            .replace(cppDirPathHolder, cppDirPath)
            .replace(cppFilenameHolder, cppFilename)
            .replace(base64HeaderHolder, CPPBase64Utils.HEADER_NAME)
            .replace(base64CppHolder, CPPBase64Utils.CPP_NAME)
    }

    fun getFileContent(type: StoringType, libName: String, cppDirPath: String, cppFilename: String): String {
        return when(type) {
            StoringType.BASIC, StoringType.OBFUSCATED -> getFileContentSimple(libName, cppDirPath, cppFilename)
            StoringType.PKCS12 -> getFileContentOpenSSL(libName, cppDirPath, cppFilename)
        }
    }

}