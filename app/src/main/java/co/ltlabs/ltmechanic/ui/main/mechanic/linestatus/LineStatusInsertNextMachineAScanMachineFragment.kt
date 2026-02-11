package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusInsertNextMachineAScanMachineBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusInsertNextMachineAScanMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "NextMachineAScanMachine";

class LineStatusInsertNextMachineAScanMachineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusInsertNextMachineAScanMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusInsertNextMachineAScanMachineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val args: LineStatusInsertNextMachineAScanMachineFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView: ")

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        val binding = FragmentLineStatusInsertNextMachineAScanMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        binding.placeNoTextView.text = args.station
        binding.toolBarTitleTextView.text = getString(R.string.line_status_place, args.station)
        binding.toolBarSelectedLineTextView.text = args.mfgLine

        coordinatorLayout = binding.coordinatorLayout

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machine ->
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machine != null) {

                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

                navigateToScanDetails(
                    args.mfgLineId,
                    args.mfgLine,
                    args.station,
                    machine.id,
                    machine.machine,
                    machine.rfid ?: "",
                    machine.subtype ?: ""
                )

                machineViewModel.machineDetailsByRfidComplete()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {

                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

                navigateToScanDetails(
                    args.mfgLineId,
                    args.mfgLine,
                    args.station,
                    machine.id,
                    machine.machine,
                    machine.rfid ?: "",
                    machine.subtype ?: ""
                )

                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        binding.btnCancel3.setOnClickListener {
//            activity?.onBackPressed()
            machineViewModel.finishInsertToSnackBarActionDatabase()
            navigateToStations()
        }

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

                    coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine number not found"))
                }
            }
            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }
                else -> {
                    dismissPopup()
                    progressBar.showProgressBar(false)
                }
            }
        })

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

        return binding.root
    }

    private fun startCameraScan() {

        Log.d(TAG, "startCameraScan: MainUtil.googlePlayAvailable: ${MainUtil.googlePlayAvailable}")

        //if (MainUtil.googlePlayAvailable) {
    //val intent = Intent(activity, CameraScanActivity::class.java)
    //startActivityForResult(intent, 0)
//} else {
    val integrator = IntentIntegrator.forSupportFragment(this)
    integrator.setPrompt("")
    integrator.initiateScan()
//}

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //        if (MainUtil.googlePlayAvailable) {
//            if (requestCode == 0) {
//                if (resultCode == CommonStatusCodes.SUCCESS) {
//                    dismissPopup()
//                    if (data != null) {
//
//                        var barcode: Barcode? = data.getParcelableExtra("barcode")
//                        machineViewModel.getMachineByMachineNo(barcode?.displayValue.toString())
//
//                    } else {
//                        coordinatorLayout.showSnackbar(
//                            languageJsonObject.getTranslation(
//                                "No QR code found"
//                            )
////                            "No QR code found"
//                        )
//                    }
//                }
//            } else {
//
//            }
//        } else {
            val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            val barCode = result?.contents
            machineViewModel.getMachineByMachineNo(barCode.toString())

//        }

        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun showPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        mainViewModel.insertToNfcDeviceDatabase(true)

        binding.btnScanCamera.setOnClickListener {
            startCameraScan()
        }

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
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

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun navigateToScanDetails(mfgLineId: Long, mfgLine: String, station: String, machineId: Long, machine: String, rfid: String, subType: String) {
        val action = LineStatusInsertNextMachineAScanMachineFragmentDirections.actionLineStatusInsertNextMachineAScanMachineFragmentToLineStatusInsertNextMachineScanMachineDetailsFragment(
            mfgLineId,
            mfgLine,
            station,
            machineId,
            machine,
            rfid,
            subType
        )
        navigate(action)
    }

    private fun navigateToStations() {
        val action = LineStatusInsertNextMachineAScanMachineFragmentDirections
            .actionLineStatusInsertNextMachineAScanMachineFragmentToLineStatusStationsFragment(
                args.mfgLineId,
                args.mfgLine,
                false,
                "",
                ""
            )
        navigate(action)
    }

}
