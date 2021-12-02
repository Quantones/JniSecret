
# JniSecret

JniSecret is an Android gradle plugin which help you to store your sensitive data into a native cpp file which will be compile by the NDK. It avoid you to put data like ApiKey or secrets into the BuildConfig class which will be easily readable in your apk.

## Installation

In your root build.gradle:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "io.github.quantones.harpocrate:jni-secret:0.0.2"
  }
}
```

In your project build.gradle:

```
apply plugin: "io.github.quantones.harpocrate.jni-secret"
```

## Under the hood

When you define a jni-secret configuration in your build.gradle project, the plugin will be launch everytime before the preBuild step. You can define a default configuration for all sensitive data shared between your build variants, and override specific values for the variant of your choice.

The plugin take those keys and values and create a cpp file into your src/main folder. After that, a Java interface is created to let your application communicate with c++ library. A final but optional step is the creation of a CMakeLists.txt file into your project folder, it will be added to the CMake path to let the NDK compiler create a .so lib which will be used at runtime.

CMakelists.txt and .cpp file will be ignored at versioning by a .gitingore create in each folder they are.

**TODO**: add the JNI interface into the .gitignore.

**TODO**: merge all .gitignore content into the base .gitignore project.

## Configuration

First step is to store your data in the local.properties to not be versionned:

```
DEFAULT_VALUE_1=defaultValue1
DEV_VALUE_2=devValue2
PROD_VALUE_2=prodValue2
```

Add this in your root build.gradle to easily retrieve properties:

```
private def readProperty(name) {
    def file = project.rootProject.file('local.properties')

    if (file.exists()) {
        def properties = new Properties()
        properties.load(file.newDataInputStream())
        return properties.getProperty(name)
    }

    return null
}
```

In your project build.gradle:

```

android {
    flavorDimensions "ENV"
    productFlavors {
        dev {
            dimension "ENV"
        }
        prod {
            dimension "ENV"
        }
    }
}

jniSecret {
    //
    // The package to create your JNI class
    // If package does not exist, it will be created
    //
    packageName "com.myproject.mypackage"

    //
    // The name of the generated class
    //
    className "MyClass"

    //
    // Optional: auto generate the CMakeLists.txt and add it to the
    // project.android.externalNativeBuild.cmake.path
    // See the Warning below
    //
    generateCMake true

    //
    // How the data will be stored in the cpp file
    //
    storingType "obfuscated"

    //
    // defaultConfig will store data for all variant build
    //
    defaultConfig {
        secret("key_1", readProperty("DEFAULT_VALUE_1"))
    }

    //
    // productFlavors will store data for specific variant
    //
    productFlavors {
        //
        // Variants must be the same as your Android flavors
        //
        dev {
            secret("key_2", readProperty("DEV_VALUE_2"))
        }
        prod {
            secret("key_2", readProperty("PROD_VALUE_2"))
        }
    }
}
```

Then click on *Make project*

As you can see above, the method to call to create a new entry is *secret("key", "value")*. The data in the defaultConfig are vailable for all variants but it could be overrided for only one variant if you need.

**Recommandation**

Choose a non relevant names for your *className* and *key* because each method name will appear in your .so file. It is better to not letting know you are storing secrets here.
Example of methods logs using the command line *strings libMyClass.so*:

```
Java_com_myproject_mypackage_MyClass_key_1
Java_com_myproject_mypackage_MyClass_key_2
```

**WARNING If you already using the NDK in your project**

If you already use the NDK and CMake in your project, remove the generateCMake property. Otherwise the plugin will erase your CMakeLists.txt and broke your project. In that case, you must define by yourself the library in your CMakeLists.txt

## Usage

After configuring your build.gradle, click on Make Project and all items will be generated. You can call the generated class to retrieve your data:

```
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    Log.d("MainActivity", "Key 1 ${MyClass().key_1()}")
    Log.d("MainActivity", "Key 2 ${MyClass().key_2()}")
}
```