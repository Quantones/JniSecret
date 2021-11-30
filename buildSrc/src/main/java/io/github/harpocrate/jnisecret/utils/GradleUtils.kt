package io.github.harpocrate.jnisecret.utils

import org.gradle.api.Project
import java.util.regex.Pattern

object GradleUtils {

    fun getCurrentFlavor(project: Project): String? {
        val gradle = project.gradle
        val tskReqStr = gradle.startParameter.taskRequests.toString()

        val pattern =
            if(tskReqStr.contains("assemble"))
                Pattern.compile("""assemble(\w+})(Release|Debug)""")
            else
                Pattern.compile("""generate(\w+)(Release|Debug)""")


        val matcher = pattern.matcher(tskReqStr)

        return if(matcher.find()) {
            matcher.group(1).toLowerCase()
        } else {
            null
        }
    }

}