package co.ltlabs.ltmechanic.ui.main.mechanic.replacemachine

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentReplaceMachineBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.ui.main.CameraScanActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.ReplaceMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.appbar.AppBarLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "ReplaceMachineFragment";

class ReplaceMachineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: ReplaceMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReplaceMachineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private var popupWindow: PopupWindow? = null

    lateinit var progressBar: ProgressBar

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onPause() {
        super.onPause()
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        activity?.findViewById<AppBarLayout>(R.id.toolbar_layout)?.visibility = View.VISIBLE
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        activity?.findViewById<AppBarLayout>(R.id.toolbar_layout)?.visibility = View.GONE
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentReplaceMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        coordinatorLayout = binding.coordinatorLayout

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView5.text = getTranslation(placeTextView5.text.toString())
                btnScan.text = getTranslation(btnScan.text.toString())

            }
        }

        binding.btnScan.setOnClickListener {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .7).toInt()

            dismissPopup()
            popupWindow = showPopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            DimUtil.dimBehind(popupWindow)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0
                navigateToScanDetails(mfgLineId.toLong(), machine.mfgLine ?: "", machine.station, machine.machine, machine.id)
                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    dismissPopup()
                    showProgressBar(false)
                }
            }
        })

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
            when (machineStatus) {
                MachineStatus.FOUND -> {

                } else -> {
                dismissPopup()

                if (MachineUtil.message.isNotBlank()) {
                    binding.coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "You do not have access to the machine's current location"
                        )
                    )
                } else {
                    binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine number not found"))
                }
            }
            }
        })

        return binding.root

    }

    private fun startCameraScan() {

    //if (MainUtil.googlePlayAvailable) {
    //val intent = Intent(activity, CameraScanActivity::class.java)
    //startActivityForResult(intent, 0)
//} else {
    val integrator = IntentIntegrator.forSupportFragment(this)
    integrator.setPrompt("")
    integrator.initiateScan()
//}

}

    private fun showPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        with(languageJsonObject) {
            with(binding) {
                labelReadyToScan.text = getTranslation(labelReadyToScan.text.toString())
                machineEditText.hint = getTranslation(machineEditText.hint.toString())
                btnScanCamera.text = getTranslation(btnScanCamera.text.toString())
                labelTitleScanNFC.text = getTranslation(labelTitleScanNFC.text.toString())
                labelNFCDescription.text = getTranslation(labelNFCDescription.text.toString())
                labelTitleScanBarcode.text = getTranslation(labelTitleScanBarcode.text.toString())
                labelBarcodeDescription.text = getTranslation(labelBarcodeDescription.text.toString())
            }
        }

        binding.btnScanCamera.setOnClickListener {
            startCameraScan()
        }

        closeButton.setOnClickListener {
            dismissPopup()
        }

        binding.btnSubmitMachine.setOnClickListener {
            machineViewModel.getMachineByMachineNo(binding.machineEditText.text.toString())

        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showProgressBar(visible: Boolean) {
        with(progressBar) {
            visibility = if (visible) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            val barCode = result?.contents
            machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun navigateToScanDetails(mfgLineId: Long, mfgLine: String, station: String, machine: String, machineId: Long) {
        val action = ReplaceMachineFragmentDirections.actionReplaceMachineFragmentToReplaceMachineScanDetailsFragment(
            mfgLineId,
            mfgLine,
            station,
            machine,
            machineId
        )
        navigate(action)
    }

}
