package com.example.runlah.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.runlah.R
import com.example.runlah.databinding.FragmentRecordBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.lang.NullPointerException
import java.util.*
val zoomLevel = 20F
class RecordFragment : Fragment(), SensorEventListener {
    private val MULTIPLE_PERMISSION_REQUEST = 1
    private lateinit var binding: FragmentRecordBinding
    private lateinit var map: GoogleMap
    private lateinit var client: FusedLocationProviderClient
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private lateinit var coordinates: LatLng
//    val timer = Timer()
    private var isStarted = false
    private lateinit var stepCounterSensor: Sensor
    private lateinit var sensorManager: SensorManager

    // step count since system reboot
    private var totalStepCount = 0F
    // step count when 'start' button clicked
    private var startStepCount = 0F
    // total step count for the session
    private var sessionStepCount = 0F
        get() = totalStepCount - startStepCount

    // total distance covered
    private var sessionDistance = 0F
    // store current location data
    private lateinit var currentLocation: Location

    private lateinit var locationRequest: LocationRequest
    private val latLngList = arrayListOf<LatLng>()
    private val floatLatLngList = arrayListOf<Float>()
    private val speedList = arrayListOf<Float>()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach { location ->
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                // update current location on map
                coordinates = LatLng(currentLatitude, currentLongitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel))
                // user starts running
                if (isStarted) {
                    // distance from previous location to new location
                    sessionDistance+= currentLocation.distanceTo(location)
                    binding.distanceTravelled.text = String.format("%.2f", (sessionDistance/1000))
                    // populate list to draw polyline
                    floatLatLngList.add(currentLatitude.toFloat())
                    floatLatLngList.add(currentLongitude.toFloat())
                    latLngList.add(coordinates)
                    // populate list to keep track of speed
                    val currentSpeed = location.speed
                    speedList.add(currentSpeed)
                    binding.currentSpeed.text = String.format("%.2f", currentSpeed)
                    // draw user's path with polyline
                    val polyLineOptions = PolylineOptions().addAll(latLngList).clickable(true)
                        .color(ContextCompat.getColor(requireContext(), R.color.polyLineBlue))
                    map.addPolyline(polyLineOptions)
                }
                // update curent location data
                currentLocation = location

            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // ping every 4s
        locationRequest = LocationRequest.create()
        locationRequest.interval = 4000
        // can handle requests coming in 2s intervals
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_record, container, false)
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment
        supportMapFragment.getMapAsync { googleMap ->
            // on map ready
            map = googleMap
            requestPermissions()
        }

        binding.btnStartStop.setOnClickListener {
            if (!isStarted) {
                isStarted = true
                checkSettingsAndStartLocationUpdates()
//                startTimer()
                binding.apply {
                    btnStartStop.text = "STOP"
                    startStepCount = totalStepCount
                    chronometer.base = SystemClock.elapsedRealtime()
                    chronometer.start()
                }

            } else {
//                timer.cancel()
                binding.chronometer.stop()
                stopLocationUpdates()
                var averageSpeed = 0.0
                speedList.forEach { speed ->
                    averageSpeed+=speed
                }
                averageSpeed /= speedList.size
                // send data to results page
                val action = RecordFragmentDirections.actionRecordFragmentToResultsFragment(
                    floatLatLngList.toFloatArray(),
                    binding.chronometer.text.toString(),
                    sessionStepCount,
                    averageSpeed.toFloat(),
                    sessionDistance
                )
                findNavController().navigate(action)
            }

        }

        return binding.root
    }

    private fun checkSettingsAndStartLocationUpdates() {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener {
            // device settings satisfied
            startLocationUpdates()
        }
        locationSettingsResponseTask.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                e.startResolutionForResult(requireActivity(), 1001)
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback)
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // if permission granted
            registerStepTrackerListener()
            map.isMyLocationEnabled = true
//            getLastLocation()
            checkSettingsAndStartLocationUpdates()
        } else {
            // API Level 28 (Android Pie) and below auto grants activity recognition permission so no pop up.
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                MULTIPLE_PERMISSION_REQUEST
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MULTIPLE_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                registerStepTrackerListener()
                map.isMyLocationEnabled = true
//                getLastLocation()
                checkSettingsAndStartLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun registerStepTrackerListener() {
        try {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepCounterSensor.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
        } catch (e: NullPointerException) {
            Log.e("HELLO", e.toString())
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        // return if null
        event ?: return
        // first value in SensorEvent is step count
        event.values.firstOrNull().let {
            Log.i("HELLO", "step count $it")
            if (it != null)
                totalStepCount = it
            if (isStarted)
                binding.stepCount.text = sessionStepCount.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor != null) {
            Log.i("HELLO", "accuracy: $accuracy")
        }
    }

    //    @SuppressLint("MissingPermission")
//    private fun getLastLocation() {
//        client.lastLocation.addOnSuccessListener {
//            currentLatitude = it.latitude
//            currentLongitude = it.longitude
//            coordinates = LatLng(currentLatitude, currentLongitude)
//            floatLatLngList.add(currentLatitude.toFloat())
//            floatLatLngList.add(currentLongitude.toFloat())
//            latLngList.add(coordinates)
//            val zoomLevel = 15F
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel))
//        }
//    }

    //    @SuppressLint("MissingPermission")
//    private fun startTimer() {
//        val timerTask = object : TimerTask() {
//            override fun run() {
//                client.lastLocation.addOnSuccessListener {
//                    getLastLocation()
//                    val polyLineOptions = PolylineOptions().addAll(latLngList).clickable(true)
//                        .color(ContextCompat.getColor(requireContext(), R.color.polyLineBlue))
//                    map.addPolyline(polyLineOptions)
//                    Log.i("HELLO", "${it.latitude} ${it.longitude}")
//
//                }
//            }
//        }
//        timer.schedule(timerTask, 2500, 2500)
//    }


}