package co.ltlabs.ltmechanic.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.util.BoxDetector
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import java.io.IOException

class CameraScanActivity : AppCompatActivity() {

    lateinit var cameraPreview: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_scan)

        cameraPreview = findViewById(R.id.camera_preview)
        createCameraSource()
    }

    private fun createCameraSource() {
        val barcodeDetector = BarcodeDetector.Builder(this).build()
        val boxDetector = BoxDetector(barcodeDetector, 350, 350)

        val cameraSource = CameraSource.Builder(this, boxDetector)
            .setAutoFocusEnabled(true)
            .setRequestedPreviewSize(1600, 1024)
            .build()

        cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }

                try {
                    cameraSource.start(cameraPreview.holder)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }

        })

        boxDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                TODO("Not yet implemented")
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                val barcodes: SparseArray<Barcode>? = detections?.detectedItems ?: SparseArray()
                if (barcodes!!.size() > 0) {
                    val intent = Intent()
                    intent.putExtra("barcode", barcodes.valueAt(0))
                    setResult(CommonStatusCodes.SUCCESS, intent)
                    finish()
                }
            }

        })
    }
}
