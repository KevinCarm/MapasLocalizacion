package com.example.mapaslocalizacion

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var actualPosition: Boolean = true
    private var json: JSONObject? = null
    private var latitudeOrigin: Double = 0.00
    private var longitudeOrigin: Double = 0.00
    private var longitudeDestiny: Double = -101.026818
    private var latitudeDestiny: Double = 20.206003
    private var markerOrigin: Marker? = null
    private var markerDestiny: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


    }
    
    //20.2125993,-100.8913107 salva
    //20.2056695,-101.0291317 gerva
    override fun onMapReady(googleMap: GoogleMap?) {
        try {
            map = googleMap!!

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        1
                    )
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                }
            }




            map.setOnMapClickListener {
                if (markerDestiny != null && markerOrigin != null) {
                    markerDestiny?.remove()
                    latitudeDestiny = it.latitude
                    longitudeDestiny = it.longitude
                    map.clear()
                    markerDestiny = map.addMarker(
                        MarkerOptions().title("Destiny")
                            .position(LatLng(latitudeDestiny, longitudeDestiny))
                    )
                    markerOrigin = map.addMarker(
                        MarkerOptions().position(LatLng(latitudeOrigin, longitudeOrigin))
                            .title("Origin")
                    )
                    callTraceRoute()
                }
            }

            map.isMyLocationEnabled = true
            map.setOnMyLocationChangeListener { location ->
                if (actualPosition) {
                    if (location != null) {
                        latitudeOrigin = location.latitude
                        longitudeOrigin = location.longitude
                        val cameraPosition =
                            CameraPosition.builder()
                                .target(LatLng(latitudeOrigin, longitudeOrigin))
                                .zoom(14f)
                                .bearing(30f)
                                .build()
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        actualPosition = false
                        markerOrigin = map.addMarker(
                            MarkerOptions().position(LatLng(latitudeOrigin, longitudeOrigin))
                                .title("Origin")
                        )
                        markerDestiny = map.addMarker(
                            MarkerOptions().position(LatLng(latitudeDestiny, longitudeDestiny))
                                .title("Destiny")
                        )
                    } else {
                        Toast.makeText(this, "Empty location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {

        }
        callTraceRoute()
    }

    private fun callTraceRoute() {
        val queue: RequestQueue = Volley.newRequestQueue(this)
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$latitudeOrigin,$longitudeOrigin&destination=$latitudeDestiny,$longitudeDestiny&key=AIzaSyB3JqzJKWgk6GSrYVCv0t6i6LNN2nXUaYI"
        val stringRequest =
            StringRequest(Request.Method.GET, url, { response ->
                try {
                    json = JSONObject(response)
                    json?.let {
                        traceRoute(it)
                    }
                    Log.d("RUTA", response)
                } catch (e: JSONException) {

                }
            }, {})
        queue.add(stringRequest)
    }


    private fun traceRoute(json: JSONObject) {
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray

        try {
            jRoutes = json.getJSONArray("routes")
            for (i in 0 until jRoutes.length()) {
                jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                for (j in 0 until jLegs.length()) {
                    jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")
                    for (k in 0 until jSteps.length()) {
                        val polyline =
                            "" + ((jSteps[k] as JSONObject)["polyline"] as JSONObject)["points"]
                        Log.i("end", "" + polyline)
                        val list = PolyUtil.decode(polyline)
                        map.addPolyline(
                            PolylineOptions().addAll(list)
                                .color(Color.rgb(87, 35, 100)).width(9f)
                        )
                    }
                }
            }
        } catch (e: JSONException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
    //AIzaSyB8ffayG_-7mvZv9ZHYP7UvMXQXqNAaW1A
}