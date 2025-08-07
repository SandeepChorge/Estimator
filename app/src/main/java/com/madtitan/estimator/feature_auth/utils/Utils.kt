package com.madtitan.estimator.feature_auth.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


fun requestStoragePermission(activity: Activity, onGranted: () -> Unit) {
    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
        onGranted()
    } else {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
    }
}
