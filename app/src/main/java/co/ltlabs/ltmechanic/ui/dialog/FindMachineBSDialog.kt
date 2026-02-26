package co.ltlabs.ltmechanic.ui.dialog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject
import javax.inject.Inject

class FindMachineBSDialog : BaseBSDialog<PopupScanOptionsBinding>() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var doneListener: ((data: String) -> Unit)? = null

    override fun getLayoutId() = R.layout.popup_scan_options

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.jTranslate = languageJsonObject
        setupListener()
    }

    fun onDoneListener(doneListener: (data: String) -> Unit) =
        apply {
            this.doneListener = doneListener
        }

    private fun setupListener() {
        binding.closePopup.setOnClickListener { dismiss() }
        binding.btnSubmitMachine.setOnClickListener {
            val text = binding.machineEditText.text.toString()
            if (text.isNotEmpty()) {
                doneListener?.invoke(text)
                dismiss()
            }
        }
        binding.btnScanCamera.setOnClickListener {
            startCameraScan()
        }
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            doneListener?.invoke(result.contents)
            dismiss()
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                barcodeLauncher.launch(ScanOptions())
            }
        }

    private fun startCameraScan() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            barcodeLauncher.launch(ScanOptions())
        }
    }

}