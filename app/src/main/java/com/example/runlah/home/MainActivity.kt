package com.example.runlah.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.runlah.R
import com.example.runlah.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navigateToFragment()
    }

    fun navigateToFragment() {
        val navController = findNavController(R.id.host_fragment)
        val bottomNav = binding.bottomNav
        NavigationUI.setupWithNavController(bottomNav, navController)
    }
}