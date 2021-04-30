package com.example.runlah.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.runlah.R
import com.example.runlah.dashboard.Record
import com.example.runlah.dashboard.TodayData
import com.example.runlah.databinding.FragmentTodayBinding
import com.example.runlah.login.LoginActivity
import com.example.runlah.util.DateUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
    private lateinit var currentDateTime: LocalDateTime
    private val todayDataMap = hashMapOf("today" to TodayData())


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDateTime = LocalDateTime.now()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_today, container, false)
        (activity as MainActivity).supportActionBar!!.title = "Today's activity"
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        getTodayData()
        binding.logOut.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Intent(requireActivity(), LoginActivity::class.java).also { intent ->
                startActivity(intent)
            }
        }
        binding.button.setOnClickListener {
            if (AppCompatDelegate.MODE_NIGHT_NO == AppCompatDelegate.getDefaultNightMode())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodayData() {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("records")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                // group by day, get today's date only
                val todayRef = todayDataMap["today"]!!
                documentSnapshot.forEach {  document ->
                    val time = document["timestamp"] as Timestamp
                    val recordDateTime = DateUtil.getDateInLocalDateTime(time)
                    if (recordDateTime.dayOfMonth == currentDateTime.dayOfMonth) {
                        val distance = (document["distanceTravelled"] as Double).toFloat()
                        val steps = (document["stepCount"] as Double).toInt()
                        todayRef.distance+= distance
                        todayRef.steps+= steps
                    }
                }
                displayData()
            }
    }

    private fun displayData() {
        val todayRef = todayDataMap["today"]!!
        val stringDistance = "${String.format("%.2f", todayRef.distance/1000)} km"
        val stringSteps = "${todayRef.steps} steps"
        binding.todayDistance.text = stringDistance
        binding.todaySteps.text = stringSteps
    }


}