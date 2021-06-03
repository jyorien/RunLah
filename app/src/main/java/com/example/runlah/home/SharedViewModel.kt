package com.example.runlah.home

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.runlah.dashboard.Record
import com.example.runlah.util.DateUtil
import com.example.runlah.util.Tips
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import java.time.LocalDateTime
import kotlin.math.truncate
const val stepKey = "steps"
const val distanceKey = "distance"
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

    private val _todayMap = MutableLiveData<HashMap<String, Any>>()
    val todayMap: LiveData<HashMap<String, Any>>
        get() = _todayMap

    private val _isDeleted = MutableLiveData(false)
    val isDeleted: LiveData<Boolean>
    get() = _isDeleted

    private val _isSingleDeleted = MutableLiveData(false)
    val isSingleDeleted: LiveData<Boolean>
        get() = _isSingleDeleted

    init {
        getHistoryData()
    }

    private fun getHistoryData() {
        // populate dashboard list data
        val docRef =
            mFirestore.collection("users").document(mAuth.currentUser!!.uid).collection("records")
        docRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { documentSnapshot, e ->
                // if error, return
                if (e != null) {
                    return@addSnapshotListener
                }
                if ( documentSnapshot == null || documentSnapshot.isEmpty) {
                    val emptyArrayList = arrayListOf<Record>()
                    _recordList.value = emptyArrayList
                    getTodayData(emptyArrayList)
                    getWeeklyDistance(emptyArrayList)
                    return@addSnapshotListener
                }

                // if update comes locally, don't update
                if (documentSnapshot.metadata.hasPendingWrites()) return@addSnapshotListener

                val recordList = arrayListOf<Record>()

                documentSnapshot.forEach { document ->
                    val docData = document.data
                    val docId = document.id
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
                        coordinatesArray =
                            givenCoordinatesArray as ArrayList<HashMap<String, Double>>
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

                    var givenSteps = docData["stepCount"].toString()

                    lateinit var steps: String

                    if (givenSteps == "null") {
                        givenSteps = "0"
                        steps = "0 steps"
                    } else {
                        steps = "${givenSteps.substringBefore(".")} steps"
                    }

                    val uuid = docData["uuid"].toString()
                    val record = Record(
                        docId,
                        displayDate,
                        distance,
                        timeTaken,
                        speed,
                        steps,
                        latLngArray,
                        uuid,
                        date,
                        rawDistance,
                        givenSteps.substringBefore(".").toInt()
                    )
                    recordList.add(record)

                }
                _recordList.value = recordList
                getTodayData(recordList)
                getWeeklyDistance(recordList)

            }

    }

    fun deleteUserRecords() {
        val docRef =
            mFirestore.collection("users").document(mAuth.currentUser!!.uid).collection("records")
        docRef.get().addOnSuccessListener { snapshot ->
            snapshot.forEach { document ->
                Log.i("hello", " deleted ${document.id}")
                document.reference.delete()
            }
            _isDeleted.value = true
        }
    }

    fun onCompleteDelete() {
        _isDeleted.value = false
    }

    fun deleteSingleUserRecord(docId: String) {
        val docRef =
            mFirestore.collection("users").document(mAuth.currentUser!!.uid).collection("records").document(docId)
        docRef.delete().addOnSuccessListener {
            _isSingleDeleted.value = true
        }
    }

    fun onCompleteSingleDelete() {
        _isSingleDeleted.value = false
    }

    private fun getWeeklyDistance(givenList: ArrayList<Record>) {
        // populate bar chart data

        // sort dates in chronological order
        val list = givenList.sortedBy { it.rawDate }
        val distanceMap = hashMapOf<Int, Double>()
        val xAxisList = arrayListOf<Int>()
        // loop through each entry
        list.forEach { record ->
            // entry must at most be 7 days old
            if (record.rawDate.isAfter(currentDate.minusDays(6))) {

                val key = record.rawDate.dayOfMonth

                if (distanceMap[key] != null) {
                    distanceMap[key] = distanceMap[key]!!.plus(record.rawDistance)
                } else {
                    distanceMap[key] = record.rawDistance
                }
            }
        }
        _weeklyDistanceMap.value = distanceMap.toSortedMap(reverseOrder())

        // populate x axis list
        var dateCount = currentDate.minusDays(6)
        while (!dateCount.isAfter(currentDate)) {
            xAxisList.add(dateCount.dayOfMonth)
            dateCount = dateCount.plusDays(1)
        }
        _xAxisValues.value = xAxisList
    }

    private fun formatMinutes(minutes: String): String {
        var minute = minutes
        if (minutes.toInt() < 10)
            minute = "0$minutes"
        return minute
    }

    private fun getTodayData(givenList: ArrayList<Record>) {
        val todayMap = hashMapOf<String, Any>()
        givenList.forEach { record ->
            if (record.rawDate.dayOfMonth == currentDate.dayOfMonth && record.rawDate.year == currentDate.year && record.rawDate.monthValue == currentDate.monthValue) {
                if (todayMap[stepKey] == null)
                    todayMap[stepKey] = record.rawSteps
                else
                    todayMap[stepKey] = (todayMap[stepKey] as Int) + record.rawSteps

                if (todayMap[distanceKey] == null)
                    todayMap[distanceKey] = record.rawDistance
                else
                    todayMap[distanceKey] =
                        (todayMap[distanceKey] as Double).plus(record.rawDistance)
            }
        }
        _todayMap.value = todayMap

    }

}