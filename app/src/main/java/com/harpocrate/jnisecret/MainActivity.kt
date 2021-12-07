package com.harpocrate.jnisecret

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

//import com.harpocrate.sample.JniSecret

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //println(JniSecret().key())
        //Log.d("MainActivity", "Secret key ${JniSecret().key()}")
    }
}