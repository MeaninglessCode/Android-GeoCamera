package com.github.meaninglesscode.geocameraapp.maps

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.meaninglesscode.geocameraapp.R
import com.github.meaninglesscode.geocameraapp.util.*
import com.github.meaninglesscode.geocameraapp.util.Constants.FILE_PROVIDER_AUTHORITY
import com.github.meaninglesscode.geocameraapp.util.Constants.GOOGLE_MAP_FRAGMENT_TAG
import com.github.meaninglesscode.geocameraapp.util.Constants.Rationales
import com.github.meaninglesscode.geocameraapp.util.Constants.Requests
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Primary user interface for viewing the Google Map and geo tagged photo markers. The
 * [MapsFragment] implements [DaggerFragment] for dependency injection purposes as well as
 * [OnMapReadyCallback] for initializing the associated [GoogleMap].
 */
class MapsFragment: DaggerFragment(), OnMapReadyCallback {
    /**
     * [ViewModelProvider.Factory] provided via dependency injection so that the [MapsViewModel]
     * can be obtained via the factory.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * View model produced via [viewModelFactory] to obtain the [MapsViewModel].
     */
    private val viewModel by viewModels<MapsViewModel> { viewModelFactory }

    private lateinit var mMap: GoogleMap
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest

    private var currentPhotoPath: String? = null
    private var lastLocation: Location? = null

    /**
     * [LocationCallback] object for getting location updates via the [FusedLocationProviderClient].
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations

            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                lastLocation = location
            }
        }
    }

    /**
     * Method overriding [DaggerFragment.onCreate] to allow for instantiation of the initial set of
     * markers to display on the map as well as the [FusedLocationProviderClient].
     *
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreate] method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        viewModel.markerPictureData.observe(this, {
            it.forEach { pictureData ->
                    if (!pictureData.displayedOnMap) {
                        val markerOptions = MarkerOptions()
                                .position(pictureData.getLatLng())

                        val marker = mMap.addMarker(markerOptions)

                        pictureData.displayedOnMap = true
                        marker.tag = pictureData
                    }
                }
        })
    }

    /**
     * Method overriding [DaggerFragment.onCreateView] to allow for instantiation of data binding
     * and setting up additional fragment components.
     *
     * @param [inflater] [LayoutInflater] automatically passed into the [onCreateView] method
     * @param [container] [ViewGroup]? automatically passed into the [onCreateView] method
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreateView] method
     * @return [View]? created by using [inflater] to inflate the associated layout with [container]
     * as the [ViewGroup]
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val rootView = inflater.inflate(R.layout.maps_frag, container, false)

        val mapFragment = childFragmentManager.findFragmentByTag(GOOGLE_MAP_FRAGMENT_TAG) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return rootView
    }

    /**
     * Method overriding [DaggerFragment.onCreateOptionsMenu] to handle inflation of the options
     * menu layout.
     *
     * @param [menu] [Menu] automatically passed into the [onCreateOptionsMenu] method
     * @param [inflater] [MenuInflater] automatically passed into the [onCreateOptionsMenu] method
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.maps_menu, menu)
    }

    /**
     * Method overriding [DaggerFragment.onOptionsItemSelected] to handle menu bar interactions.
     *
     * @param [item] [MenuItem] interacted with by the user and automatically passed into the method
     * @return [Boolean] value returned based on whether or not an action was performed
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_switch_to_camera -> {

                if (!hasPermission(requireActivity(), CAMERA)) {
                    checkPermission(CAMERA, Requests.REQUEST_CAMERA_PERMISSION,
                            Rationales.CAMERA_RATIONALE_TITLE, Rationales.CAMERA_RATIONALE_MESSAGE
                    )
                }
                else if (!hasPermission(requireActivity(), ACCESS_FINE_LOCATION) && !hasPermission(requireActivity(), ACCESS_COARSE_LOCATION)) {
                    requireView().showSnackbar(
                        "Location access required to tag photos.",
                        Snackbar.LENGTH_SHORT
                    )
                }
                else if (lastLocation == null) {
                    requireView().showSnackbar(
                        "Location still initializing. Please wait a moment.",
                        Snackbar.LENGTH_SHORT
                    )
                }
                else
                    dispatchImageCaptureIntent()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Method overriding [DaggerFragment.onActivityResult] to handle results from the camera
     * activity that can be launched via this [DaggerFragment].
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle the result if it was the take photo request
        if (requestCode == Requests.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            val currentMillis = Calendar.getInstance().timeInMillis
            val latitude = lastLocation!!.latitude
            val longitude = lastLocation!!.longitude

            viewModel.savePictureData(currentPhotoPath!!, latitude, longitude, currentMillis)
        }
    }

    /**
     * Method overriding [DaggerFragment.onResume] to check whether or not the user has denied any
     * permissions since last time they were in the app.
     */
    override fun onResume() {
        super.onResume()

        if (!hasPermission(requireActivity(), CAMERA)) {
            checkPermission(CAMERA, Requests.REQUEST_CAMERA_PERMISSION,
                    Rationales.CAMERA_RATIONALE_TITLE, Rationales.CAMERA_RATIONALE_MESSAGE
            )
        }
        if (!hasPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))) {
            checkPermission(ACCESS_FINE_LOCATION, Requests.REQUEST_FINE_LOCATION_PERMISSION,
                    Rationales.LOCATION_RATIONALE_TITLE, Rationales.LOCATION_RATIONALE_MESSAGE
            )
        }
    }

    /**
     * Method overriding [DaggerFragment.onPause] to stop location updates in the event of the app
     * being paused.
     */
    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }

    /**
     * Method overriding [DaggerFragment.onRequestPermissionsResult] to provide the user with
     * information based on what permission they allowed or denied.
     *
     * @param [requestCode] [Int] representing the type of permission request
     * @param [permissions] [Array] of [String] representing the permissions associated with the
     * request
     * @param [grantResults] [IntArray] representing the results of the permission grant request
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Requests.REQUEST_FINE_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enableLocationServices()
                else {
                    requireView().showSnackbar(
                            "Location access required to tag photos.",
                            Snackbar.LENGTH_SHORT
                    )
                }
            }

            Requests.REQUEST_CAMERA_PERMISSION -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requireView().showSnackbar(
                            "Camera access required to take photos.",
                            Snackbar.LENGTH_SHORT
                    )
                }
            }
        }
    }

    /**
     * Method implementing [OnMapReadyCallback] to instantiate [mMap], initialize data from
     * [viewModel], and attempt to enable location services.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setInfoWindowAdapter(MapInfoWindowAdapter(requireContext()))

        viewModel.initializePictureData()
        enableLocationServices()
    }

    /**
     * Method to enable map user location services if permissions have been granted. If permissions
     * are granted, this method sets the value of [lastLocation] as well as setting up location
     * updates for [fusedLocationProviderClient]. If location permissions have not been granted,
     * then the function asks for them.
     */
    @SuppressLint("MissingPermission")
    private fun enableLocationServices() {
        locationRequest = LocationRequest()

        locationRequest.interval = 12000
        locationRequest.fastestInterval = 12000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (hasPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))) {
            /**
             * Attempt to get the last location value from [fusedLocationProviderClient] and use it
             * to set the initial value of [lastLocation].
             */
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener {
                if (it != null) {
                    lastLocation = it

                    val latLng = LatLng(it.latitude, it.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16F)
                    mMap.animateCamera(cameraUpdate)
                }
            }

            fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            mMap.isMyLocationEnabled = true
        }
        else {
            checkPermission(ACCESS_FINE_LOCATION, Requests.REQUEST_FINE_LOCATION_PERMISSION,
                Rationales.LOCATION_RATIONALE_TITLE, Rationales.LOCATION_RATIONALE_MESSAGE
            )
        }
    }

    /**
     * Method to create a [MediaStore.ACTION_IMAGE_CAPTURE] [Intent] and launch into it so that the
     * user may take a photo.
     */
    private fun dispatchImageCaptureIntent() {
        val imageCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (imageCaptureIntent.resolveActivity(requireActivity().packageManager) != null) {
            var photoFile: File? = null

            try {
                photoFile = viewModel.createImageFile(requireActivity())
                currentPhotoPath = photoFile.absolutePath
            }
            catch (e: Exception) {
                requireView().showSnackbar(
                        "Failed to create image file.", Snackbar.LENGTH_SHORT
                )
            }

            if (photoFile != null) {
                val photoUri = FileProvider.getUriForFile(
                    requireContext(), FILE_PROVIDER_AUTHORITY, photoFile
                )

                imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(imageCaptureIntent, Requests.REQUEST_TAKE_PHOTO)
            }
        }
    }

    /**
     * Helper method to check whether or not a permission has been granted and, if it has not been
     * granted, call [shouldShowRequestPermissionRationale] to determine if a detailed popup should
     * be shown to describe why the application needs the permission or to simply call
     * [requestPermissions] to query the user for the requests permission.
     *
     * @param [permission] [String] representing the permission to request
     * @param [requestCode] [Int] representing the code of the permission to request so that it may
     * be used in [onRequestPermissionsResult]
     * @param [rationalTitle] [String] title of the [AlertDialog] shown if a permissions rationale
     * should be displayed to the user
     * @param [rationaleMessage] [String] message of the [AlertDialog] shown if a permissions
     * rationale should be displayed to the user
     */
    private fun checkPermission(
        permission: String, requestCode: Int, rationalTitle: String, rationaleMessage: String
    ) {
        if (!hasPermission(requireActivity(), permission)) {
            if (shouldShowRequestPermissionRationale(permission)) {

                AlertDialog.Builder(requireContext())
                    .setTitle(rationalTitle)
                    .setMessage(rationaleMessage)
                    .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                        requestPermissions(arrayOf(permission), requestCode)
                    }
                    .create()
                    .show()
            }
            else
                requestPermissions(arrayOf(permission), requestCode)
        }
    }
}