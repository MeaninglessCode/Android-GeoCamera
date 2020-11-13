package com.github.meaninglesscode.geocameraapp.data.source.local

import androidx.room.*
import com.github.meaninglesscode.geocameraapp.data.PictureData

/**
 * [Dao] (Data Access Object) for [PictureData]. Used for interaction with the [Room] database.
 */
@Dao
interface PictureDataDao {
    /**
     * Method to select and return all [PictureData] from the "picture_data" table.
     *
     * @return [List] of all [PictureData] in the table
     */
    @Query("SELECT * FROM picture_data")
    suspend fun getAllPictureData(): List<PictureData>

    /**
     * Method to select and return the [PictureData] from the "picture_data" table that matches the
     * given [uri].
     *
     * @param [uri] [String] representing the URI of the [PictureData] to retrieve from the table
     * @return [PictureData]? returned from the table if there was data matching [uri]. If not, then
     * the returned data is null
     */
    @Query("SELECT * FROM picture_data WHERE uri = :uri")
    suspend fun getPictureDataByUri(uri: String): PictureData?

    /**
     * Insert a new [PictureData] into the "picture_data" table. If there is a conflict, then the
     * conflicting data is replaced by the new data.
     *
     * @param [pictureData] [PictureData] to insert into the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictureData(pictureData: PictureData)

    /**
     * Update the [pictureData] in the "picture_data" table matching the given [pictureData] to the
     * new values contained within the [PictureData] object.
     *
     * @param [pictureData] [PictureData] whose values are to be updated
     * @return [Int] representing the number of data updated (should always be 1)
     */
    @Update
    suspend fun updatePictureData(pictureData: PictureData): Int

    /**
     * Delete all [PictureData] from the "picture_data" table where the URI of the row is not
     * contained in [pictureUris].
     *
     * @param [pictureUris] [List] of [String] URIs to check against
     */
    @Query("DELETE FROM picture_data WHERE uri NOT IN(:pictureUris)")
    suspend fun clearDeletedPictureData(pictureUris: List<String>)

    /**
     * Delete all data from the "picture_data" table with a URI matching the given [uri]. Should
     * always delete a single data because [uri] is the primary key and must be unique for data to
     * be inserted into the table.
     *
     * @param [uri] [String] representing the URI of the data to delete from the table
     * @return [Int] representing the number of data deleted (should always be 1)
     */
    @Query("DELETE FROM picture_data WHERE uri = :uri")
    suspend fun deletePictureDataByUri(uri: String): Int

    /**
     * Deletes all [PictureData] from the "picture_data" table.
     */
    @Query("DELETE FROM picture_data")
    suspend fun deleteAllPictureData()
}