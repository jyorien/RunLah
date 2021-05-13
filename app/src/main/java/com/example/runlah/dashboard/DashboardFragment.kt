package com.example.runlah.dashboard

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runlah.R
import com.example.runlah.databinding.FragmentDashboardBinding
import com.example.runlah.home.MainActivity
import com.example.runlah.util.DateUtil
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var recordList: ArrayList<Record>
    private lateinit var binding: FragmentDashboardBinding
    private val weeklyDistanceList = arrayListOf<Distance>()
    private val weeklyTotalDistanceMap = hashMapOf<Int, Float>()
    private lateinit var currentDate: LocalDateTime
    private val xAxisValues = arrayListOf<Int>()
    private var MIN_X_VALUE = 0
    private var MAX_X_VALUE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // hide up button
        (activity as MainActivity).supportActionBar!!.title = "Dashboard"
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility =
            View.VISIBLE
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        currentDate = LocalDateTime.now()
        getHistoryData()
        return binding.root
    }

    fun getHistoryData() {
        val firestore = FirebaseFirestore.getInstance()
        recordList = arrayListOf()
        val docRef =
            firestore.collection("users").document(auth.currentUser!!.uid).collection("records")
        docRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot == null)
                    return@addOnSuccessListener

                documentSnapshot.forEach { document ->
                    val docData = document.data
                    val time = docData["timestamp"] as Timestamp
                    val date = DateUtil.getDateInLocalDateTime(time)
                    val minute = formatMinutes(date.minute.toString())
                    val displayDate =
                        "${date.dayOfMonth} ${date.month} ${date.year} ${date.hour}:${minute}"

                    var distance = "${docData["distanceTravelled"]}"

                    if (date.isAfter(currentDate.minusDays(7))) {
                        weeklyDistanceList.add(Distance(distance.toFloat(), date))
                    }
                    if (distance == "null") distance = "0.00"
                    else distance = (distance.toFloat() / 1000).toString()
                    distance = "${String.format("%.2f", distance.toFloat())} km"


                    var timeTaken = "${docData["timeTaken"]}"
                    if (timeTaken == "null") timeTaken = "00:00"

                    var speed = "${docData["averageSpeed"]}"
                    if (speed == "null") speed = "0 m/s"
                    else speed = "${String.format("%.2f", speed.toFloat())} m/s"
                    val givenCoordinatesArray = docData["coordinatesArray"]
                    var coordinatesArray = arrayListOf<HashMap<String, Double>>()
//                    val coordinatesArray = docData["coordinatesArray"]
                    if (givenCoordinatesArray is HashMap<*, *>) {
                        givenCoordinatesArray.forEach {
                            coordinatesArray.add(it.value as HashMap<String, Double>)
                        }
                    } else
                        coordinatesArray = givenCoordinatesArray as ArrayList<HashMap<String, Double>>
                    // cast to arraylist so can iterate through
                    val latLngArray = arrayListOf<LatLng>()
                    (coordinatesArray).forEach { coordinates ->
                        latLngArray.add(
                            LatLng(
                                coordinates["latitude"]!!,
                                coordinates["longitude"]!!
                            )
                        )
                    }
                    var givenSteps = docData["stepCount"]
                    var steps = ""
                    // if data from flutter app, type is long
                    if (givenSteps is Long)
                        steps = "${givenSteps.toDouble().toInt()} steps"
                    // if data from android app, type is double
                    else
                        steps = "${(givenSteps as Double).toInt()} steps"
                    if (steps == "null steps") steps = "0 steps"

                    val uuid = docData["uuid"].toString()
                    val record = Record(
                        displayDate,
                        distance,
                        timeTaken,
                        speed,
                        steps,
                        latLngArray,
                        uuid
                    )
                    recordList.add(record)

                }
                for (i in 6 downTo 0) {
                    xAxisValues.add(currentDate.minusDays(i.toLong()).dayOfMonth)
                }
                MIN_X_VALUE = xAxisValues[0]
                populateWeeklyTotalDistanceMap()
                val data = getChartData()
                getChartAppearance()
                prepareChartData(data)
                binding.historyList.adapter = HistoryListAdapter(recordList) { record ->
                    val action =
                        DashboardFragmentDirections.actionDashboardFragmentToHistoryFragment(record)
                    findNavController().navigate(action)
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


    private fun populateWeeklyTotalDistanceMap() {
        weeklyDistanceList.sortBy { it.date }

        val groupedWeeklyDistanceList = weeklyDistanceList.groupBy { it.date.dayOfMonth }
        groupedWeeklyDistanceList.forEach { (key, value) ->
            var totalDistance = 0.0F
            value.forEach { distance ->
                totalDistance += distance.distance
            }
            weeklyTotalDistanceMap[key] = totalDistance
        }
        Log.i("hello", "map $weeklyTotalDistanceMap")

    }

    val label = "Distance (m)"
    private fun getChartAppearance() {
        binding.barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = value.toInt().toString()

        }
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.description.isEnabled = false
        binding.barChart.axisLeft.granularity = 10f
        binding.barChart.axisLeft.axisMinimum = 0f
        binding.barChart.xAxis.textColor =
            ContextCompat.getColor(requireContext(), R.color.invertedTextColor)
        binding.barChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.invertedTextColor)
        binding.barChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.invertedTextColor)
    }

    private fun getChartData(): BarData {
        val values = arrayListOf<BarEntry>()
        MIN_X_VALUE = xAxisValues.first().toInt()
        MAX_X_VALUE = xAxisValues.last().toInt()
        for (i in MIN_X_VALUE..MAX_X_VALUE) {
            var coordinates = BarEntry(i.toFloat(), 0f)
            if (weeklyTotalDistanceMap.keys.contains(i)) {
                val y = weeklyTotalDistanceMap[i]

                coordinates = BarEntry(i.toFloat(), (y!!).roundToInt().toFloat())
            }
            values.add(coordinates)
        }
        val set1 = BarDataSet(values, label)
        val datasets = arrayListOf<IBarDataSet>()
        datasets.add(set1)
        return BarData(datasets)
    }

    private fun prepareChartData(data: BarData) {
        data.setValueTextSize(12f)
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.invertedTextColor))
        binding.barChart.data = data
        binding.barChart.animateY(500)
        binding.barChart.invalidate()
    }

}