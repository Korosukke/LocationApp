package com.example.splootassignmnet

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.splootassignmnet.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.net.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Places.initialize(applicationContext, "AIzaSyA3zeQUA47kyCgI5XJFJMn6zybxb3jPqeQ")
        placesClient = Places.createClient(this)

        getcurrentLocationUser()
    }

    private fun getcurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }

        val getLocation = fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location

                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                getcurrentLocationUser()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Current Location")
        )

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

        mMap.setOnMapClickListener(this)
    }

    override fun onMapClick(latLng: LatLng) {
        mMap.clear()

        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
        )

        // Specify the fields you want to retrieve for the place
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.RATING
        )

        // Create a FetchPlaceRequest using the place ID and fields
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // Use the PlacesClient to fetch the place details
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response: FindCurrentPlaceResponse ->
                val likelyPlaces = response.placeLikelihoods
                if (likelyPlaces != null && likelyPlaces.isNotEmpty()) {
                    val mostLikelyPlace = likelyPlaces[0].place

                    val poiName = mostLikelyPlace.name
                    val poiAddress = mostLikelyPlace.address
                    val poiRating = mostLikelyPlace.rating

                    // Show the information dialog
                    val dialog = Dialog(this@MapsActivity)
                    dialog.setContentView(R.layout.interest_info)

                    val nameTextView = dialog.findViewById<TextView>(R.id.name)
                    val addressTextView = dialog.findViewById<TextView>(R.id.address)
                    val ratingTextView = dialog.findViewById<TextView>(R.id.rating)

                    nameTextView.text = poiName
                    addressTextView.text = poiAddress
                    ratingTextView.text = poiRating?.toString() ?: ""

                    dialog.show()
                }
            }
            .addOnFailureListener { exception: Exception ->
                // Handle the failure case if the place details cannot be fetched
                Toast.makeText(this, "Failed to fetch place details", Toast.LENGTH_SHORT).show()
            }
    }
}