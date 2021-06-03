package com.example.runlah.dashboard

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.runlah.R
import com.example.runlah.databinding.FragmentDashboardBinding
import com.example.runlah.home.MainActivity
import com.example.runlah.home.SharedViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var weeklyTotalDistanceMap: TreeMap<Int, Double>
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // hide up button
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar!!.title = "Dashboard"
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility =
            View.VISIBLE

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.lifecycleOwner = this

        viewModel.recordList.observe(viewLifecycleOwner, { list ->
            binding.historyList.adapter = HistoryListAdapter(list,
                { record ->
                    val action =
                        DashboardFragmentDirections.actionDashboardFragmentToHistoryFragment(record)
                    findNavController().navigate(action)
                },
                { record ->
                    showDeleteDialog(record.documentId)

                })
            (binding.historyList.adapter as HistoryListAdapter).notifyDataSetChanged()

        })

        viewModel.weeklyDistanceMap.observe(viewLifecycleOwner, { map ->
            weeklyTotalDistanceMap = map as TreeMap<Int, Double>

        })

        viewModel.xAxisValues.observe(viewLifecycleOwner, {
            val data = getChartData(weeklyTotalDistanceMap, it)
            getChartAppearance(it)
            prepareChartData(data)

        })

        viewModel.isSingleDeleted.observe(viewLifecycleOwner, { isSingleDelete ->
            if (isSingleDelete) {
                Snackbar.make(
                    binding.root,
                    "The entry has been permanently deleted",
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.onCompleteSingleDelete()
            }

        })
        viewModel.isDeleted.observe(viewLifecycleOwner, { isDeleted ->
            if (isDeleted) {
                Snackbar.make(
                    binding.root,
                    "Your running entries have been permanently deleted",
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.onCompleteDelete()
            }

        })
        return binding.root
    }

    private fun getChartAppearance(xAxisValues: ArrayList<Int>) {
        val newList = arrayListOf<String>()
        xAxisValues.forEach {
            newList.add(it.toString())
        }
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(newList)
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.description.isEnabled = false
        binding.barChart.axisLeft.granularity = 10f
        binding.barChart.axisLeft.axisMinimum = 0f
        binding.barChart.xAxis.textColor =
            ContextCompat.getColor(requireContext(), R.color.invertedTextColor)
        binding.barChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.invertedTextColor)
        binding.barChart.legend.textColor =
            ContextCompat.getColor(requireContext(), R.color.invertedTextColor)
    }

    private fun getChartData(map: Map<Int, Double>, xAxisList: ArrayList<Int>): BarData {
        val values = arrayListOf<BarEntry>()
        val yAxisValuesList = arrayListOf<Double>()
        for (day in xAxisList) {

            if (map[day] == null) {
                yAxisValuesList.add(0.0)
            }
            else
                yAxisValuesList.add(map[day] as Double)
        }
        // force the x axis to be spaced evenly, rename the labels in IndexAxisValueFormatter
        for (i in 0..6) {
            val coordinates = BarEntry(i.toFloat(), yAxisValuesList[i].roundToInt().toFloat())
//            if (map.keys.contains(i)) {
//                val y = map[i]
//                coordinates = BarEntry(i.toFloat(), (y!!).roundToInt().toFloat())
//            }
            values.add(coordinates)
        }
        val set1 = BarDataSet(values, "Distance (m)")
        set1.color = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
        val datasets = arrayListOf<IBarDataSet>()
        datasets.add(set1)
        return BarData(datasets)
    }

    private fun prepareChartData(data: BarData) {
        data.setValueTextSize(12f)
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.invertedTextColor))
        binding.barChart.data = data
        binding.barChart.animateY(500)
        binding.barChart.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> showDeleteAllDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteAllDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete entries")
        builder.setMessage("Are you sure you want to delete all entries?")
        builder.setPositiveButton("Delete") { _, _ ->
            viewModel.deleteUserRecords()
        }
        builder.setNegativeButton("Cancel") { _, _ ->

        }
        val alertDialog = builder.create()
        alertDialog.setOnShowListener { _ ->
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.lightRed))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }
        alertDialog.show()
    }

    private fun showDeleteDialog(docId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete entry")
        builder.setMessage("Are you sure you want to delete this entry?")
        builder.setPositiveButton("Delete") { _, _ ->
            viewModel.deleteSingleUserRecord(docId)
        }
        builder.setNegativeButton("Cancel") { _, _ ->

        }
        val alertDialog = builder.create()
        alertDialog.setOnShowListener { _ ->
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.lightRed))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }
        alertDialog.show()
    }

}