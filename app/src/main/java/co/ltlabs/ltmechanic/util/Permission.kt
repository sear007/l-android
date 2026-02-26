package co.ltlabs.ltmechanic.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log

class Permission {

    private val TAG = "Permission"

    fun setUpReadExternalStorage(activity: Activity) {
        try {
            val permission = ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission to read denied")
                makeRequestReadExternalStorage(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUpWriteExternalStorage(activity: Activity) {
        try {
            val permission = ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission to read denied")
                makeRequestWriteExternalStorage(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun accessNFC(activity: Activity) {
        try {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.NFC),
                RECORD_REQUEST_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun accessFineLocation(activity: Activity) {
        try {
            try {
                val permission = ContextCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        RECORD_REQUEST_CODE
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun accessCoarseLocation(activity: Activity) {
        try {
            try {
                val permission = ContextCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        RECORD_REQUEST_CODE
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeRequestReadExternalStorage(activity: Activity) {
        try {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeRequestWriteExternalStorage(activity: Activity) {
        try {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val RECORD_REQUEST_CODE = 101
        const val WRITE_EXTERNAL_STORAGE_CODE = 102
    }
}