package com.github.meaninglesscode.geocameraapp.data.source.local

import com.github.meaninglesscode.geocameraapp.data.PictureData
import com.github.meaninglesscode.geocameraapp.data.Result
import com.github.meaninglesscode.geocameraapp.data.source.PictureDataDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [PictureDataLocalDataSource] class implementing the [PictureDataDataSource] interface for
 * interaction with the associated database.
 *
 * @param [pictureDataDao] [PictureDataDao] for interaction with the associated database
 * @param [ioDispatcher] [CoroutineDispatcher] for launching tasks in a nonblocking way
 */
class PictureDataLocalDataSource internal constructor(
    private val pictureDataDao: PictureDataDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): PictureDataDataSource {
    /**
     * Simple method exposing the [pictureDataDao] getAllPictureData method to get all [PictureData]
     * from the table and return them wrapped in a [Result].
     *
     * @return [Result.Success] containing a [List] of [PictureData] returned from the table. If
     * there is an error, then a [Result.Error] is returned instead.
     */
    override suspend fun getAllPictureData(): Result<List<PictureData>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(pictureDataDao.getAllPictureData())
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Simple method exposing the [pictureDataDao] getPictureDataByUri method to get the
     * [PictureData] with a URI of [uri] form the table and return it wrapped in a [Result].
     *
     * @param [uri] [String] representing the URi of the [PictureData] to fetch from the table
     * @return [Result.Success] containing a [PictureData] returned from the table. If there is an
     * error, then a [Result.Error] is returned instead.
     */
    override suspend fun getPictureData(uri: String): Result<PictureData> = withContext(ioDispatcher) {
        return@withContext try {
            val picture = pictureDataDao.getPictureDataByUri(uri)

            when {
                picture != null -> Result.Success(picture)
                else -> Result.Error(Exception("Picture not found!"))
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Simple method exposing the [PictureDataDao] insertPictureData method.
     *
     * @param [pictureData] [PictureData] to insert into the table
     */
    override suspend fun savePictureData(pictureData: PictureData) = withContext(ioDispatcher) {
        pictureDataDao.insertPictureData(pictureData)
    }

    /**
     * Method to clear all [PictureData] associated with pictures that have since been deleted
     * from disk.
     */
    override suspend fun clearDeletedPictureData() {
        TODO("Not yet implemented")
    }

    /**
     * Simple method exposing the [PictureDataDao] deleteAllPictureData method. Deletes all
     * [PictureData] from the table.
     */
    override suspend fun deleteAllPictureData() = withContext(ioDispatcher) {
        pictureDataDao.deleteAllPictureData()
    }

    /**
     * Simple method exposing the [PictureDataDao] deletePictureDataByUri method. Deletes the
     * [PictureData] with a URI matching [uri] from the table.
     *
     * @param [uri] [String] representing the URI of the [PictureData] to delete from the table
     */
    override suspend fun deletePictureData(uri: String) = withContext<Unit>(ioDispatcher) {
        pictureDataDao.deletePictureDataByUri(uri)
    }
}