package com.example.runlah.record

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.runlah.R

class ResultsFragment : Fragment() {

    val args: ResultsFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val startLat = args.startLatitude
        Log.i("HELLO","${startLat} ${args.startLongitude}, ${args.endLatitude}, ${args.endLongitude}")
        return inflater.inflate(R.layout.fragment_results, container, false)
    }

}