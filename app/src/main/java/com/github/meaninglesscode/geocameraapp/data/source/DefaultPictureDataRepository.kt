package com.github.meaninglesscode.geocameraapp.data.source

import com.github.meaninglesscode.geocameraapp.data.PictureData
import com.github.meaninglesscode.geocameraapp.data.Result
import com.github.meaninglesscode.geocameraapp.data.succeeded
import com.github.meaninglesscode.geocameraapp.di.ApplicationModule.PictureDataLocalDataSource
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject

/**
 * [DefaultPictureDataRepository] that implements the [PictureDataRepository] interface. The
 * [PictureDataLocalDataSource] is provided via dependency injection.
 *
 * @param [pictureDataLocalDataSource] [PictureDataDataSource] implementing class for local data
 * storage and interaction
 * @param [ioDispatcher] [CoroutineDispatcher] for completing tasks without blocking
 */
class DefaultPictureDataRepository @Inject constructor(
    @PictureDataLocalDataSource private val pictureDataLocalDataSource: PictureDataDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): PictureDataRepository {
    /**
     * [ConcurrentMap] of [String] associated to [PictureData] to be used as a local cache to avoid
     * having to call the Room database in all cases. The [String] value is the [PictureData.uri] of
     * the stored [PictureData].
     */
    private var cachedPictureData: ConcurrentMap<String, PictureData>? = null

    /**
     * Method to retrieve all [PictureData] and return them wrapped in a [Result]. This method also
     * refreshes [cachedPictureData].
     *
     * @return [Result.Success] containing a [List] of [PictureData] returned from the data source.
     * If there is an error, then a [Result.Error] is returned instead.
     */
    override suspend fun getAllPictureData(): Result<List<PictureData>> {
        return withContext(ioDispatcher) {

            cachedPictureData?.let { cachedData ->
                return@withContext Result.Success(cachedData.values.sortedBy { it.time })
            }

            val newData = fetchPictureDataFromLocal()
            (newData as? Result.Success)?.let { refreshCache(it.data) }

            cachedPictureData?.values?.let {pictureData ->
                return@withContext Result.Success(pictureData.sortedBy { it.time })
            }

            (newData as? Result.Success)?.let {
                if (it.data.isEmpty())
                    return@withContext Result.Success(it.data)
            }

            return@withContext Result.Error(Exception("Illegal state"))
        }
    }

    /**
     * Helper method to actually retrieve the [PictureData] for [getAllPictureData] from the
     * appropriate [PictureDataDataSource].
     *
     * @return [Result.Success] containing a [List] of [PictureData] returned from the
     * [pictureDataLocalDataSource]. Returns a [Result.Error] in the event of an error
     */
    private suspend fun fetchPictureDataFromLocal(): Result<List<PictureData>> {
        val localPictureData = pictureDataLocalDataSource.getAllPictureData()
        if (localPictureData.succeeded)
            return localPictureData
        return Result.Error(Exception("Error fetching local picture data"))
    }

    /**
     * Method to retrieve a [PictureData] with the given [uri] and return it wrapped in a [Result].
     *
     * @param [uri] [String] representing the URI of the [PictureData] to retrieve
     * @return [Result.Success] containing a [PictureData] returned from the data source. If there
     * is an error, then a [Result.Error] is returned instead
     */
    override suspend fun getPictureData(uri: String): Result<PictureData> {
        return withContext(ioDispatcher) {

            getCachedPictureDataWithUri(uri)?.let {
                return@withContext Result.Success(it)
            }

            val newData = fetchPictureDataFromLocal(uri)
            (newData as? Result.Success)?.let { cachePictureData(it.data) }

            return@withContext newData
        }
    }

    /**
     * Helper method to actually retrieve the [PictureData] for [getPictureData] from the
     * appropriate [PictureDataDataSource].
     *
     * @param [uri] [String] representing the URI of the [PictureData] to retrieve
     * @return [Result.Success] containing a [PictureData] returned from the data source. If there
     * is an error, then a [Result.Error] is returned instead
     */
    private suspend fun fetchPictureDataFromLocal(uri: String): Result<PictureData> {
        val localPictureData = pictureDataLocalDataSource.getPictureData(uri)
        if (localPictureData.succeeded)
            return localPictureData
        return Result.Error(Exception("Error fetching local picture data"))
    }

    /**
     * Saves the given [pictureData] into the appropriate [PictureDataDataSource] as well as
     * ensuring [cachedPictureData] coherency.
     *
     * @param [pictureData] [PictureData] to save into the [PictureDataDataSource]
     */
    override suspend fun savePictureData(pictureData: PictureData) {
        cacheAndPerform(pictureData) {
            coroutineScope {
                launch { pictureDataLocalDataSource.savePictureData(it) }
            }
        }
    }

    /**
     * Deletes all [PictureData] from the [PictureDataDataSource]s that is associated with pictures
     * that have since been deleted from disk as well as removing them from [cachedPictureData].
     */
    override suspend fun clearDeletedPictureData() {
        TODO("Not yet implemented")
    }

    /**
     * Deletes all [PictureData] from the [PictureDataDataSource]s and then clears
     * [cachedPictureData].
     */
    override suspend fun deleteAllPictureData() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { pictureDataLocalDataSource.deleteAllPictureData() }
            }
        }
    }

    /**
     * Deletes the [PictureData] with the given URI from the [PictureDataDataSource]s and then
     * removes it from [cachedPictureData] to maintain coherency.
     *
     * @param [uri] [String] representing the URI of the [PictureData] to delete
     */
    override suspend fun deletePictureData(uri: String) {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { pictureDataLocalDataSource.deletePictureData(uri) }
            }
        }
        cachedPictureData?.remove(uri)
    }

    /**
     * Gets the [PictureData] with the given [uri] from [cachedPictureData].
     *
     * @param [uri] [String] URI of the data to retrieve from [cachedPictureData]
     * @return [PictureData] from [cachedPictureData] with the given [uri]
     */
    private fun getCachedPictureDataWithUri(uri: String) = cachedPictureData?.get(uri)

    /**
     * Refreshes [cachedPictureData] with the given [pictureData].
     *
     * @param [pictureData] [List] of [PictureData] to use to refresh [cachedPictureData]
     */
    private fun refreshCache(pictureData: List<PictureData>) {
        cachedPictureData?.clear()
        pictureData.sortedBy { it.time }.forEach {
            cacheAndPerform(it) {}
        }
    }

    /**
     * Method to cache the given [data] and then perform the given [action] after the caching is
     * performed.
     *
     * @param [data] [PictureData] to be passed into [cachePictureData]
     * @param [action] Action to be performed after [cachePictureData] is completed
     */
    private inline fun cacheAndPerform(data: PictureData, action: (PictureData) -> Unit) {
        action(cachePictureData(data))
    }

    /**
     * Adds the given [data] to [cachedPictureData].
     *
     * @param [data] [PictureData] to insert into [cachedPictureData]
     * @return [PictureData] cached into [cachedPictureData]
     */
    private fun cachePictureData(data: PictureData): PictureData {
        val dataToCache = PictureData(data.uri, data.latitude, data.longitude, data.time)

        if (cachedPictureData == null)
            cachedPictureData = ConcurrentHashMap()

        cachedPictureData?.put(dataToCache.uri, dataToCache)
        return dataToCache
    }
}