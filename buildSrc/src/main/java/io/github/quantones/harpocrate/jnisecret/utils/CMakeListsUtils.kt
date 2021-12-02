package io.github.quantones.harpocrate.jnisecret.utils

object CMakeListsUtils {

    private const val cppDirPathHolder = "%cpp_dir_path_holder%"
    private const val cppFilenameHolder = "%cpp_file_name_holder%"
    private const val libNameHolder = "%lib_name%"

    private val cMakeListsContent = """
        cmake_minimum_required(VERSION 3.4.1)
        add_library($libNameHolder SHARED $cppDirPathHolder/$cppFilenameHolder)
        find_library(log-lib log)
        target_link_libraries($libNameHolder ${"$"}{log-lib} )
    """.trimIndent()

    fun getFileContent(libName: String, cppDirPath: String, cppFilename: String): String {
        return cMakeListsContent
            .replace(libNameHolder, libName)
            .replace(cppDirPathHolder, cppDirPath)
            .replace(cppFilenameHolder, cppFilename)
    }

}