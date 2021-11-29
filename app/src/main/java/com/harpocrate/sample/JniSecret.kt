package com.harpocrate.sample

public class JniSecret {
    init
    {
        System.loadLibrary("jni_secret")
    }
    external fun key(): String
	external fun modifyByDev(): String
	external fun shared(): String
}