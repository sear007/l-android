package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusAddMachineScanMachineDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.MachineCheckInRequest
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusAddMachineScanMachineDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "AddMDetailsFragment";

/**
 * A simple [Fragment] subclass.
 */
class LineStatusAddMachineScanMachineDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val  viewModel: LineStatusAddMachineScanMachineDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusAddMachineScanMachineDetailsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private lateinit var progressBar: ProgressBar

    private val args: LineStatusAddMachineScanMachineDetailsFragmentArgs by navArgs()

    private var machineAlreadyInStation = false

    private var machineId = 0L
    private var machine = ""

    private var confirmAClicked = false

    private var popupWindow: PopupWindow? = null

    private var nextMachineA = MachineInStation(0, "", "", "", "")

    private lateinit var coordinatorLayout: CoordinatorLayout

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        if (args.rfId.isNotEmpty()) {
            machineViewModel.getMachineByRfid(args.rfId)
        }

        if (args.machine.isNotEmpty()) {
            machineViewModel.getMachineByMachineNo(args.machine)
        }
        val binding = FragmentLineStatusAddMachineScanMachineDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        progressBar = binding.progressBar

        binding.viewModel = viewModel

        coordinatorLayout = binding.coordinatorLayout

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView6.text = getTranslation(placeTextView6.text.toString())
                btnCancelInsert.text = getTranslation(btnCancelInsert.text.toString())
                btnConfirmInsert.text = getTranslation(btnConfirmInsert.text.toString())
            }
        }

        machineViewModel.getNextMachineAByStation("${args.station}-A", args.mfgLineId)

        binding.toolBarSelectedLineTextView2.text = args.mfgLine
        binding.placeNoTextView.text = args.station
        binding.placeTextView4.visibility = View.GONE
        binding.machineIDTextView.visibility = View.GONE

        binding.btnConfirmInsert.setOnClickListener {
            confirmAClicked = false

            if (!MachineUtil.machineArea.toLowerCase().contains("floating")) {
                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .4).toInt()

                dismissPopup()
                popupWindow = showCheckInConfirmationPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                DimUtil.dimBehind(popupWindow)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            } else {
                val machineCheckInRequest = MachineCheckInRequest(
                    machineId,
                    binding.placeNoTextView.text.toString(),
                    args.mfgLineId,
                    DateTime(DateTimeZone.UTC).toString()
                )
                machineViewModel.checkInNewMachine(machineCheckInRequest, addAction = true)
            }
        }

        binding.btnConfirmOpenALineSetup2.setOnClickListener {
            confirmAClicked = true

            if (!MachineUtil.machineArea.toLowerCase().contains("floating")) {
                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .4).toInt()

                dismissPopup()
                popupWindow = showCheckInConfirmationPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                DimUtil.dimBehind(popupWindow)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            } else {
                val machineCheckInRequest = MachineCheckInRequest(
                    machineId,
                    binding.placeNoTextView.text.toString(),
                    args.mfgLineId,
                    DateTime(DateTimeZone.UTC).toString()
                )
                machineViewModel.checkInNewMachine(machineCheckInRequest, addAction = true)
            }


        }

        binding.btnCancelInsert.setOnClickListener {
//            navigateToStations()
            activity?.onBackPressed()
        }

        machineViewModel.nextMachineA.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "onCreateView: nextMachineA: $nextMachineA")
            if (it != null) {
                if (it.machine.isNotBlank()) {
                    binding.btnConfirmOpenALineSetup2.visibility = View.INVISIBLE
                    nextMachineA = it
                }
            }
        })


        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }
                else -> {
                    progressBar.showProgressBar(false)
                }
            }
        })

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machineByNo ->
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machineByNo != null) {

                MachineUtil.machineNo = machineByNo.machine
                MachineUtil.machineArea = machineByNo.area
                MachineUtil.machineLocation = if (machineByNo.area.toLowerCase().contains("prod")) {
                    "${machineByNo.mfgLine} - ${machineByNo.station}"
                } else {
                    machineByNo.area
                }
                MachineUtil.machineHasOpenTickets = machineByNo.hasOpenTicket

                machineId = machineByNo.id

                if (machineByNo.rfid == null) {
                    binding.placeTextView4.visibility = View.GONE
                    binding.machineIDTextView.visibility = View.GONE
                } else {
                    if (machineByNo.rfid.isNotBlank()) {
                        binding.placeTextView4.visibility = View.VISIBLE
                        binding.machineIDTextView.visibility = View.VISIBLE
                    } else {
                        binding.placeTextView4.visibility = View.GONE
                        binding.machineIDTextView.visibility = View.GONE
                    }
                }

                binding.btnConfirmOpenALineSetup2.text =
                    "${languageJsonObject.getTranslation("CONFIRM AND OPEN")} ${args.station}-A"

                machine = machineByNo.machine
                binding.machineNoTextView.text = machineByNo.machine
                binding.machineCodeTextView.text = machineByNo.machine
                binding.machineIDTextView.text = machineByNo.rfid
                binding.machineSubTypeTextView.text = machineByNo.subtype

                dismissPopup()

                machineAlreadyInStation = machineByNo.station.isNotBlank()

            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(
            viewLifecycleOwner,
            Observer { machineByNo ->
                if (machineByNo != null) {

                    MachineUtil.machineNo = machineByNo.machine
                    MachineUtil.machineArea = machineByNo.area
                    MachineUtil.machineLocation =
                        if (machineByNo.area.toLowerCase().contains("prod")) {
                            "${machineByNo.mfgLine} - ${machineByNo.station}"
                        } else {
                            machineByNo.area
                        }
                    MachineUtil.machineHasOpenTickets = machineByNo.hasOpenTicket

                    machineId = machineByNo.id

                    if (machineByNo.rfid == null) {
                        binding.placeTextView4.visibility = View.GONE
                        binding.machineIDTextView.visibility = View.GONE
                    } else {
                        if (machineByNo.rfid.isNotBlank()) {
                            binding.placeTextView4.visibility = View.VISIBLE
                            binding.machineIDTextView.visibility = View.VISIBLE
                        } else {
                            binding.placeTextView4.visibility = View.GONE
                            binding.machineIDTextView.visibility = View.GONE
                        }
                    }

                    binding.btnConfirmOpenALineSetup2.text =
                        "${languageJsonObject.getTranslation("CONFIRM AND OPEN")} ${args.station}-A"

                    machine = machineByNo.machine
                    binding.machineNoTextView.text = machineByNo.machine
                    binding.machineCodeTextView.text = machineByNo.machine
                    binding.machineIDTextView.text = machineByNo.rfid
                    binding.machineSubTypeTextView.text = machineByNo.subtype

                    dismissPopup()

                    machineAlreadyInStation = machineByNo.station.isNotBlank()

                }
            })

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
            when (machineStatus) {
                MachineStatus.FOUND -> {

                }
                else -> {
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
                    MachineUtil.mchineNotFound = true
                    activity?.onBackPressed()
                }
            }
        })

        machineViewModel.machineCheckInStatus.observe(
            viewLifecycleOwner,
            Observer { checkInStatus ->
                if (checkInStatus != null) {
                    when (checkInStatus) {
                        MachineCheckinStatus.SUCCESS -> {

                            if (confirmAClicked) {
                                navigateToMachineA()
                            } else {
                                navigateToStations(true, machine, args.station)
                            }

                        }
                        MachineCheckinStatus.MACHINE_NOT_WORKING -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "This Machine is Retired/Not Available"
                                )
                            )
                        }
                        MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "This machine number is already in place"
                                )
                            )
                        }
                        MachineCheckinStatus.NOT_IN_FLOATING_AREA -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Cannot check in. Machine is not in Floating area"
                                )
                            )
                        }
                        MachineCheckinStatus.HAS_OPEN_TICKETS -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Cannot check in. Machine has open tickets"
                                )
                            )
                        }
                        else -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Check in: Something went wrong"
                                )
                            )
                        }

                    }

                    machineViewModel.setMachineCheckInStatusComplete()
                }
            })

        return binding.root
    }

    private fun showPopupWindow(view: View) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .7).toInt()

        dismissPopup()
        popupWindow = getPopupWindow()
        popupWindow?.isOutsideTouchable = true

        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.let {
                    if (it.x < 0 || it.x > width) return true
                    if (it.y < 0 || it.y > height) return true
                }

                return false
            }

        })

        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, -25)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
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
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        val barCode = result?.contents
        machineViewModel.getMachineByMachineNo(barCode.toString())

//        }

        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun showCheckInConfirmationPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupCheckInConfirmationMessageBinding.inflate(inflater)

        if (MachineUtil.machineHasOpenTickets) {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(
                    languageJsonObject
                        .getTranslation(
                            "This machine has open Repair Ticket [] and is currently in []. Do you want to move this machine on []?"
                        )
                )
                    .format(
                        MachineUtil.machineOpenTicketNo,
                        MachineUtil.machineLocation,
                        "${args.mfgLine} - ${args.station}"
                    )
        } else {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(
                    languageJsonObject
                        .getTranslation(
                            "This machine is currently in []. Do you want to move this machine on [] ?"
                        )
                )
                    .format(MachineUtil.machineLocation, "${args.mfgLine} - ${args.station}")
        }


        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            dismissPopup()
            val machineCheckInRequest = MachineCheckInRequest(
                machineId,
                args.station,
                args.mfgLineId,
                DateTime(DateTimeZone.UTC).toString()
            )
            machineViewModel.checkInNewMachine(machineCheckInRequest, addAction = true)
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

    private fun getPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        binding.btnScanCamera.setOnClickListener {
            startCameraScan()
        }

//        closeButton.visibility = View.INVISIBLE

        with(languageJsonObject) {
            with(binding) {
                labelReadyToScan.text = getTranslation(labelReadyToScan.text.toString())
                machineEditText.hint = getTranslation(machineEditText.hint.toString())
                btnScanCamera.text = getTranslation(btnScanCamera.text.toString())
                labelTitleScanNFC.text = getTranslation(labelTitleScanNFC.text.toString())
                labelNFCDescription.text = getTranslation(labelNFCDescription.text.toString())
                labelTitleScanBarcode.text = getTranslation(labelTitleScanBarcode.text.toString())
                labelBarcodeDescription.text =
                    getTranslation(labelBarcodeDescription.text.toString())
            }
        }

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            activity?.let {
                it.onBackPressed()
                dismissPopup()
            }
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

    private fun navigateToMachineA() {
        val action =
            LineStatusAddMachineScanMachineDetailsFragmentDirections.actionLineStatusAddMachineScanMachineDetailsFragmentToLineStatusAddMachineNextMachineAFragment(
                args.mfgLineId,
                args.mfgLine,
                "${args.station}-A"
            )
        navigate(action)
    }

    private fun navigateToStations(addMachineSuccess: Boolean, machine: String, station: String) {
        val action =
            LineStatusAddMachineScanMachineDetailsFragmentDirections.actionLineStatusAddMachineScanMachineDetailsFragmentToLineStatusStationsFragment(
                args.mfgLineId,
                args.mfgLine,
                addMachineSuccess,
                machine,
                station
            )
        navigate(action)
    }

}
