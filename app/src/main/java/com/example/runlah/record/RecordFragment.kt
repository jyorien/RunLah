package com.example.runlah.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.runlah.R
import com.example.runlah.databinding.FragmentRecordBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class RecordFragment : Fragment() {
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var binding: FragmentRecordBinding
    private lateinit var map: GoogleMap
    private lateinit var client: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
            map = googleMap
//            val latitude = 1.3715
//            val longitude = 103.9633
//            val homeLatLng = LatLng(lat, lng)
//            val zoomLevel = 15F
//
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
//            map.addMarker(MarkerOptions().position(homeLatLng))
            requestLocationPermission()

//            map.setOnMapClickListener { latLng ->
//                val markerOptions = MarkerOptions()
//                markerOptions.position(latLng)
//                markerOptions.title("${latLng.latitude} ${latLng.longitude}")
//                map.clear()
//                map.animateCamera(
//                    CameraUpdateFactory.newLatLngZoom(
//                        latLng, 15F
//                    )
//                )
//                map.addMarker(markerOptions)
//            }
        }

        return binding.root
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                map.isMyLocationEnabled = true
            }
            else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}