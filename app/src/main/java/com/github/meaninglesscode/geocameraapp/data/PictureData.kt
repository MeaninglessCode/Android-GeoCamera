package com.github.meaninglesscode.geocameraapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

/**
 * Data class to contain all [PictureData] related data. This class is annotated as an [Entity] for
 * use with Room. The table name associated with the Room database is "picture_data".
 *
 * @param [uri] [String] representing the unique identifier associated with the given [PictureData].
 * The column name of [uri] is "uri"
 * @param [latitude] [Double] representing the [latitude] associated with the given [PictureData].
 * The column name of [latitude] is "latitude"
 * @param [longitude] [Double] representing the [longitude] associated with the given [PictureData].
 * The column name of [longitude] is "longitude"
 * @param [time] [Long] representing the [time] at which the associated Picture was taken. The
 * column name of [time] is "time"
 * @param [displayedOnMap] [Boolean] representing whether or not the [PictureData] has had a marker
 * generated and displayed on the Google Map for it. This value is not stored in the Room database.
 */
@Entity(tableName = "picture_data")
data class PictureData @JvmOverloads constructor(
        @PrimaryKey @ColumnInfo(name = "uri") var uri: String = "",
        @ColumnInfo(name = "latitude") var latitude: Double = 0.0,
        @ColumnInfo(name = "longitude") var longitude: Double = 0.0,
        @ColumnInfo(name = "time") var time: Long = 0,
        @Ignore var displayedOnMap: Boolean = false
) {
    /**
     * Helper method to return the [latitude] and [longitude] together as a [LatLng] object.
     */
    fun getLatLng() = LatLng(latitude, longitude)
}