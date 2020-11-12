package com.github.meaninglesscode.geocameraapp.data.source

import com.github.meaninglesscode.geocameraapp.data.PictureData
import com.github.meaninglesscode.geocameraapp.data.Result

/**
 * Interface for [PictureData] data repositories to implement. Contains methods for interaction with
 * the associated table to be implemented in other repositories.
 */
interface PictureDataRepository {
    /**
     * Method to be implemented in order to get a [List] of all [PictureData] within the Room
     * database.
     *
     * @return [Result] containing a [List] of [PictureData] or a [Result.Error] if an error was
     * encountered
     */
    suspend fun getAllPictureData(): Result<List<PictureData>>

    /**
     * Method to be implemented in order to get [PictureData] from the Room database by its
     * associated URI.
     *
     * @param [uri] [String] representing the uniquely identifying [uri] associated with the desired
     * [PictureData]
     * @return [Result] containing the returned [PictureData] or a [Result.Error] if an error was
     * encountered
     */
    suspend fun getPictureData(uri: String): Result<PictureData>

    /**
     * Method to be implemented in order to save the given [PictureData] into the Room database.
     *
     * @param [pictureData] [PictureData] to save into the Room database
     */
    suspend fun savePictureData(pictureData: PictureData)

    /**
     * Method to be implemented in order to clear all [PictureData] associated with files that have
     * since been deleted from disk.
     */
    suspend fun clearDeletedPictureData()

    /**
     * Method to be implemented in order to clear all [PictureData] from the Room database.
     */
    suspend fun deleteAllPictureData()

    /**
     * Method to be implemented in order to delete all [PictureData] from the Room database with the
     * given URI. Should always delete a single [PictureData] since URI is unique.
     *
     * @param [uri] [String] representing the uniquely identifying [uri] associated with the desired
     * [PictureData] to delete
     */
    suspend fun deletePictureData(uri: String)
}