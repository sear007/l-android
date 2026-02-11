package co.ltlabs.ltmechanic.ui.main.mechanic.setupline

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
import co.ltlabs.ltmechanic.databinding.FragmentSetupLineMachineDetailsWithScanBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineMachineInPlaceDetailsWithScanViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "DetailsWScanFragment";

class SetupLineMachineDetailsWithScanFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private val viewModel: SetupLineMachineInPlaceDetailsWithScanViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupLineMachineInPlaceDetailsWithScanViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var nfcAdapter: NfcAdapter? = null

    private val args: SetupLineMachineDetailsWithScanFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    private var existingMachine = false

    private var currentMachine = MachineInStation(0, "", "", "", "")
    private var existingMachineInPlace = ""
    private var existingMachineInPlaceId = 0L

    private var nextMachineA = MachineInStation(0, "", "", "", "")

    private var confirmClicked = false

    private var machineAlreadyInStation = false

    private var linePlacesEmpty = false

    private var keepEmptyClicked = false

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        existingMachine = args.existingMachine


        val binding = FragmentSetupLineMachineDetailsWithScanBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel



        if (args.rfid.isBlank()) {
            binding.placeTextView4.visibility = View.GONE
            binding.machineIDTextView.visibility = View.GONE
        }

        machineViewModel.currentMfgLineId = args.mfgLineId

        coordinatorLayout = binding.coordinatorLayout

//        binding.placeNoTextView.text = args.station
//        binding.machineNoTextView.text = args.machine
//        binding.machineCodeTextView.text = args.machine
//        binding.machineIDTextView.text = args.rfid
//        binding.machineSubTypeTextView.text = args.subType
//        binding.machineIDTextView.text = "1"

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeNoTextView.text = getTranslation(args.station)
                machineNoTextView.text = getTranslation(args.machine)
                machineCodeTextView.text = getTranslation(args.machine)
                machineIDTextView.text = getTranslation(args.rfid)
                machineSubTypeTextView.text = getTranslation(args.subType)
                btnScan.text = getTranslation(btnScan.text.toString())
                btnKeepEmpty.text = getTranslation(btnKeepEmpty.text.toString())
                btnCancelLineSetup.text = getTranslation(btnCancelLineSetup.text.toString())
                btnConfirmLineSetup.text = getTranslation(btnConfirmLineSetup.text.toString())
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                typeTextView.text = getTranslation(typeTextView.text.toString())

            }
        }

        machineViewModel.getNextMachineAByStation("${args.station}-A", args.mfgLineId)

        machineViewModel.nextMachineA.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "onCreateView: nextMachineA: $it")
            if (it != null) {
                nextMachineA = it
            }

        })

        progressBar = binding.progressBar

        machineViewModel.getMachineByStation(args.station, args.mfgLineId)

        machineViewModel.machine.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {
                existingMachineInPlace = machine.station
                existingMachineInPlaceId = machine.id
            }
        })

        machineViewModel.machinesInStation.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "onCreateView: it: $it")
            linePlacesEmpty = it == null

            if (it == null) {
                navigateToSetupLine()
            } else {
                Log.d(TAG, "onCreateView: navigateToSetupLinePlaces: ${it == null}")
                navigateToSetupLinePlaces()
            }
        })

        machineViewModel.machineInStationCount.observe(viewLifecycleOwner, Observer {machineCount ->

            Log.d(TAG, "onCreateView: machineCount: $machineCount")

//            if (machineCount > 1) {
//                binding.btnConfirmOpenALineSetup.visibility = View.INVISIBLE
//            } else {
//                binding.btnConfirmOpenALineSetup.visibility = View.VISIBLE

                binding.btnConfirmOpenALineSetup.visibility = if (args.station.contains("-A")) View.INVISIBLE else View.VISIBLE

            binding.btnConfirmOpenALineSetup.visibility = if (args.showNextButton) View.VISIBLE else View.INVISIBLE
//            }
        })

//        binding.btnConfirmOpenALineSetup.visibility = if (args.station.contains("-A")) View.INVISIBLE else View.VISIBLE

        binding.btnConfirmOpenALineSetup.text = "${languageJsonObject.getTranslation("CONFIRM AND OPEN")} ${args.station}-A"

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

                existingMachine = false

                currentMachine.id = machineByNo.id.toLong()
                binding.machineNoTextView.text = machineByNo.machine
                binding.machineCodeTextView.text = machineByNo.machine

                machineByNo.rfid?.let {
                    binding.machineIDTextView.visibility = if (it.isBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    binding.placeTextView4.visibility = if (it.isBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }


                binding.machineIDTextView.text = machineByNo.rfid
                binding.machineSubTypeTextView.text = machineByNo.subtype

                machineAlreadyInStation = machineByNo.station.isNotBlank() || machineByNo.station == "0"
                Log.d(TAG, "onCreateView: machineAlreadyInStation: $machineAlreadyInStation")

                dismissPopup()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machineByNo ->
            if (machineByNo != null) {

                MachineUtil.machineNo = machineByNo.machine
                MachineUtil.machineArea = machineByNo.area
                MachineUtil.machineLocation = if (machineByNo.area.toLowerCase().contains("prod")) {
                    "${machineByNo.mfgLine} - ${machineByNo.station}"
                } else {
                    machineByNo.area
                }
                MachineUtil.machineHasOpenTickets = machineByNo.hasOpenTicket

                existingMachine = false

                currentMachine.id = machineByNo.id.toLong()
                binding.machineNoTextView.text = machineByNo.machine
                binding.machineCodeTextView.text = machineByNo.machine

                Log.d(TAG, "onCreateView: machineByNo.rfid: ${machineByNo.rfid}")

                binding.machineIDTextView.visibility = if (machineByNo.rfid == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                binding.placeTextView4.visibility = if (machineByNo.rfid == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                machineByNo.rfid?.let {
                    binding.machineIDTextView.visibility = if (it.isBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    binding.placeTextView4.visibility = if (it.isBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

                binding.machineIDTextView.text = machineByNo.rfid
                binding.machineSubTypeTextView.text = machineByNo.subtype

                machineAlreadyInStation = machineByNo.station.isNotBlank() || machineByNo.station == "0"
                Log.d(TAG, "onCreateView: machineAlreadyInStation: $machineAlreadyInStation")

                dismissPopup()
            }
        })

        binding.btnConfirmLineSetup.setOnClickListener {

            confirmClicked = true
            keepEmptyClicked = false
//
//            if (!MachineUtil.machineArea.toLowerCase().contains("floating")) {
//                val dm = DisplayMetrics()
//                activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//
//                val width = (dm.widthPixels * .9).toInt()
//                val height = (dm.heightPixels * .4).toInt()
//
//                dismissPopup()
//                popupWindow = showCheckInConfirmationPopupWindow()
//                popupWindow?.isOutsideTouchable = true
//                popupWindow?.isFocusable = true
//                popupWindow?.update(0, 0, width, height)
//                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)
//
//                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//
//            } else {
//
//
//            }

            if (!existingMachine) {

//                if (!machineAlreadyInStation) {

//                    if (existingMachineInPlace.isNotBlank()) {
////                        val machineCheckOutRequest = MachineCheckInRequest(
////                            existingMachineInPlaceId,
////                            "",
////                            null,
////                            DateTime(DateTimeZone.UTC).toString())
//                        machineViewModel.checkOutMachine(existingMachineInPlaceId)
//                    }

//                    val machineCheckInRequest = MachineCheckInRequest(
//                        currentMachine.id,
//                        binding.placeNoTextView.text.toString(),
//                        args.mfgLineId,
//                        DateTime(DateTimeZone.UTC).toString())
//                    machineViewModel.checkInMachine(machineCheckInRequest)

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
                    machineViewModel.replaceMachine(
                        existingMachineInPlaceId,
                        args.machineID,
                        args.station,
                        args.mfgLineId
                    )
                }
//                } else {
//                    Toast.makeText(activity, "This machine number is already in place", Toast.LENGTH_SHORT).show()
//                }
            } else {
                navigateToSetupLinePlaces()
            }

        }

        binding.btnConfirmOpenALineSetup.setOnClickListener {

            confirmClicked = false
            keepEmptyClicked = false


            if (!existingMachine) {

                if (existingMachineInPlace.isNotBlank()) {
//                    val machineCheckOutRequest = MachineCheckInRequest(
//                        existingMachineInPlaceId,
//                        "",
//                        null,
//                        DateTime(DateTimeZone.UTC).toString())
//                    machineViewModel.checkOutMachine(existingMachineInPlaceId)
                }

//                val machineCheckInRequest = MachineCheckInRequest(
//                    currentMachine.id,
//                    binding.placeNoTextView.text.toString(),
//                    args.mfgLineId,
//                    DateTime(DateTimeZone.UTC).toString())
//                machineViewModel.checkInMachine(machineCheckInRequest)

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
                    machineViewModel.replaceMachine(
                        0,
                        args.machineID,
                        args.station,
                        args.mfgLineId
                    )
                }



            } else {
                if (nextMachineA.machine.isNotBlank()) {
                    navigateToSelf(true,
                        nextMachineA.id,
                        nextMachineA.machine,
                        nextMachineA.station,
                        nextMachineA.rfid,
                        args.mfgLine,
                        args.mfgLineId, args.subType, args.showNextButton)
                } else {
                    navigateToScanMachine(args.mfgLine, "${args.station}-A", args.mfgLineId)
                }
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

        binding.btnKeepEmpty.setOnClickListener {
            keepEmptyClicked = true
            if (args.machine.isNotBlank()) {

//                val machineCheckOutRequest = MachineCheckInRequest(
//                    args.machineID,
//                    "",
//                    null,
//                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkOutMachine(args.machineID)
            } else {


                navigateToSetupLinePlaces()
            }
        }

        binding.btnCancelLineSetup.setOnClickListener {
            navigateToSetupLinePlaces()
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
                    showProgressBar(true)
                } else -> {
                showProgressBar(false)
            }
            }
        })

        machineViewModel.machineCheckOutStatus.observe(viewLifecycleOwner, Observer { checkOutStatus ->
            when (checkOutStatus) {
                MachineCheckoutStatus.SUCCESS -> {
                    machineViewModel.getMachinesInStation(args.mfgLineId)
                    if (keepEmptyClicked) {
//                        coordinatorLayout.showSnackbar("Station/Place has been emptied")
                    }

                    Log.d(TAG, "onCreateView: linePlacesEmpty: $linePlacesEmpty")
                }
                else -> {

                }
            }
        })

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            when (checkInStatus) {
                MachineCheckinStatus.SUCCESS -> {

//                    machineViewModel.getMachinesInStation(args.mfgLineId)

                    if (confirmClicked) {

                        if (existingMachineInPlace.isNotBlank()) {
//                        val machineCheckOutRequest = MachineCheckInRequest(
//                            existingMachineInPlaceId,
//                            "",
//                            null,
//                            DateTime(DateTimeZone.UTC).toString())
//                            machineViewModel.checkOutMachine(existingMachineInPlaceId)
                        }

//                        navigateToSetupLine()
                            navigateToSetupLinePlaces()

                    } else {

                        Log.d(TAG, "onCreateView: linePlacesEmpty: $linePlacesEmpty")

                        if (nextMachineA.machine.isNotBlank()) {
                            navigateToSelf(true,
                                nextMachineA.id,
                                nextMachineA.machine,
                                nextMachineA.station,
                                nextMachineA.rfid,
                                args.mfgLine,
                                args.mfgLineId, args.subType,args.showNextButton)
////                            if (!linePlacesEmpty) {
////                                navigateToSetupLinePlaces()
////                            } else {
////                                navigateToSetupLine()
////                            }
//
                        } else {
//                            if (!linePlacesEmpty) {
//                                navigateToSetupLinePlaces()
//                            } else {
//                                navigateToSetupLine()
//                            }
                            navigateToScanMachine(args.mfgLine, "${args.station}-A", args.mfgLineId)
                        }

                    }

                }
                MachineCheckinStatus.MACHINE_NOT_WORKING -> {
                    coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "This Machine is Retired/Not Available"
                        )
                    )
                }
                MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE -> {
                    coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "This machine number is already in place"
                        )
                    )
                }
                MachineCheckinStatus.NOT_IN_FLOATING_AREA -> {
                    coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "Cannot check in. Machine is not in Floating area"
                        )
                    )
                }
                MachineCheckinStatus.USER_ON_FLOATING_AREA_ASSIGNED -> {
                    coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "Cannot checkout!! User is not assigned to Floating area"
                        )
                    )
                }
                MachineCheckinStatus.HAS_OPEN_TICKETS -> {
                    coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "Cannot check in. Machine has open tickets"
                        )
                    )
                }

                else -> {
                    coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Check in: Something went wrong"))
                }
            }
        })

        return binding.root
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

    fun startCameraScan(view: View) {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("")
        integrator.initiateScan()
    }

    private fun showPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_scan_options, null)
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        // Start translation
        with(languageJsonObject) {
            kotlin.with(binding) {
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
        // End translation

        binding.btnScanCamera.setOnClickListener {
            startCameraScan(it)
        }

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            dismissPopup()
        }

        binding.btnSubmitMachine.setOnClickListener {
//            dismissPopup()

//            val selectedLine = "YTI-02"
//            val machineNo = "BLKJ 382976"
//            val place = "01"
//            val machineCode = "MC 254"
//            val machineID = "31546256"
//
//            navigateToSetupLineMachineDetails(selectedLine, place, machineNo, machineCode, machineID)

            activity?.hideKeyboard()

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

    private fun showCheckInConfirmationPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupCheckInConfirmationMessageBinding.inflate(inflater)

        if (MachineUtil.machineHasOpenTickets) {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(languageJsonObject
                    .getTranslation(
                        "This machine has open Repair Ticket [] and is currently in []. Do you want to move this machine on []?"
                    ))
                    .format(MachineUtil.machineOpenTicketNo, MachineUtil.machineLocation, "${args.mfgLine} - ${args.station}")
        } else {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(languageJsonObject
                    .getTranslation(
                        "This machine is currently in []. Do you want to move this machine on [] ?"
                    ))
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
//            if (confirmClicked) {
//                val machineCheckInRequest = MachineCheckInRequest(
//                    currentMachine.id,
//                    args.station,
//                    args.mfgLineId,
//                    DateTime(DateTimeZone.UTC).toString())
//                machineViewModel.checkInMachine(machineCheckInRequest)
//            } else {
//                val machineCheckInRequest = MachineCheckInRequest(
//                    currentMachine.id,
//                    args.station,
//                    args.mfgLineId,
//                    DateTime(DateTimeZone.UTC).toString())
//                machineViewModel.checkInMachine(machineCheckInRequest)
//            }
            machineViewModel.replaceMachine(
                existingMachineInPlaceId,
                args.machineID,
                args.station,
                args.mfgLineId
            )
            MachineUtil.clear()
        }

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()
        }

//        LineUtil.machinesHasTickets.add(
//            ClearLineErrorMachine(
//                "MCP0001",
//                "01"
//            )
//        )
//        LineUtil.machinesHasTickets.add(
//            ClearLineErrorMachine(
//                "MCP0002",
//                "02"
//            )
//        )


        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            val barCode = result?.contents
            machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToSetupLinePlaces() {
        val action = SetupLineMachineDetailsWithScanFragmentDirections
            .actionSetupLineMachineDetailsWithScanFragmentToSetupLinePlacesFragment(
                args.mfgLineId,
                args.mfgLine
            )
        navigate(action)
    }

    private fun navigateToSelf(existingMachine: Boolean, machineID: Long, machine: String, station: String, rfid: String, mfgLine: String, mfgLineId: Long, subType: String, showNextButton: Boolean) {
        val action = SetupLineMachineDetailsWithScanFragmentDirections.actionSetupLineMachineDetailsWithScanFragmentSelf(
            existingMachine,
            machineID,
            machine,
            station,
            rfid,
            mfgLine,
            mfgLineId,
            subType,
            showNextButton
        )
        navigate(action)
    }

    private fun navigateToScanMachine(mfgLine: String, station: String, mfgLineId: Long) {
        val action = SetupLineMachineDetailsWithScanFragmentDirections.actionSetupLineMachineDetailsWithScanFragmentToSetupLineScanMachineFragment(mfgLine, station, mfgLineId, "linePlaces")
        navigate(action)
    }

    private fun navigateToSetupLine() {
        val action = SetupLineMachineDetailsWithScanFragmentDirections.actionSetupLineMachineDetailsWithScanFragmentToSetupLineFragment(args.mfgLine, args.mfgLineId)
        navigate(action)
    }

}
