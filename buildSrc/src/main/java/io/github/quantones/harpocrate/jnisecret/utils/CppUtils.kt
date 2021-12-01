package io.github.quantones.harpocrate.jnisecret.utils

object CppUtils {

    private const val packageNameHolder = "%package_name%"
    private const val classNameHolder = "%class_name%"
    private const val keyHolder = "%key%"
    private const val valueHolder = "%value%"
    private const val alphaAddHolder = "%alpha_add%"

    private val cppHeaders = """
        #include <jni.h>
        #include <string>
    """.trim()

    private val cppAlphaClass = """
        class Alpha : public std::string
        {
        public:
            Alpha(std::string str)
            {
                std::string phrase(str.c_str(), str.length());
                this->assign(phrase);
            }
            Alpha c(char c) {
                std::string phrase(this->c_str(), this->length());
                phrase += c;
                this->assign(phrase);
        
                return *this;
            }
        };
    """.trim()

    private val cppStringFunction = """
        extern "C" JNIEXPORT jstring
        JNICALL
        Java_${packageNameHolder}_${classNameHolder}_$keyHolder(JNIEnv *env, jobject object) {
            Alpha str("");
            std::string value = "$valueHolder";
            return env->NewStringUTF(value.c_str());
        }
    """.trim()

    private val cppAlphaFunction = """
        extern "C" JNIEXPORT jstring
        JNICALL
        Java_${packageNameHolder}_${classNameHolder}_$keyHolder(JNIEnv *env, jobject object) {
            Alpha str("");
            std::string myStr = str.$alphaAddHolder;
            return env->NewStringUTF(myStr.c_str());
        }
    """.trim()

    fun transformPackageName(packageName: String): String {
        return packageName.replace('.', '_').toLowerCase()
    }

    fun transformClassName(className: String): String {
        return className[0].toUpperCase()+className.substring(1)
    }

    fun getCppFunction(packageName: String, className: String, key: String, value: String): String {
        return cppStringFunction
            .replace(packageNameHolder, packageName)
            .replace(classNameHolder, className)
            .replace(keyHolder, key)
            .replace(valueHolder, value)
    }


    fun getCppAlphaFunction(packageName: String, className: String, key: String, value: String): String {
        return cppAlphaFunction
            .replace(packageNameHolder, packageName)
            .replace(classNameHolder, className)
            .replace(keyHolder, key)
            .replace(alphaAddHolder, value)
    }

    fun getCppBasicContent(packageName: String, className: String, keyValues: List<Pair<String, String>>): String {
        return """
            $cppHeaders
            ${
            keyValues.joinToString("\n") {
                getCppFunction(packageName, className, it.first, it.second)
            }
        }
        """.trimIndent()
    }


    fun getCppObfuscatedContent(packageName: String, className: String, keyValues: List<Pair<String, String>>): String {
        val byteArray = keyValues
            .map {
                val byteArrayString =
                    it.second
                        .map { char ->
                            "c('$char')"
                        }
                        .joinToString(".")
                Pair(it.first, byteArrayString)
            }

        return """
                $cppHeaders
                $cppAlphaClass
                ${
                    byteArray.joinToString("\n") {
                    getCppAlphaFunction(packageName, className, it.first, it.second)
                    }
                }
        """.trimIndent()
    }

    fun getCppContent(packageName: String, className: String, keyValues: List<Pair<String, String>>, type: StoringType): String {
        return if(type == StoringType.BASIC) {
            getCppBasicContent(packageName, className, keyValues)
        } else {
            getCppObfuscatedContent(packageName, className, keyValues)
        }
    }

}