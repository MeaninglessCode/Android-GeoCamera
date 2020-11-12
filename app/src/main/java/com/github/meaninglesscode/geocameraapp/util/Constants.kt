package com.github.meaninglesscode.geocameraapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * [Object] containing various constants used throughout the application.
 */
object Constants {
    const val FILE_PROVIDER_AUTHORITY = "com.github.meaninglesscode.geocameraapp.fileprovider"
    const val GOOGLE_MAP_FRAGMENT_TAG = "GOOGLE_MAP"

    object DateFormats {
        val IMAGE_FILENAME_FORMATTER = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S", Locale.US)
        val IMAGE_MAP_MARKER_FORMATTER = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.US)
    }

    object Requests {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_CAMERA_PERMISSION = 101
        const val REQUEST_FINE_LOCATION_PERMISSION = 102
    }

    object Rationales {
        const val CAMERA_RATIONALE_TITLE = "Camera Permission Needed"
        const val CAMERA_RATIONALE_MESSAGE = "This application requires camera permission to take photos."
        const val LOCATION_RATIONALE_TITLE = "Location Permission Needed"
        const val LOCATION_RATIONALE_MESSAGE = "This application requires location permission to tag photos."
    }
}