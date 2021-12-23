package com.harpocrate.jnisecret

import android.content.res.AssetManager

class JniSecretOpenSSL {
  init {
    System.loadLibrary("JniSecret")
  }

  external fun test(manager: AssetManager): String
}
