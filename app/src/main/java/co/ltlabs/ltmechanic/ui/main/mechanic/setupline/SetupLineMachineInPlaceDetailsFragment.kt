package co.ltlabs.ltmechanic.ui.main.mechanic.setupline

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.databinding.FragmentSetupLineMachineInPlaceDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.MachineCheckInRequest
import co.ltlabs.ltmechanic.ui.main.CameraScanActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineMachineInPlaceDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "InPlaceDetailsFragment";

/**
 * A simple [Fragment] subclass.
 */
class SetupLineMachineInPlaceDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private lateinit var progressBar: ProgressBar

    val viewModel: SetupLineMachineInPlaceDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupLineMachineInPlaceDetailsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var popupWindow: PopupWindow? = null

    private val args: SetupLineMachineInPlaceDetailsFragmentArgs by navArgs()

    private var nextMachine = MachineInStation(0, "", "", "", "")
    private var nextMachineA = MachineInStation(0, "", "", "", "")

    private var currentMachine = MachineInStation(0, "", "", "", "")

    private var existingMachine = false

    private var confirmClicked = false

    private var machineAlreadyInStation = false
    var place = ""

    private lateinit var setupBinding: FragmentSetupLineMachineInPlaceDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        machineViewModel.getMachineByMachineNo(args.machineNo)

        existingMachine = args.existingMachine

        setupBinding = FragmentSetupLineMachineInPlaceDetailsBinding.inflate(inflater)
        val binding = FragmentSetupLineMachineInPlaceDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar
        binding.toolBarTitleTextView.text = languageJsonObject.getTranslation(binding.toolBarTitleTextView.text.toString())

        Log.d(TAG, "onCreateView: args.machineID ${args.machineID}")

        currentMachine.id = args.machineID.toLong()

        if (args.rfid.isBlank()) {
            binding.placeTextView4.visibility = View.GONE
            binding.machineIDTextView.visibility = View.GONE
        }

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                btnScan.text = getTranslation(btnScan.text.toString())
                btnKeepEmpty.text = getTranslation(btnKeepEmpty.text.toString())
                btnCancelLineSetup.text = getTranslation(btnCancelLineSetup.text.toString())
                btnConfirmLineSetup.text = getTranslation(btnConfirmLineSetup.text.toString())
                placeTextView6.text = getTranslation(placeTextView6.text.toString())
            }
        }

        coordinatorLayout = binding.coordinatorLayout

        binding.toolBarSelectedLineTextView2.text = args.selectedLine
        binding.placeNoTextView.text = args.place
        binding.machineNoTextView.text = args.machineNo
        binding.machineCodeTextView.text = args.machineCode
        binding.machineIDTextView.text = args.rfid
//        binding.machineIDTextView.text = "1"
        binding.machineSubTypeTextView.text = args.subType
        if (args.place.contains("-A")) {
            binding.btnConfirmOpenALineSetup.visibility = View.INVISIBLE
        } else {
            binding.btnConfirmOpenALineSetup.visibility = View.VISIBLE
        }

        machineViewModel.getMachineByStation(args.place, args.selectedLineId)

        val placeInt = args.place.replace("-A", "").toInt()
        place = if (placeInt > 9) placeInt.toString() else "0$placeInt"
        val machineSamePlace = if (placeInt > 9) "$placeInt-A" else "0$placeInt-A"
        var nextPlace = if ((placeInt + 1) > 9) "${placeInt + 1}" else "0${placeInt + 1}"
        val nextMachineSamePlace = "0$nextPlace-A"

        machineViewModel.getNextMachineByStation(nextPlace, args.selectedLineId)
        machineViewModel.getNextMachineAByStation(machineSamePlace, args.selectedLineId)

        binding.btnConfirmOpenALineSetup.text = "${languageJsonObject.getTranslation("CONFIRM AND OPEN")} $machineSamePlace"

        machineViewModel.nextMachine.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                nextMachine = it
            }

            Log.d(TAG, "onCreateView: nextMachine: ${nextMachine.machine}")
        })

        machineViewModel.nextMachineA.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                nextMachineA = it
            }

            Log.d(TAG, "onCreateView: nextMachineA: ${nextMachineA.machine}")
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machineByNo ->
            if (machineByNo != null) {

                existingMachine = false

                currentMachine.id = machineByNo.id.toLong()
                binding.machineNoTextView.text = machineByNo.machine
                binding.machineCodeTextView.text = machineByNo.machine
                binding.machineIDTextView.text = machineByNo.rfid

                Log.d(TAG, "onCreateView: machineByNo.station.isNotBlank(): ${machineByNo.station.isNotBlank()}")

                machineAlreadyInStation = machineByNo.station.isNotBlank()

                dismissPopup()
            }
        })

        binding.btnCancelLineSetup.setOnClickListener {

            if (args.origin == "linePlaces") {
                navigateToLinePlaces()
            } else {
                if (existingMachine) {
                    navigateToSetupLine()
                } else {
                    navigateToLineSetupScanMachine(args.selectedLine, args.place)
                }
            }

        }

//        if (existingMachine) {
//        } else {
//        }

        binding.btnConfirmLineSetup.setOnClickListener {

            confirmClicked = true

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
                if (!existingMachine) {

                    Log.d(TAG, "onCreateView: binding.placeNoTextView.text.toString(): ${binding.placeNoTextView.text.toString()}")

//                if (!machineAlreadyInStation) {
                    val machineCheckInRequest = MachineCheckInRequest(
                        currentMachine.id,
                        binding.placeNoTextView.text.toString(),
                        args.selectedLineId,
                        DateTime(DateTimeZone.UTC).toString())
                    machineViewModel.checkInMachine(machineCheckInRequest)
                } else {
                    Log.d(TAG, "onCreateView: nextMachine.machine: ${nextMachine.machine}")
                    if (args.origin == "linePlaces") {
                        navigateToLinePlaces()
                    } else {
                        if (nextMachine.machine.isNotBlank()) {
                            navigateToSelf(args.selectedLine, nextPlace, nextMachine.machine, nextMachine.machine, nextMachine.id.toString(), nextMachine.rfid, nextMachine.subType)
                        } else {
                            navigateToLineSetupScanMachine(args.selectedLine, nextPlace)
                        }
                    }

                }
            }

        }

        binding.btnConfirmOpenALineSetup.setOnClickListener {

            confirmClicked = false

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
                    currentMachine.id,
                    place,
                    args.selectedLineId,
                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkInMachine(machineCheckInRequest)
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
            confirmClicked = !place.contains("-A")
            if (args.machineNo.isNotBlank()) {

//                val machineCheckOutRequest = MachineCheckInRequest(
//                    currentMachine.id,
//                    "",
//                    null,
//                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkOutMachine(currentMachine.id)
            } else {

                // TODO uncomment when raised
                if (args.origin == "linePlaces") {
                    navigateToLinePlaces()
                } else {
                    if (confirmClicked) {
                        if (nextMachine.machine.isNotBlank()) {
                            existingMachine = true
                            navigateToSelf(args.selectedLine, nextPlace, nextMachine.machine, nextMachine.machine, nextMachine.id.toString(), nextMachine.rfid, nextMachine.subType)

                            dismissPopup()
                        } else {
                            navigateToLineSetupScanMachine(args.selectedLine, nextPlace)
                        }
                    } else {
                        if (nextMachineA.machine.isNotBlank()) {
                            existingMachine = true

                            navigateToSelf(args.selectedLine, machineSamePlace, nextMachineA.machine, nextMachineA.machine, nextMachineA.id.toString(), nextMachineA.rfid, nextMachineA.subType)

                            dismissPopup()
                        } else {
                            navigateToLineSetupScanMachine(args.selectedLine, machineSamePlace)
                        }
                    }
                }

            }
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

                    binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine number not found"))
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

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            if(checkInStatus != null) {
                when (checkInStatus) {
                    MachineCheckinStatus.SUCCESS -> {
//                    machinePlacesViewModel.machinePlaces.add(MachinePlace(place, args.machineNo, args.machineID, args.machineNo))

                        if (confirmClicked) {
                            // TODO uncomment when raised
                            if (args.origin == "linePlaces") {
                                navigateToLinePlaces()
                            } else {
                                if (nextMachine.machine.isNotBlank()) {
                                    existingMachine = true
                                    navigateToSelf(args.selectedLine, nextPlace, nextMachine.machine, nextMachine.machine, nextMachine.id.toString(), nextMachine.rfid, nextMachine.subType)

                                    dismissPopup()
                                } else {
                                    navigateToLineSetupScanMachine(args.selectedLine, nextPlace)
                                }
                            }
                        } else {
                            if (nextMachineA.machine.isNotBlank()) {
                                existingMachine = true

                                navigateToSelf(args.selectedLine, machineSamePlace, nextMachineA.machine, nextMachineA.machine, nextMachineA.id.toString(), nextMachineA.rfid, nextMachineA.subType)

                                dismissPopup()
                            } else {
                                navigateToLineSetupScanMachine(args.selectedLine, machineSamePlace)
                            }
                        }

                    }
                    MachineCheckinStatus.MACHINE_NOT_WORKING -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This Machine is Retired/Not Available"))
                    }
                    MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This machine number is already in place"))
                    }
                    MachineCheckinStatus.NOT_IN_FLOATING_AREA -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine is not in Floating area"))
                    }
                    MachineCheckinStatus.HAS_OPEN_TICKETS -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine has open tickets"))
                    }
                    else -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Check in: Something went wrong"))
                    }

                }

                machineViewModel.setMachineCheckInStatusComplete()
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

    private fun startCameraScan(view: View) {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("")
        integrator.initiateScan()
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
            startCameraScan(it)
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
                    .format(MachineUtil.machineOpenTicketNo, MachineUtil.machineLocation, "${args.selectedLine} - ${args.place}")
        } else {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(languageJsonObject
                    .getTranslation(
                        "This machine is currently in []. Do you want to move this machine on [] ?"
                    ))
                    .format(MachineUtil.machineLocation, "${args.selectedLine} - ${args.place}")
        }

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            dismissPopup()
            if (confirmClicked) {
                val machineCheckInRequest = MachineCheckInRequest(
                    currentMachine.id,
                    args.place,
                    args.selectedLineId,
                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkInMachine(machineCheckInRequest)
            } else {
                val machineCheckInRequest = MachineCheckInRequest(
                    currentMachine.id,
                    args.place,
                    args.selectedLineId,
                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkInMachine(machineCheckInRequest)
            }
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

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            val barCode = result?.contents
            machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToLineSetupScanMachine(selectedLine: String, selectedPlace: String) {
        val action = SetupLineMachineInPlaceDetailsFragmentDirections
            .actionSetupLineMachineInPlaceDetailsFragmentToSetupLineScanMachineFragment(selectedLine, selectedPlace, args.selectedLineId, "")
        navigate(action)
    }

    private fun navigateToSetupLine() {
        val action = SetupLineMachineInPlaceDetailsFragmentDirections
            .actionSetupLineMachineInPlaceDetailsFragmentToSetupLineFragment(args.selectedLine, args.selectedLineId)
        navigate(action)
    }

    private fun navigateToSelf(
        selectedLine: String,
        place: String,
        machineNo: String,
        machineCode: String,
        machineID: String,
        rfid: String,subType: String) {
        Log.d(TAG, "navigateToSelf: ")
        val action = SetupLineMachineInPlaceDetailsFragmentDirections.actionSetupLineMachineInPlaceDetailsFragmentSelf(selectedLine,
            place,
            machineNo,
            machineCode,
            machineID,
            rfid,
            args.selectedLineId,
            true, "", subType)
        navigate(action)
    }

    private fun navigateToLinePlaces() {
        val action = SetupLineMachineInPlaceDetailsFragmentDirections.actionSetupLineMachineInPlaceDetailsFragmentToSetupLinePlacesFragment(args.selectedLineId, args.selectedLine)
        navigate(action)
    }

}
