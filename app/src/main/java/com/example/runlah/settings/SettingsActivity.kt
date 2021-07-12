package com.example.runlah.settings

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.runlah.R
import com.example.runlah.databinding.ActivitySettingsBinding
import com.example.runlah.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        val sharedPref = this.getSharedPreferences(getString(R.string.dark_mode_string), MODE_PRIVATE)
        val isDark = sharedPref.getBoolean(getString(R.string.dark_mode_string), false)
        binding.darkModeSwitch.isChecked = isDark
        binding.darkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPref.edit().putBoolean(getString(R.string.dark_mode_string), true).apply()
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPref.edit().putBoolean(getString(R.string.dark_mode_string),false).apply()
            }
            Log.i("hello", sharedPref.getBoolean(getString(R.string.dark_mode_string), false).toString())
        }
        binding.logOut.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Intent(this, LoginActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}