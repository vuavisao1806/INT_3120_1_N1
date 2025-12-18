package com.example.locationpins.ui.screen.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocationManager : DefaultLifecycleObserver {
    private var client: FusedLocationProviderClient? = null

    private val _location = MutableStateFlow<android.location.Location?>(null)
    val location = _location.asStateFlow()

    private var isTracking = false
    private var hasPermission = false

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                Log.d("LocationManager", "onLocationResult: ${it.latitude}, ${it.longitude} t=${it.time}")
                _location.value = it
            }
        }
    }

    fun init(context: Context) {
        if (client == null) {
            val appContext = context.applicationContext
            client = LocationServices.getFusedLocationProviderClient(appContext)

            val permissionState = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            hasPermission = (permissionState == PackageManager.PERMISSION_GRANTED)

            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    fun onPermissionGranted() {
        hasPermission = true
        startUpdates()
    }

    override fun onStart(owner: LifecycleOwner) {
        startUpdates()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startUpdates() {
        if (hasPermission && !isTracking && client != null) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateDistanceMeters(0f)
                .build()

            client?.requestLocationUpdates(request, callback, Looper.getMainLooper())
            isTracking = true
        }
    }

    private fun stopUpdates() {
        if (isTracking) {
            client?.removeLocationUpdates(callback)
            isTracking = false
        }
    }
}