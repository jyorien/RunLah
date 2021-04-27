package com.example.runlah.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.example.runlah.R
import com.example.runlah.databinding.FragmentHistoryBinding
import com.example.runlah.home.MainActivity
import com.example.runlah.record.zoomLevel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    val args: HistoryFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_history, container, false)
        val recordResult = args.record
        (activity as MainActivity).supportActionBar!!.title = recordResult.date
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.apply {
            historyDisplayDistance.text = recordResult.distance
            historyDisplaySpeed.text = recordResult.speed
            historyDisplaySteps.text = recordResult.steps
            historyDisplayTimeTaken.text = recordResult.timeTaken
        }
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.history_map) as SupportMapFragment
        supportMapFragment.getMapAsync { map ->
            // get coordinates for start and end markers
            val startCoordinates = LatLng(
                recordResult.latLngArray.first().latitude,
                recordResult.latLngArray.first().longitude
            )
            val endCoordinates = LatLng(
                recordResult.latLngArray.last().latitude,
                recordResult.latLngArray.last().longitude
            )
            // add markers
            val startMarkerOptions = MarkerOptions().position(startCoordinates).title("Start")
            val endMarkerOptions = MarkerOptions().position(endCoordinates).title("End")
            map.addMarker(startMarkerOptions)
            map.addMarker(endMarkerOptions)
            // draw route
            val polyLineOptions = PolylineOptions().addAll(recordResult.latLngArray).clickable(true).color(
                ContextCompat.getColor(requireContext(), R.color.polyLineBlue)
            )
            map.addPolyline(polyLineOptions)
            // pan camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(recordResult.latLngArray.first(), zoomLevel))
        }
        return binding.root
    }


}