package co.ltlabs.ltmechanic.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.AppConfig.APP_ALIAS_NAME
import com.ltlabs.lt_core.ui.ApkDownloaderDialog
import com.ltlabs.lt_core.ui.LtDownloadDialog

object UpdateManager {

    fun downloadNewApk(activity: Activity, apkLink: String, latestVersion: String) {

        try {
            val permission = ContextCompat.checkSelfPermission(
                activity.applicationContext,  Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Permission.WRITE_EXTERNAL_STORAGE_CODE
                )
            } else {
                val dialog = ApkDownloaderDialog(activity)
                dialog.setButtonAcceptLabel(ColorStateList.valueOf(activity.resources.getColor(R.color.blue_400)))
                dialog.show(apkLink, latestVersion, APP_ALIAS_NAME, activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}