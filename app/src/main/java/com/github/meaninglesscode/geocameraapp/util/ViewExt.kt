package com.github.meaninglesscode.geocameraapp.util

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar

/**
 * This extension method makes [Snackbar.make] into an extension method for [View]s.
 */
fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).run {
        show()
    }
}

/**
 * This extension method triggers a [Snackbar] message when the value contained by the
 * snackbarTaskMessageLiveEvent is modified.
 */
fun View.setupSnackbar(
    lifecycleOwner: LifecycleOwner, snackbarEvent: LiveData<Event<Int>>, timeLength: Int
) {
    snackbarEvent.observe(lifecycleOwner, { event ->
        event.getContentIfNotHandled()?.let {
            showSnackbar(context.getString(it), timeLength)
        }
    })
}