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

    // StateFlow để UI lắng nghe
    private val _location = MutableStateFlow<android.location.Location?>(null)
    val location = _location.asStateFlow()

    private var isTracking = false
    private var hasPermission = false

    // Giữ tham chiếu callback để remove sau này
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

            // 1. Kiểm tra xem quyền đã được cấp từ trước chưa
            val permissionState = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            hasPermission = (permissionState == PackageManager.PERMISSION_GRANTED)

            // 2. Đăng ký lắng nghe vòng đời App
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    // Hàm gọi từ UI khi user vừa bấm "Cho phép"
    fun onPermissionGranted() {
        hasPermission = true
        startUpdates()
    }

    // Tự động chạy khi App nổi lên (bất kể màn hình nào)
    override fun onStart(owner: LifecycleOwner) {
        startUpdates()
    }

    // Tự động tắt khi App chìm xuống (Home/Tắt màn hình)
    override fun onStop(owner: LifecycleOwner) {
        stopUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startUpdates() {
        // Chỉ chạy nếu có quyền + chưa chạy + client đã init
        if (hasPermission && !isTracking && client != null) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000) // 3 giây
                .setMinUpdateDistanceMeters(0f) // Hoặc di chuyển 2 mét
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