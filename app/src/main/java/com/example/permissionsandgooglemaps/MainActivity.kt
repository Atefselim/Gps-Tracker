package com.example.permissionsandgooglemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity() ,OnMapReadyCallback{
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
        if(isGranted){
            getUserLocation()
        }else{
            showRationalDialog()
        }
    }
    private var googleMap:GoogleMap?=null
    lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermission()
    }
    val locationCallBack = object : LocationCallback() {
        var marker : Marker?  =null
        override fun onLocationResult(locationResult: LocationResult) {
            for(location in locationResult.locations){
            Log.e("Location", "${location.latitude}")
            Log.e("Location", "${location.longitude}")
                if (marker == null) {
                    val markerOption = MarkerOptions()
                    markerOption.position(LatLng(location.latitude, location.longitude))
                    markerOption.title("Current User Location")
                    marker = this@MainActivity.googleMap?.addMarker(markerOption)
                }else{
                    marker?.position = LatLng(location.latitude,location.longitude)
                }
                this@MainActivity.googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),14.0F))
            }
        }

    }
    @SuppressLint("MissingPermission")
    private fun getUserLocation(){
//        Toast.makeText(this,"User Granted Permission",Toast.LENGTH_LONG).show()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setDurationMillis(5000)
            .build()
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,10000)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallBack, Looper.getMainLooper())

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

    }
    private fun showRationalDialog(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("why we need this permission")
        dialog.setMessage("we need this permission to find and track nearest drivers")
        dialog.setPositiveButton("yes,I understand"){ dialog,which->
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            dialog.dismiss()
        }
        dialog.setNegativeButton("No, I Refuse"){dialog,which->
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun checkForPermission(){
        when{
            ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED ->{
                getUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->{
                showRationalDialog()
            }else ->{
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallBack)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }
}
