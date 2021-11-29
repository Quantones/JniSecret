package com.harpocrate.jnisecret

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.harpocrate.sample.JniSecret

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(JniSecret().key())
    }
}