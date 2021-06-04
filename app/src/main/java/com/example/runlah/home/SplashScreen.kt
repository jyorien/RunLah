package com.example.runlah.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runlah.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Intent(this, MainActivity::class.java).also { intent ->
                startActivity(intent)
                finish()
            }
        }
        else {
            Intent(this, LoginActivity::class.java).also { intent ->
                startActivity(intent)
                finish()
            }
        }
    }


}