package com.github.meaninglesscode.geocameraapp.maps

import android.app.Activity
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.meaninglesscode.geocameraapp.data.PictureData
import com.github.meaninglesscode.geocameraapp.data.Result
import com.github.meaninglesscode.geocameraapp.data.source.PictureDataRepository
import com.github.meaninglesscode.geocameraapp.data.succeeded
import com.github.meaninglesscode.geocameraapp.util.Constants
import com.github.meaninglesscode.geocameraapp.util.Event
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * [ViewModel] representing the screen used to view a [ToDoItem]s. The [ToDoItemViewViewModel]
 * implements [ViewModel].
 *
 * @param [toDoItemsRepository] [ToDoItemsRepository] for interacting with [ToDoItem]s. This param
 * is automatically passed via dependency injection
 */

/**
 * [ViewModel] representing the associated [MapsFragment]. The [MapsViewModel] implements
 * [ViewModel].
 *
 * @param [pictureDataRepository] [PictureDataRepository] for interaction with [PictureData] passed
 * automatically via dependency injection
 */
class MapsViewModel @Inject constructor(
    private val pictureDataRepository: PictureDataRepository
): ViewModel() {
    // Live data used for the snack bar in MapsFragment
    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    // Live data used for maintaining the picture data associated with markers drawn on the map
    private var _markerPictureDataList: MutableList<PictureData> = mutableListOf()
    private val _markerPictureData = MutableLiveData<List<PictureData>>()
    val markerPictureData: LiveData<List<PictureData>> = _markerPictureData

    /**
     * Method to call [File.createTempFile] to create a new file in the
     * [Environment.DIRECTORY_PICTURES] directory.
     *
     * @param [activity] [Activity] to use in order to get the external files directory associated
     * with [Environment.DIRECTORY_PICTURES]
     * @return The new [File] created
     */
    fun createImageFile(activity: Activity): File {
        return File.createTempFile(
                "JPEG_${Constants.DateFormats.IMAGE_FILENAME_FORMATTER.format(Date())}",
                ".jpg", activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
    }

    /**
     * Method to create a new [PictureData] objected and save it into the [pictureDataRepository]
     */
    fun savePictureData(uri: String, latitude: Double, longitude: Double, time: Long) = viewModelScope.launch{
        val newPictureData = PictureData(uri, latitude, longitude, time)
        pictureDataRepository.savePictureData(newPictureData)

        _markerPictureDataList.add(newPictureData)
        _markerPictureData.value = _markerPictureDataList
    }

    /**
     * Method to initialize [_markerPictureDataList] with all picture data retrieved from the
     * [pictureDataRepository] for the initial state of the Google map.
     */
    fun initializePictureData() = viewModelScope.launch {
        val allPictureData = pictureDataRepository.getAllPictureData()

        if (allPictureData.succeeded && (allPictureData is Result.Success)) {
            _markerPictureDataList = allPictureData.data.toMutableList()
            _markerPictureData.value = _markerPictureDataList
        }
    }

    /**
     * Method to clear all picture data from the database associated with pictures that have
     * been deleted from disk.
     */
    fun clearDeletedPictureData(activity: Activity) = viewModelScope.launch {
        val filesToCheck = mutableListOf<String>()

        activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.walk()?.forEach {
            filesToCheck.add(it.absolutePath)
        }

        pictureDataRepository.clearDeletedPictureData(filesToCheck)
        initializePictureData()
    }
}