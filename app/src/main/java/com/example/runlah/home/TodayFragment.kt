package com.example.runlah.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.runlah.R
import com.example.runlah.databinding.FragmentTodayBinding
import com.example.runlah.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_today, container, false)
        (activity as MainActivity).supportActionBar!!.title = "Today's activity"
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        binding.logOut.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Intent(requireActivity(), LoginActivity::class.java).also {  intent ->
                startActivity(intent)
            }
        }
        return binding.root
    }


}