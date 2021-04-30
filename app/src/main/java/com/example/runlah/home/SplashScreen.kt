package com.example.runlah.home

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.runlah.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("hello","hello there??")
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Intent(this, MainActivity::class.java).also { intent ->
                Log.i("hello","hello there")
                startActivity(intent)
                finish()
            }
        }
        else {
            Intent(this, LoginActivity::class.java).also { intent ->
                Log.i("hello","hello there")
                startActivity(intent)
                finish()
            }
        }

    }
}