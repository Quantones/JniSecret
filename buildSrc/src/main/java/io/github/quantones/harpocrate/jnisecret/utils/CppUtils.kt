package io.github.quantones.harpocrate.jnisecret.utils

object CppUtils {

    private const val packageNameHolder = "%package_name%"
    private const val classNameHolder = "%class_name%"
    private const val keyHolder = "%key%"
    private const val valueHolder = "%value%"
    private const val alphaAddHolder = "%alpha_add%"
    private const val pkcs12FilenameHolder = "%pkcs12_filename%"

    private val cppHeaders = """
        #include <jni.h>
        #include <string>
    """.trimIndent()

    private val cppHeadersOpenSSL = """
        #include <android/asset_manager.h>
        #include <android/asset_manager_jni.h>
        #include <openssl/pem.h>
        #include <openssl/err.h>
        #include <openssl/pkcs12.h>
        #include <openssl/rsa.h>
        #include "base64.h"
    """.trimIndent()

    private val cppDefineOpenSSL = """
        #define TAG "$classNameHolder"
        #define PKCS12_NAME "$pkcs12FilenameHolder"
        #define DECRYPTED_LENGTH 4096
    """.trimIndent()

    private val cppGetPkcs12FileFunction = """
        FILE * getPkcs12File(JNIEnv *env, jobject assetManager) {

            AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

            AAsset* asset = AAssetManager_open(mgr, PKCS12_NAME, AASSET_MODE_BUFFER);

            size_t fileLength = AAsset_getLength(asset);

            char* fileContent = new char[fileLength];

            AAsset_read(asset, fileContent, fileLength);

            FILE* file = fmemopen(fileContent, fileLength, "r");

            AAsset_close(asset);

            return file;
        }
    """.trimIndent()

    private val cppGetPkcs12Function = """
        PKCS12 * getPkcs12(JNIEnv * env, jobject assetManager) {

            FILE * file = getPkcs12File(env, assetManager);

            PKCS12 *p12 = d2i_PKCS12_fp(file, nullptr);

            fclose (file);

            return p12;
        }
    """.trimIndent()

    private val cppPassOpenSSLFunction = """
        const char * getPass() {
            Alpha str("");
            std::string myStr = str.$alphaAddHolder;
            return myStr.c_str();
        }
    """.trimIndent()

    private val cppDecryptFunction = """
        unsigned char * decrypt(JNIEnv * env, jobject assetManager, std::string encrypted) {

            OpenSSL_add_all_algorithms();

            ERR_load_crypto_strings();

            EVP_PKEY *pkey;
            X509 *cert;
            STACK_OF(X509) *ca = nullptr;
            PKCS12 * p12 = getPkcs12(env, assetManager);

            if (!p12) {
                return nullptr;
            }
            
            Alpha str("");
            std::string myStr = str.$alphaAddHolder;

            if (!PKCS12_parse(p12, myStr.c_str(), &pkey, &cert, &ca)) {
                return nullptr;
            }

            rsa_st * rsa = EVP_PKEY_get1_RSA(pkey);

            PKCS12_free(p12);
            sk_X509_pop_free(ca, X509_free);
            X509_free(cert);
            EVP_PKEY_free(pkey);

            std::string decoded = base64_decode(encrypted);

            unsigned char decrypted[DECRYPTED_LENGTH]={};

            int result = RSA_private_decrypt(decoded.size(), reinterpret_cast<const unsigned char *>(decoded.data()), decrypted, rsa, RSA_PKCS1_PADDING);

            if(result < 0) {
                return nullptr;
            }

            return decrypted;
        }

    """.trimIndent()

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
    """.trimIndent()

    private val cppStringFunction = """
        extern "C" JNIEXPORT jstring
        JNICALL
        Java_${packageNameHolder}_${classNameHolder}_$keyHolder(JNIEnv *env, jobject object) {
            std::string value = "$valueHolder";
            return env->NewStringUTF(value.c_str());
        }
    """.trimIndent()

    private val cppAlphaFunction = """
        extern "C" JNIEXPORT jstring
        JNICALL
        Java_${packageNameHolder}_${classNameHolder}_$keyHolder(JNIEnv *env, jobject object) {
            Alpha str("");
            std::string myStr = str.$alphaAddHolder;
            return env->NewStringUTF(myStr.c_str());
        }
    """.trimIndent()

    private val cppOpenSSLFunction = """
        extern "C" JNIEXPORT jstring
        JNICALL
        Java_${packageNameHolder}_${classNameHolder}_$keyHolder(JNIEnv *env, jobject object, jobject assetManager) {
            Alpha str("");
            std::string myStr = str.$valueHolder;
            unsigned char * decrypted = decrypt(env, assetManager, myStr.data());
            if(!decrypted) {
                std::string error = "ERROR OCCURED";
                return env->NewStringUTF(error.c_str());
            }
            const char * c = reinterpret_cast<char const *>(decrypted);
            std::string s( c, strlen(c));
            return env->NewStringUTF(s.c_str());
        }
    """.trimIndent()

    fun transformPackageName(packageName: String): String {
        return packageName.replace('.', '_').toLowerCase()
    }

    fun transformClassName(className: String): String {
        return className[0].toUpperCase()+className.substring(1)
    }

    private fun getCppFunction(packageName: String, className: String, key: String, value: String): String {
        return cppStringFunction
            .replace(packageNameHolder, packageName)
            .replace(classNameHolder, className)
            .replace(keyHolder, key)
            .replace(valueHolder, value)
    }


    private fun getCppAlphaFunction(packageName: String, className: String, key: String, value: String): String {
        return cppAlphaFunction
            .replace(packageNameHolder, packageName)
            .replace(classNameHolder, className)
            .replace(keyHolder, key)
            .replace(alphaAddHolder, value)
    }

    private fun getCppOpenSSLFunction(packageName: String, className: String, key: String, value: String): String {
        return cppOpenSSLFunction
            .replace(packageNameHolder, packageName)
            .replace(classNameHolder, className)
            .replace(keyHolder, key)
            .replace(valueHolder, value)
    }

    private fun getCppBasicContent(packageName: String, className: String, keyValues: List<Pair<String, String>>): String {
        return """
            $cppHeaders
            ${
            keyValues.joinToString("\n") {
                getCppFunction(packageName, className, it.first, it.second)
            }
        }
        """.trimIndent()
    }


    private fun getCppObfuscatedContent(packageName: String, className: String, keyValues: List<Pair<String, String>>): String {
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

    private fun getCppOpenSSLContent(packageName: String, className: String, pkcs12: String, password: String, keyValues: List<Pair<String, String>>): String {

        val alphaPass = password
            .map { char -> "c('$char')" }
            .joinToString(".")

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
            $cppHeadersOpenSSL
            
            ${cppDefineOpenSSL
                .replace(classNameHolder, className)
                .replace(pkcs12FilenameHolder, pkcs12)}
                
            $cppAlphaClass
            
            $cppGetPkcs12FileFunction
            
            $cppGetPkcs12Function
            
            ${cppDecryptFunction.replace(alphaAddHolder, alphaPass)}
            
            ${
            byteArray.joinToString("\n") {
                getCppOpenSSLFunction(packageName, className, it.first, it.second)
            }
        }
        """.trimIndent()
    }

    fun getCppContent(packageName: String,
                      className: String,
                      keyValues: List<Pair<String, String>>,
                      type: StoringType,
                      pkcs12: String = "",
                      password: String = ""): String {
        return when(type) {
            StoringType.BASIC -> getCppBasicContent(packageName, className, keyValues)
            StoringType.OBFUSCATED -> getCppObfuscatedContent(packageName, className, keyValues)
            StoringType.PKCS12 -> getCppOpenSSLContent(packageName, className, pkcs12, password, keyValues)
        }
    }

}