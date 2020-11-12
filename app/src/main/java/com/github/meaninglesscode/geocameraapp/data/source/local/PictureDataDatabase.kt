package com.github.meaninglesscode.geocameraapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.meaninglesscode.geocameraapp.data.PictureData

/**
 * [RoomDatabase] containing the "picture_data" table. The database entities are set to be
 * represented by the [PictureData] class. Implemented by the abstract class [PictureDataDatabase]
 * implementing [RoomDatabase].
 */
@Database(entities = [PictureData::class], version = 1, exportSchema = false)
abstract class PictureDataDatabase: RoomDatabase() {
    /**
     * Allows for retrieval and use of the [PictureDataDao] from the [PictureDataDatabase].
     */
    abstract fun pictureDataDao(): PictureDataDao
}