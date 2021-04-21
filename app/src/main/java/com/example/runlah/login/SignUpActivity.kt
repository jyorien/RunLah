package com.example.runlah.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.runlah.R
import com.example.runlah.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        // hide action bar
        if (supportActionBar != null)
            this.supportActionBar?.hide()

        binding.navigateToLogin.setOnClickListener { finish() }
        auth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener {
            val email = binding.signupEmailInput.text.toString()
            val password = binding.signupPasswordInput.text.toString()
            val confirmPassword = binding.signupConfirmPasswordInput.text.toString()
            createAccount(email, password, confirmPassword)
        }

    }

    private fun createAccount(email: String, password: String, confirmPassword: String) {
        if (!validateCredentials(email, password, confirmPassword))
            return

        binding.progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                Snackbar.make(binding.root, "Sign Up Successful!", Snackbar.LENGTH_SHORT).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onShown(transientBottomBar: Snackbar?) {
                        super.onShown(transientBottomBar)
                        Thread.sleep(300)
                        finish()
                    }
                }).show()

            }
            else if (task.exception is FirebaseAuthUserCollisionException)
                binding.signupEmail.error = "Email is already taken"

            else
                Snackbar.make(binding.root, "hi ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun validateCredentials(email: String, password: String, confirmPassword: String): Boolean {
        binding.signupEmail.isErrorEnabled = false
        binding.signupPassword.isErrorEnabled = false
        binding.signupConfirmPassword.isErrorEnabled = false

        if (email.isEmpty()) {
            binding.signupEmail.error = "Email required"
            binding.signupEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.signupPassword.error = "Password required"
            binding.signupPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            binding.signupPassword.error = "Password must have at least 6 characters"
            return false
        }
        if (password != confirmPassword) {
            binding.signupPassword.error = "Passwords do not match!"
            binding.signupConfirmPassword.error = "Passwords do not match"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmail.error = "Please enter a valid email address"
            binding.signupEmail.requestFocus()
            return false
        }
        return true

    }
}