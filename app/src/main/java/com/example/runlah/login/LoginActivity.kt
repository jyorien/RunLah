package com.example.runlah.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.runlah.home.MainActivity
import com.example.runlah.R
import com.example.runlah.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Intent(this, MainActivity::class.java).also { intent ->
                finish()
                startActivity(intent)
            }
        }
        // hide action bar
        if (supportActionBar != null)
            this.supportActionBar?.hide()
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding.btnLogin.setOnClickListener {
            val email = binding.loginEmailInput.text.toString()
            val password = binding.loginPasswordInput.text.toString()
            login(email, password)
        }

        binding.navigateToSignup.setOnClickListener {
            Intent(this, SignUpActivity::class.java).also { intent ->
                startActivity(intent)
            }
        }
    }

    private fun login(email: String, password: String) {
        binding.progressBar2.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.progressBar2.visibility = View.GONE
            if (task.isSuccessful) {
                Intent(this, MainActivity::class.java).also {
                    finish()
                    startActivity(it)
                }

            } else
                Snackbar.make(binding.root, "${task.exception?.message}", Snackbar.LENGTH_LONG)
                    .show()
        }
    }
}