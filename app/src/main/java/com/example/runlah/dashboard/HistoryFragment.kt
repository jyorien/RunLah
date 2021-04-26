package com.example.runlah.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.example.runlah.R
import com.example.runlah.databinding.FragmentHistoryBinding
import com.example.runlah.home.MainActivity

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
        Log.i("HELLO",args.record.toString())
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
        return binding.root
    }


}