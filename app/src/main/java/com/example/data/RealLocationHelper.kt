package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import com.example.data.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class RealLocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var activeCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData = withContext(Dispatchers.IO) {
        var rawLocation: Location? = null

        try {
            // First attempt with Google Play Services Fused Location
            rawLocation = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc, null)
                }.addOnFailureListener {
                    continuation.resume(null, null)
                }
            }
        } catch (e: Exception) {
            Log.w("RealLocationHelper", "Fused location failed, trying LocationManager: ${e.message}")
        }

        // Fallback to Android LocationManager GPS or Network provider if fused returned null
        if (rawLocation == null) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager != null) {
                    val gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    rawLocation = gps ?: net
                }
            } catch (e: Exception) {
                Log.w("RealLocationHelper", "LocationManager fallback failed: ${e.message}")
            }
        }

        val lat = rawLocation?.latitude ?: 0.0
        val lng = rawLocation?.longitude ?: 0.0
        val accuracy = rawLocation?.accuracy ?: 10f

        val addressInfo = resolveAddress(lat, lng)

        LocationData(
            latitude = lat,
            longitude = lng,
            title = addressInfo.first,
            address = addressInfo.second,
            isLive = false,
            accuracyMeters = accuracy
        )
    }

    private fun resolveAddress(latitude: Double, longitude: Double): Pair<String, String> {
        if (latitude == 0.0 && longitude == 0.0) {
            return Pair("Ubicación actual", "Esperando señal de satélite GPS...")
        }

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.thoroughfare ?: addr.featureName ?: addr.locality ?: "Mi ubicación"
                    val full = listOfNotNull(addr.thoroughfare, addr.subThoroughfare, addr.locality, addr.adminArea, addr.countryName)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    return Pair(feature, if (full.isNotBlank()) full else "Coordenadas: $latitude, $longitude")
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.thoroughfare ?: addr.featureName ?: addr.locality ?: "Mi ubicación"
                    val full = listOfNotNull(addr.thoroughfare, addr.subThoroughfare, addr.locality, addr.adminArea, addr.countryName)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    return Pair(feature, if (full.isNotBlank()) full else "Coordenadas: $latitude, $longitude")
                }
            }
        } catch (e: Exception) {
            Log.e("RealLocationHelper", "Geocoder error: ${e.message}")
        }

        return Pair("Ubicación GPS", "Lat: %.5f, Lng: %.5f".format(Locale.US, latitude, longitude))
    }

    @SuppressLint("MissingPermission")
    fun startLiveLocationUpdates(
        durationMinutes: Int,
        onUpdate: (LocationData) -> Unit
    ) {
        stopLiveLocationUpdates()

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds interval
        ).setMinUpdateIntervalMillis(5000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val addressInfo = resolveAddress(loc.latitude, loc.longitude)
                onUpdate(
                    LocationData(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        title = addressInfo.first,
                        address = addressInfo.second,
                        isLive = true,
                        liveDurationMinutes = durationMinutes,
                        remainingMinutes = durationMinutes,
                        accuracyMeters = loc.accuracy,
                        isMoving = loc.speed > 0.5f
                    )
                )
            }
        }

        activeCallback = callback
        try {
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("RealLocationHelper", "Error starting live updates: ${e.message}")
        }
    }

    fun stopLiveLocationUpdates() {
        activeCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            activeCallback = null
        }
    }
}
