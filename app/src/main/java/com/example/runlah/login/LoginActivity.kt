package com.example.runlah.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.runlah.home.MainActivity
import com.example.runlah.R
import com.example.runlah.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.view.*

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth
    val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null || account != null) {
            Intent(this, MainActivity::class.java).also { intent ->
                finish()
                startActivity(intent)
            }
        }
        // hide action bar
        if (supportActionBar != null)
            this.supportActionBar?.hide()
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        (binding.googleSignin.getChildAt(0) as TextView).text = "Sign In With Google"
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

        binding.googleSignin.setOnClickListener {
            when (it.id) {
                R.id.google_signin -> googleLogin()

            }
        }
    }

    private fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty())
            return
        binding.progressBar2.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.progressBar2.visibility = View.GONE
            if (task.isSuccessful) {
                Intent(this, MainActivity::class.java).also {
                    finish()
                    startActivity(it)
                }

            } else
                Snackbar.make(binding.root, "${task.exception?.message}", Snackbar.LENGTH_LONG).show()
        }
    }
    private fun googleLogin() {
        binding.progressBar2.visibility = View.VISIBLE
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("345768625554-7gs5b5n5orcefkls5t6sr2pn65t8ajlk.apps.googleusercontent.com")
        .requestEmail()
        .build();
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        binding.progressBar2.visibility = View.GONE
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Snackbar.make(binding.root, "grr ${e.statusCode.toString()}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        binding.progressBar2.visibility = View.GONE
//        try {
//            val account = completedTask.getResult(ApiException::class.java)
//            if (account.is)
//
//        } catch (e: ApiException) {
//            Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_LONG).show()
//        }
//    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Intent(this, MainActivity::class.java).also {
                        finish()
                        startActivity(it)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Snackbar.make(binding.root, "sign in failed ${task.exception.toString()}", Snackbar.LENGTH_LONG).show()
                }
            }
    }
}