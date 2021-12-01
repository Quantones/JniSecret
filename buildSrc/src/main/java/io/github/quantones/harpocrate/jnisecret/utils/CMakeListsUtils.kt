package io.github.quantones.harpocrate.jnisecret.utils

object CMakeListsUtils {

    private const val cppDirPathHolder = "%cpp_dir_path_holder%"
    private const val cppDirNameHolder = "%cpp_dir_name_holder%"
    private const val cppFilenameHolder = "%cpp_file_name_holder%"
    private const val environmentHolder = "%env%"

    private val cMakeListsContent = """
        cmake_minimum_required(VERSION 3.4.1)
        add_library(${Config.SO_LIB_NAME} SHARED $cppDirPathHolder/$cppFilenameHolder)
        find_library(log-lib log)
        target_link_libraries(${Config.SO_LIB_NAME} ${"$"}{log-lib} )
    """.trimIndent()

    fun getFileContent(cppDirPath: String, cppFilename: String): String {
        return cMakeListsContent
            .replace(cppDirPathHolder, cppDirPath)
            .replace(cppFilenameHolder, cppFilename)
    }

}