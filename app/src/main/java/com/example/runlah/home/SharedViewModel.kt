package com.example.runlah.home

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.runlah.dashboard.Distance
import com.example.runlah.dashboard.Record
import com.example.runlah.util.DateUtil
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import java.time.LocalDateTime

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _recordList = MutableLiveData<ArrayList<Record>>()
    val recordList: LiveData<ArrayList<Record>>
        get() = _recordList

    private val _weeklyDistanceMap = MutableLiveData<Map<Int, Double>>()
    val weeklyDistanceMap: LiveData<Map<Int, Double>>
    get() = _weeklyDistanceMap

    private val _xAxisValues = MutableLiveData<ArrayList<Int>>()
    val xAxisValues: LiveData<ArrayList<Int>>
    get() = _xAxisValues

    private val currentDate = LocalDateTime.now()

    init {
        getHistoryData()
    }
    private fun getHistoryData() {

        val docRef =
            mFirestore.collection("users").document(mAuth.currentUser!!.uid).collection("records")
        docRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE)  { documentSnapshot, e ->

                // if error, return
                if (e != null || documentSnapshot == null || documentSnapshot.isEmpty) {
                    return@addSnapshotListener
                }

                // if update comes locally, don't update
                if (documentSnapshot.metadata.hasPendingWrites()) return@addSnapshotListener

                val recordList = arrayListOf<Record>()

                documentSnapshot.forEach { document ->
                    val docData = document.data
                    Log.i("hello","timestamp ${docData["timestamp"]}")
                    val time = docData["timestamp"] as Timestamp
                    val date = DateUtil.getDateInLocalDateTime(time)
                    val minute = formatMinutes(date.minute.toString())
                    val displayDate =
                        "${date.dayOfMonth} ${date.month} ${date.year} ${date.hour}:${minute}"
                    val rawDistance = docData["distanceTravelled"] as Double
                    var distance = "$rawDistance"

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
                    val givenSteps = docData["stepCount"]
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
                        uuid,
                        date,
                        rawDistance
                    )
                    recordList.add(record)

                }
                _recordList.value = recordList
                getWeeklyDistance(recordList)

            }

    }

    private fun getWeeklyDistance(givenList: ArrayList<Record>) {
        val list = givenList.sortedBy { it.rawDate }
        val distanceMap = hashMapOf<Int, Double>()
        val xAxisList = arrayListOf<Int>()
        list.forEach { record ->
            if (record.rawDate.isAfter(currentDate.minusDays(6))) {

                val key = record.rawDate.dayOfMonth

                if (distanceMap[key] != null) {
                    Log.i("hello","help plus ${record.rawDate} ${record.rawDistance}")
                    distanceMap[key] = distanceMap[key]!!.plus(record.rawDistance)
                } else {
                    Log.i("hello","help assign ${record.rawDate} ${record.rawDistance}")
                    distanceMap[key] = record.rawDistance
                }
            }
        }
        Log.i("hello","hello $distanceMap")
        _weeklyDistanceMap.value = distanceMap.toSortedMap(reverseOrder())

        // populate x axis list
        for (i in currentDate.minusDays(6).dayOfMonth..currentDate.dayOfMonth) {
            xAxisList.add(i)
        }
        _xAxisValues.value = xAxisList

    }


    private fun formatMinutes(minutes: String): String {
        var minute = minutes
        if (minutes.toInt() < 10)
            minute = "0$minutes"
        return minute
    }
}