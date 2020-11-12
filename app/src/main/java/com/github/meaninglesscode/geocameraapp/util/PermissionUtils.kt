package com.github.meaninglesscode.geocameraapp.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Helper function to determine whether or not the given permission has been granted.
 *
 * @param [activity] [Activity] used for [Context] when calling [ActivityCompat.checkSelfPermission]
 * @param [permission] [String] representing the permission to check the status of
 * @return [Boolean] indicating whether or not the given permission has been granted or not
 */
fun hasPermission(activity: Activity, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Helper function to determine whether or nto all permissions in the given permissions array have
 * been granted.
 *
 * @param [activity] [Activity] used for [Context] when calling [hasPermission]
 * @param [permissions] [Array] of [String]s representing the permissions to check the status of
 * @return [Boolean] indicating whether or not all of the given permissions were granted or not
 */
fun hasPermissions(activity: Activity, permissions: Array<String>): Boolean {
    var hasAllPermissions = true

    for (permission in permissions)
        if (!hasPermission(activity, permission))
            hasAllPermissions = false

    return hasAllPermissions
}