package com.example.runlah.dashboard

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.runlah.R
import com.example.runlah.databinding.FragmentDashboardBinding
import com.example.runlah.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DashboardFragment : Fragment() {
    val records = arrayListOf(
        Record("26/4/2021", "5 km", "60 min", "5 m/s", "300 steps"),
        Record("26/4/2021", "5 km", "60 min", "5 m/s", "300 steps")
    )
    private lateinit var auth: FirebaseAuth
    private lateinit var recordList: ArrayList<Record>

    private lateinit var binding: FragmentDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // hide up button
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        getHistoryData()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getHistoryData() {
        val firestore = FirebaseFirestore.getInstance()
        recordList = arrayListOf()
        val docRef =
            firestore.collection("users").document(auth.currentUser!!.uid).collection("records")
        docRef.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot == null)
                    return@addOnSuccessListener

                documentSnapshot.forEach { document ->
                    val docData = document.data

                    val time = docData["timestamp"] as com.google.firebase.Timestamp
                    val formatter =
                        DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss O uuuu", Locale.ENGLISH)
                    // parse string to LocalDateTime object
                    val date = LocalDateTime.parse(time.toDate().toString(), formatter)
                    val minute = formatMinutes(date.minute.toString())
                    val displayDate = "${date.dayOfMonth} ${date.month} ${date.year} ${date.hour}:${minute}"

                    var distance = "${docData["distanceTravelled"]}"
                    if (distance == "null") distance = "0.00"
                    else distance = (distance.toFloat() / 1000).toString()
                    distance ="${String.format("%.2f", distance.toFloat())} km"

                    var timeTaken = "${docData["timeTaken"]}"
                    if (timeTaken == "null") timeTaken = "00:00"

                    var speed = "${docData["averageSpeed"]}"
                    if (speed == "null") speed = "0 m/s"
                    else speed = "${String.format("%.2f", speed.toFloat())} m/s"

                    var steps = "${(docData["stepCount"] as Double).toInt()} steps"
                    if (steps == "null steps") steps = "0 steps"
                    val record = Record(
                        displayDate,
                        distance,
                        timeTaken,
                        speed,
                        steps,
                        docData["startLat"].toString(),
                        docData["startLng"].toString(),
                        docData["endLat"].toString(),
                        docData["endLng"].toString()
                    )
                    recordList.add(record)

                }
                binding.historyList.adapter = HistoryListAdapter(recordList) {
                    Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
                }


            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun formatMinutes(minutes: String): String {
        var minute = minutes
        if (minutes.toInt() < 10)
            minute = "0$minutes"
        return minute
    }


}