package com.github.meaninglesscode.geocameraapp.util

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.meaninglesscode.geocameraapp.R
import com.github.meaninglesscode.geocameraapp.data.PictureData
import com.github.meaninglesscode.geocameraapp.util.Constants.DateFormats
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Class implementing [GoogleMap.InfoWindowAdapter] for displaying a custom popup when a given
 * [Marker] is clicked.
 *
 * @param [context] [Context] to use in order to retrieved the appropriate [LayoutInflater]
 */
class MapInfoWindowAdapter constructor(context: Context): GoogleMap.InfoWindowAdapter {
    // Inflater used for creating the view
    private val inflater: LayoutInflater =
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * Required override for [GoogleMap.InfoWindowAdapter.getInfoWindow]. Using this override is
     * unnecessary since I want to use the default info bubble.
     */
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    /**
     * Required override for [GoogleMap.InfoWindowAdapter.getInfoContents]. Using this override is
     * necessary since I intend to set custom contents to the bubble when a [Marker] is clicked.
     *
     * @param [marker] [Marker] to instantiate a [View] for
     * @return [View]? resulting from the [LayoutInflater]
     */
    override fun getInfoContents(marker: Marker): View? {
        val pictureData = marker.tag as PictureData

        val view = inflater.inflate(R.layout.marker_info_window, null)

        val imageView = view.findViewById<ImageView>(R.id.marker_image_view)

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true

        BitmapFactory.decodeFile(pictureData.uri, bitmapOptions)

        val targetWidth = (bitmapOptions.outWidth.toDouble() / 2.5).toInt()
        val targetHeight = (bitmapOptions.outHeight.toDouble() / 2.5).toInt()
        val photoWidth = bitmapOptions.outWidth
        val photoHeight = bitmapOptions.outHeight

        val scalingFactor = max(1, min(photoWidth / targetWidth, photoHeight / targetHeight))

        bitmapOptions.inJustDecodeBounds = false
        bitmapOptions.inSampleSize = scalingFactor

        val bitmap = BitmapFactory.decodeFile(pictureData.uri, bitmapOptions)
        imageView.setImageBitmap(bitmap)

        val timestampTextView = view.findViewById<TextView>(R.id.marker_timestamp)
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = pictureData.time
        timestampTextView.text = DateFormats.IMAGE_MAP_MARKER_FORMATTER.format(calendar.time)

        return view
    }
}