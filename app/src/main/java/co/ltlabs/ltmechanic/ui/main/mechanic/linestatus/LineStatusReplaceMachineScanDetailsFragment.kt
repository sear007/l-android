package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.databinding.FragmentLineStatusReplaceMachineScanDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupKeepEmptyConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusReplaceMachineScanDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class LineStatusReplaceMachineScanDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusReplaceMachineScanDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusReplaceMachineScanDetailsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private var nfcAdapter: NfcAdapter? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var popupWindow: PopupWindow? = null

    lateinit var progressBar: ProgressBar

    private val args: LineStatusReplaceMachineScanDetailsFragmentArgs by navArgs()

    private var action2 = ""

    var keepEmptyClicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        val binding = FragmentLineStatusReplaceMachineScanDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        binding.machineNoTextView.text = args.machine
        binding.stationTextView.text = args.station
        binding.mfgLineTextView.text = args.mfgLine

        coordinatorLayout = binding.coordinatorLayout

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView7.text = getTranslation(placeTextView7.text.toString())
                btnScan.text = getTranslation(btnScan.text.toString())
                btnKeepEmpty.text = getTranslation(btnKeepEmpty.text.toString())
                btnCancel3.text = getTranslation(btnCancel3.text.toString())
            }
        }

        binding.btnKeepEmpty.setOnClickListener {
            keepEmptyClicked = true
            if (args.station.isNotBlank()) {

//                val checkOutRequest = MachineCheckInRequest(
//                    args.machineId,
//                    "",
//                    null,
//                    DateTime(DateTimeZone.UTC).toString()
//                )

                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .4).toInt()

                dismissPopup()
                popupWindow = showConfirmationPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

                DimUtil.dimBehind(popupWindow)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

//                machineViewModel.checkOutMachine(args.machineId, keepEmpty = true)

            } else {
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not assigned to a place."))
            }
        }

        binding.btnCancel3.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnScan.setOnClickListener {
            action2 = "scan_new"
            if (args.station.isNotBlank()) {
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
            } else {
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not assigned to a place."))
            }
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machine ->
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

                if (action2 == "scan_new") {
                    navigateToScanDetailsConfirm(
                        args.mfgLineId,
                        args.mfgLine,
                        args.machineId,
                        args.machine,
                        args.station,
                        machine.id,
                        machine.machine,
                        machine.station
                    )
                } else {
                    if (machine.station.isNotBlank()) {
                        navigateToScanDetailsConfirm(
                            args.mfgLineId,
                            args.mfgLine,
                            args.machineId,
                            args.machine,
                            args.station,
                            machine.id,
                            machine.machine,
                            machine.station
                        )
                    } else {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not in Production Line"))
                    }
                }






                machineViewModel.machineDetailsByRfidComplete()

            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

                if (action2 == "scan_new") {
                    navigateToScanDetailsConfirm(
                        args.mfgLineId,
                        args.mfgLine,
                        args.machineId,
                        args.machine,
                        args.station,
                        machine.id,
                        machine.machine,
                        machine.station
                    )
                } else {
                    if (machine.station.isNotBlank()) {
                        navigateToScanDetailsConfirm(
                            args.mfgLineId,
                            args.mfgLine,
                            args.machineId,
                            args.machine,
                            args.station,
                            machine.id,
                            machine.machine,
                            machine.station
                        )
                    } else {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not in Production Line"))
                    }
                }


                machineViewModel.machineDetailsByMachineNoComplete()

            }
        })

        machineViewModel.machineCheckOutStatus.observe(viewLifecycleOwner, Observer { checkOutStatus ->
            when (checkOutStatus) {
                MachineCheckoutStatus.SUCCESS -> {
//                    machineViewModel.getMachinesInStation(args.mfgLineId)
                    if (keepEmptyClicked) {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Station/Place has been emptied"))
                    }

                    navigateToStations()

                }
                else -> {

                }
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

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            if(checkInStatus != null) {
                when (checkInStatus) {
                    MachineCheckinStatus.SUCCESS -> {

                        navigateToStationDetails()

                    }
                }

                machineViewModel.setMachineCheckInStatusComplete()
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

        mainViewModel.insertToNfcDeviceDatabase(true)

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

    private fun showConfirmationPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupKeepEmptyConfirmationMessageBinding.inflate(inflater)

//        binding.textView3.text = getString(R.string.keep_empty_confirmation_message, args.mfgLine, args.station)
        binding.textView3.text = "${languageJsonObject.getTranslation("Are you sure you want to empty Line")} ${args.mfgLine} ${args.station}?"

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
//            machineViewModel.checkOutMachine(args.machineId, true)
            machineViewModel.checkOutMachine(args.machineId, keepEmpty = true)
        }

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()
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

    private fun navigateToStations() {
        val action = LineStatusReplaceMachineScanDetailsFragmentDirections
            .actionLineStatusReplaceMachineScanDetailsFragmentToLineStatusStationsFragment(args.mfgLineId, args.mfgLine, false, args.machine,
                args.station)
        navigate(action)
    }

    private fun navigateToStationDetails() {
        val action = LineStatusReplaceMachineScanDetailsFragmentDirections.actionLineStatusReplaceMachineScanDetailsFragmentToLineStatusStationsFragment(
            args.mfgLineId,
            args.mfgLine,
            false,
            args.machine,
            args.station
        )
        navigate(action)
    }

    private fun navigateToScanDetailsConfirm(mfgLineId: Long, mfgLine: String, machineId: Long, machine: String, station: String, machineIdToCheckIn: Long, machineToCheckIn: String, scannedMachineStation: String) {
        val action = LineStatusReplaceMachineScanDetailsFragmentDirections.actionLineStatusReplaceMachineScanDetailsFragmentToLineStatusReplaceMachineScanDetailsConfirmFragment(
            mfgLineId,
            mfgLine,
            machineId,
            machine,
            station,
            machineIdToCheckIn,
            machineToCheckIn,
            scannedMachineStation
        )
        navigate(action)
    }

}
