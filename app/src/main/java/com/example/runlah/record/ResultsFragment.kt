package com.example.runlah.record

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.example.runlah.R
import com.example.runlah.databinding.FragmentResultsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class ResultsFragment : Fragment() {
    private lateinit var binding: FragmentResultsBinding
    private val args: ResultsFragmentArgs by navArgs()
    private lateinit var client: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_results, container, false)

        val latLngArray = arrayListOf<LatLng>()
        for (i in 0..args.latlngList.size-2 step 2) {
            // format floatarray into latlngarray
            val latLng = LatLng(args.latlngList[i].toDouble(), args.latlngList[i+1].toDouble())
            latLngArray.add(latLng)
        }

        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.result_map) as SupportMapFragment
        supportMapFragment.getMapAsync { map ->
            // get coordinates for start and end markers
            val startCoordinates = LatLng(
                latLngArray.first().latitude,
                latLngArray.first().longitude
            )
            val endCoordinates = LatLng(
                latLngArray.last().latitude,
                latLngArray.last().longitude
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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(startCoordinates,15F))
        }

        return binding.root
    }

}