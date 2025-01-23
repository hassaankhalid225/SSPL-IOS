package com.sspl.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.AppTextTitle
import com.sspl.ui.postcreation.CameraPermissionDeniedDialog
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.jordond.compass.Coordinates
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.MobileGeolocator
import dev.jordond.compass.permissions.LocationPermissionController
import dev.jordond.compass.permissions.PermissionState
import dev.jordond.compass.permissions.mobile.openSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import shared.PermissionCallback
import shared.PermissionStatus
import shared.PermissionType
import shared.createPermissionsManager
import shared.rememberCameraManager
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun LocationScreen() {

    val apiCoordinates = Coordinates(26.4767636, 74.2738066)
    val allowedRadius = 50 // meters
    val scope = rememberCoroutineScope()
    val scaffoldState = remember { SnackbarHostState() }
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }

    val locationPermissionController = remember { LocationPermissionController() }
    val geoLocation = remember { MobileGeolocator(locationPermissionController) }


    var launchCamera by remember { mutableStateOf(value = false) }
    var launchSetting by remember { mutableStateOf(value = false) }
    var permissionRationalDialog by remember { mutableStateOf(value = false) }
    val permissionsManager = createPermissionsManager(object : PermissionCallback {
        override fun onPermissionStatus(
            permissionType: PermissionType, status: PermissionStatus
        ) {
            when (status) {
                PermissionStatus.GRANTED -> {
                    when (permissionType) {
                        PermissionType.CAMERA -> launchCamera = true
                        else -> Unit
                    }
                }

                else -> {
                    permissionRationalDialog = true
                }
            }
        }
    })
    val cameraManager = rememberCameraManager {
        scope.launch {
            withContext(Dispatchers.Default) {
                it?.let { img ->
                    img.toByteArray()?.let { asByteArray ->
                        println("ByteArray is $asByteArray")
                        scaffoldState.showSnackbar("Image captuted success")
                    }
                }
            }
        }
    }

    if (launchCamera) {
        if (permissionsManager.isPermissionGranted(PermissionType.CAMERA)) {
            cameraManager.launch()
        } else {
            permissionsManager.askPermission(PermissionType.CAMERA)
        }
        launchCamera = false
    }
    if (launchSetting) {
        permissionsManager.launchSettings()
        launchSetting = false
    }
    if (permissionRationalDialog) {
        CameraPermissionDeniedDialog(onDismiss = { permissionRationalDialog = false },
            onOpenSettings = {
                permissionRationalDialog = false
                launchSetting = true
            })
    }

    var isGettingLocation by remember {
        mutableStateOf(false)
    }


    var locationState by remember { mutableStateOf(LocationState()) }

    LaunchedEffect(locationState.isPermissionGranted) {
        if (locationState.isPermissionGranted) {
            locationState = locationState.copy(isLocationOn = geoLocation.isAvailable())
        }
    }
    LaunchedEffect(locationState.isLocationOn) {
        if (locationState.isLocationOn) {
            isGettingLocation = true
            when (val result = geoLocation.current(Priority.HighAccuracy)) {
                is GeolocatorResult.Error -> {
                    println(result.message)
                    scaffoldState.showSnackbar(result.message)
                    isGettingLocation = false
                }

                is GeolocatorResult.Success -> {
                    locationState = locationState.copy(
                        latitude = result.data.coordinates.latitude.toString(),
                        longitude = result.data.coordinates.longitude.toString()
                    )
                    val distance = calculateDistance(apiCoordinates, Coordinates(
                        result.data.coordinates.latitude,
                        result.data.coordinates.longitude
                    ))
                    val isWithinRadius = distance <= allowedRadius


                    if (isWithinRadius) {
                        scaffoldState.showSnackbar("You are within the allowed radius.")
                    } else {
                        scaffoldState.showSnackbar("You are outside the allowed radius. Distance: ${distance.roundToInt()} meters")
                    }
                    isGettingLocation = false

                }
            }
        } else {
            scaffoldState.showSnackbar("Turn on devices Location")
            // TODO: go to mobile settings to turn device location on (not App permission)
        }
    }
    if (locationState.isPermissionDeniedForever) {
        PermissionDeniedDialog(
            onDismiss = { locationState = locationState.copy(isPermissionDeniedForever = false) },
            onGoToSettings = {
                locationState = locationState.copy(isPermissionDeniedForever = false)
                controller.openAppSettings()
            }
        )
    }


    Scaffold(snackbarHost = { SnackbarHost(scaffoldState) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    scope.launch {
                        requestPermissions(
                            locationPermissionController = locationPermissionController,
                            onPermissionGranted = {
                                locationState = locationState.copy(isPermissionGranted = true)
                            },
                            onPermissionDenied = {
                                locationState = locationState.copy(isPermissionDenied = true)
                            },
                            onPermissionDeniedForever = {
                                locationState = locationState.copy(isPermissionDeniedForever = true)
                            },
                            onLocationUndetermined = {
                                locationState = locationState.copy(hasPhoneNoLocation = true)
                            }
                        )
                    }
                }) {
                    AppTextSubTitle("Mark Attendance", color = Color.White)
                }

                AppTextBody(if (isGettingLocation) "Getting Location..." else "location-> latitude :${locationState.latitude}")


                AppTextBody(
                    if (isGettingLocation) "Getting Location..." else "location-> longitude:${locationState.longitude}",
                    modifier = Modifier.clickable {
                        locationState = locationState.copy(hasPhoneNoLocation = true)
                    })

                androidx.compose.animation.AnimatedVisibility(locationState.hasPhoneNoLocation) {
                    Button(onClick = {
                        launchCamera = true
                    }) {
                        AppTextTitle("Take Selfie and Post")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedDialog(onDismiss: () -> Unit, onGoToSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppTextTitle("Location Permission Required") },
        text = { AppTextBody("This app needs location permission to function properly. Please grant the permission in Settings.") },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                AppTextSubTitle("Go to Settings", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                AppTextSubTitle("Cancel", color = Color.White)
            }
        }
    )
}

data class LocationState(
    val latitude: String? = null,
    val longitude: String? = null,
    val isLocationOn: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val hasPhoneNoLocation: Boolean = false,
    val isPermissionDenied: Boolean = false,
    val isPermissionDeniedForever: Boolean = false
)

suspend fun requestPermissions(
    locationPermissionController: LocationPermissionController,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionDeniedForever: () -> Unit,
    onLocationUndetermined: () -> Unit
) {
    val result = locationPermissionController.requirePermissionFor(Priority.HighAccuracy)
    println("result location is $result")
    when (result) {
        PermissionState.NotDetermined -> onLocationUndetermined()
        PermissionState.Granted -> onPermissionGranted()
        PermissionState.Denied -> onPermissionDenied()
        PermissionState.DeniedForever -> onPermissionDeniedForever()
    }
}
suspend fun calculateDistance(coord1: Coordinates, coord2: Coordinates): Double {
    return withContext(Dispatchers.Default){

        val R = 6371e3 // Earth's radius in meters
        val φ1 = coord1.latitude * PI / 180
        val φ2 = coord2.latitude * PI / 180
        val Δφ = (coord2.latitude - coord1.latitude) * PI / 180
        val Δλ = (coord2.longitude - coord1.longitude) * PI / 180

        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

         R * c // Distance in meters
    }
}
