package com.example.runlah.record

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ResultsFragment : Fragment() {
    private lateinit var binding: FragmentResultsBinding
    private val args: ResultsFragmentArgs by navArgs()
    private lateinit var request: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        request = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data!!.extras!!.get("data") as Bitmap
                getImage(imageBitmap)
                (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE
            }
        }
        super.onCreate(savedInstanceState)

    }
    private fun getImage(imageBitmap: Bitmap) {
        binding.apply {
            btnCamera.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            imageView.setImageBitmap(imageBitmap)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_results, container, false)
        (activity as MainActivity).supportActionBar!!.title = "Results"
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        val latLngArray = arrayListOf<LatLng>()
        // get coordinates to mark on map
        for (i in 0..args.latlngList.size - 2 step 2) {
            // format floatarray into latlngarray
            val latLng = LatLng(args.latlngList[i].toDouble(), args.latlngList[i + 1].toDouble())
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
        binding.finalDistanceTravelled.text = String.format("%.2f", (distanceTravelled / 1000))


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
                ContextCompat.getColor(requireContext(), R.color.polyLineBlue)
            )
            map.addPolyline(polyLineOptions)
            // pan camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(endCoordinates, zoomLevel))

            binding.fabNext.setOnClickListener {
                val uuid = UUID.randomUUID().toString()
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val sessionData = hashMapOf(
                    "timestamp" to FieldValue.serverTimestamp(),
                    "timeTaken" to timeTaken,
                    "stepCount" to stepCount,
                    "averageSpeed" to averageSpeed,
                    "distanceTravelled" to distanceTravelled,
                    "coordinatesArray" to latLngArray,
                    "uuid" to uuid
                )
                firestore.collection("users").document(auth.uid!!).collection("records").document(
                    uuid
                ).set(sessionData).addOnSuccessListener {
                    if (binding.imageView.drawable != null) {
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val imagesRef = storageRef.child(auth.currentUser!!.uid).child(uuid)
                        val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val uploadTask = imagesRef.putBytes(data)
                        uploadTask
                            .addOnSuccessListener {
                                Log.i("hello","upload SUCCESS")
                                requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility =
                                    View.VISIBLE
                                findNavController().navigate(R.id.action_resultsFragment_to_today_fragment)
                            }
                            .addOnFailureListener {
                                Log.i("hello",it.toString())
                            }
                    }
                    else {
                        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility =
                            View.VISIBLE
                        findNavController().navigate(R.id.action_resultsFragment_to_today_fragment)
                    }

                }


            }
        }
        binding.btnCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        return binding.root
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            request.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Log.i("hello",e.toString())
        }
    }



}