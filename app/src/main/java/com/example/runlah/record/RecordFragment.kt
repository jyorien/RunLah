package com.example.runlah.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.runlah.R
import com.example.runlah.databinding.FragmentRecordBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.util.*

class RecordFragment : Fragment() {
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var binding: FragmentRecordBinding
    private lateinit var map: GoogleMap
    private lateinit var client: FusedLocationProviderClient
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private lateinit var coordinates: LatLng
    val timer = Timer()
    private var isStarted = false


    private val latLngList = arrayListOf<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingPermission")
    private fun startTimer() {
        val timerTask = object : TimerTask() {
            override fun run() {
                client.lastLocation.addOnSuccessListener {
                    getLastLocation()
                    val polyLineOptions = PolylineOptions().addAll(latLngList).clickable(true).color(ContextCompat.getColor(requireContext(),R.color.polyLineBlue))
                    map.addPolyline(polyLineOptions)
                    Log.i("HELLO", "${it.latitude} ${it.longitude}")

                }
            }
        }
        timer.schedule(timerTask, 2500, 2500)
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
            requestLocationPermission()
//            latLngList.add(LatLng(1.315,103.9633))
//            val polyLineOptions = PolylineOptions().addAll(latLngList).clickable(true).color(ContextCompat.getColor(requireContext(),R.color.polyLineBlue))
//            map.addPolyline(polyLineOptions)
            binding.btnStartStop.setOnClickListener {
                if (!isStarted) {
                    startTimer()
                    binding.btnStartStop.text = "STOP"
                    isStarted = true
                }
                else {
                    timer.cancel()

                    val action = RecordFragmentDirections.actionRecordFragmentToResultsFragment(latLngList.first().latitude.toFloat(), latLngList.first().longitude.toFloat(), latLngList.last().latitude.toFloat(), latLngList.last().longitude.toFloat() )
                    findNavController().navigate(action)
                }

            }
        }

        return binding.root
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            getLastLocation()
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                map.isMyLocationEnabled = true
                getLastLocation()
            }
            else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        client.lastLocation.addOnSuccessListener {
            currentLatitude = it.latitude
            currentLongitude = it.longitude
            coordinates = LatLng(currentLatitude, currentLongitude)
            latLngList.add(coordinates)
            val zoomLevel = 15F
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel))
        }
    }

}