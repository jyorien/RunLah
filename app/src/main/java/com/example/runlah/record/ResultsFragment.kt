package com.example.runlah.record

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.runlah.R
import com.example.runlah.databinding.FragmentResultsBinding
import com.example.runlah.home.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResultsFragment : Fragment() {
    private lateinit var binding: FragmentResultsBinding
    private val args: ResultsFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_results, container, false)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        val latLngArray = arrayListOf<LatLng>()
        // get coordinates to mark on map
        for (i in 0..args.latlngList.size-2 step 2) {
            // format floatarray into latlngarray
            val latLng = LatLng(args.latlngList[i].toDouble(), args.latlngList[i+1].toDouble())
            latLngArray.add(latLng)
        }
        val timeTaken = args.timeTaken
        val stepCount = args.stepCount
        val averageSpeed = args.averageSpeed
        val distanceTravelled = args.distanceTravelled
        val startLat = latLngArray.first().latitude
        val startLng = latLngArray.first().longitude
        val endLat = latLngArray.last().latitude
        val endLng = latLngArray.last().longitude

        // bind data to views
        binding.finalTimeTaken.text = timeTaken
        binding.finalStepsTaken.text = stepCount.toString()
        binding.averageSpeed.text = String.format("%.2f", averageSpeed)
        binding.finalDistanceTravelled.text = String.format("%.2f", (distanceTravelled/1000))


        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.result_map) as SupportMapFragment
        supportMapFragment.getMapAsync { map ->
            // get coordinates for start and end markers
            val startCoordinates = LatLng(
                startLat,
                startLng
            )
            val endCoordinates = LatLng(
                endLat,
                endLng
            )
            // add markers
            val startMarkerOptions = MarkerOptions().position(startCoordinates).title("Start")
            val endMarkerOptions = MarkerOptions().position(endCoordinates).title("End")
            map.addMarker(startMarkerOptions)
            map.addMarker(endMarkerOptions)
            // draw route
            val polyLineOptions = PolylineOptions().addAll(latLngArray).clickable(true).color(
                ContextCompat.getColor(requireContext(),R.color.polyLineBlue))
            map.addPolyline(polyLineOptions)
            // pan camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(endCoordinates, zoomLevel))

            binding.fabNext.setOnClickListener {
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val sessionData = hashMapOf(
                    "timestamp" to FieldValue.serverTimestamp(),
                    "timeTaken" to timeTaken,
                    "stepCount" to stepCount,
                    "averageSpeed" to averageSpeed,
                    "distanceTravelled" to distanceTravelled,
                    "startLat" to startLat,
                    "startLng" to startLng,
                    "endLat" to endLat,
                    "endLng" to endLng
                )
                firestore.collection("users").document(auth.uid!!).collection("records").add(sessionData).addOnSuccessListener {
                    requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
                    findNavController().navigate(R.id.action_resultsFragment_to_today_fragment)
                }
            }
        }

        return binding.root
    }

}