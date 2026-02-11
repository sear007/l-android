package co.ltlabs.ltmechanic.ui.main.mechanic.replacemachine

import android.content.Context
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.databinding.FragmentReplaceMachineScanDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupKeepEmptyConfirmationMessageBinding
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.ReplaceMachineScanDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "ReplaceMachine";

class ReplaceMachineScanDetailsFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: ReplaceMachineScanDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReplaceMachineScanDetailsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val nfcViewModel: NFCViewModel by activityViewModels()

    private var popupWindow: PopupWindow? = null

    lateinit var progressBar: ProgressBar

    private val args: ReplaceMachineScanDetailsFragmentArgs by navArgs()

    var keepEmptyClicked = false


    private var nfcAdapter: NfcAdapter? = null

    private var action = ""
    private var action2 = ""

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        val binding = FragmentReplaceMachineScanDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        binding.machineNoTextView.text = args.machine
        binding.mfgLineTextView.text = args.mfgLine
        binding.stationTextView.text = args.station

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView7.text = getTranslation(placeTextView7.text.toString())
                btnScan.text = getTranslation(btnScan.text.toString())
                btnKeepEmpty.text = getTranslation(btnKeepEmpty.text.toString())
                btnCancel2.text = getTranslation(btnCancel2.text.toString())
            }
        }

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                when (action) {
                    "replace" -> {
                        MachineUtil.machineNo = machine.machine
                        MachineUtil.machineArea = machine.area
                        MachineUtil.machineLocation =
                            if (machine.area.toLowerCase().contains("prod")) {
                                "${machine.mfgLine} - ${machine.station}"
                            } else {
                                machine.area
                            }
                        MachineUtil.machineHasOpenTickets = machine.hasOpenTicket
//                        navigateToReplace(mfgLineId, machine.mfgLine ?: "", machine.station, machine.machine, machine.id)
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


                    }
                    "move_machine" -> {

                        navigateToMoveMachine(
                            machine.id,
                            machine.machine,
                            machine.rfid ?: "",
                            machine.subtype ?: "",
                            machine.area,
                            machine.station,
                            machine.mfgLine ?: ""
                        )
                    }
                    "query_machine" -> {
                        navigateToQueryMachine(machine.id, machine.machine)
                    }
                }

                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                when (action) {
                    "replace" -> {
                        MachineUtil.machineNo = machine.machine
                        MachineUtil.machineArea = machine.area
                        MachineUtil.machineLocation =
                            if (machine.area.toLowerCase().contains("prod")) {
                                "${machine.mfgLine} - ${machine.station}"
                            } else {
                                machine.area
                            }
                        MachineUtil.machineHasOpenTickets = machine.hasOpenTicket
//                        navigateToReplace(mfgLineId, machine.mfgLine ?: "", machine.station, machine.machine, machine.id)
                        if (action2 == "scan_new") {
                            dismissPopup()
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
                                dismissPopup()
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


                    }
                    "move_machine" -> {
                        dismissPopup()
                        navigateToMoveMachine(
                            machine.id,
                            machine.machine,
                            machine.rfid ?: "",
                            machine.subtype ?: "",
                            machine.area,
                            machine.station,
                            machine.mfgLine ?: ""
                        )
                    }
                    "query_machine" -> {
                        dismissPopup()
                        navigateToQueryMachine(machine.id, machine.machine)
                    }
                }

                machineViewModel.machineDetailsByRfidComplete()
            }
        })

//        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
//            when (machineStatus) {
//                MachineStatus.FOUND -> {
//
//                }
//                MachineStatus.NOT_FOUND -> {
//                    binding.coordinatorLayout.showSnackbar(
//                        languageJsonObject.getTranslation(
//                            "Machine number not found"
//                        )
//                    )
//                }
//            }
//        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    dismissPopup()
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        coordinatorLayout = binding.coordinatorLayout

        binding.btnCancel2.setOnClickListener {
            activity?.onBackPressed()
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

                Log.d(TAG, "onCreateView: dm.heightPixels: ${dm.heightPixels}")

                val width = (dm.widthPixels * .9).toInt()
//                val height = (dm.heightPixels * .25).toInt()
                val height = (dm.heightPixels * (if (dm.heightPixels <= 1184) .30 else .25)).toInt()

                dismissPopup()
                popupWindow = showConfirmationPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

            } else {
                coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not assigned to a place."))
            }
        }

        binding.btnScan.setOnClickListener {
            keepEmptyClicked = false
            action = "replace"
            action2 = "scan_new"
            nfcViewModel.apply {
                mfgLineId = args.mfgLineId
                mfgLine = args.mfgLine
                machineId = args.machineId
                machine = args.machine
                station = args.station
                setNFCAction(NFCAction.REPLACE_MACHINE_CONFIRM)
            }
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

//        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machine ->
//            mainViewModel.insertToNfcDeviceDatabase(false)
//            if (machine != null) {
//                val mfgLineId = machine.mfgLineId ?: 0
//
//                navigateToScanDetailsConfirm(
//                    args.mfgLineId,
//                    args.mfgLine,
//                    args.machineId,
//                    args.machine,
//                    args.station,
//                    machine.id,
//                    machine.machine,
//                    machine.station
//                )
//
//                machineViewModel.machineDetailsByRfidComplete()
//
//            }
//        })
//
//        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
//            if (machine != null) {
//                val mfgLineId = machine.mfgLineId ?: 0
//
//                navigateToScanDetailsConfirm(
//                    args.mfgLineId,
//                    args.mfgLine,
//                    args.machineId,
//                    args.machine,
//                    args.station,
//                    machine.id,
//                    machine.machine,
//                    machine.station
//                )
//
//                machineViewModel.machineDetailsByMachineNoComplete()
//
//            }
//        })

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
            if (machineStatus != null) {
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

                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Machine number not found"
                                )
                            )
                        }
                    }
                }

                machineViewModel.machineStatusComplete()
            }
        })

//        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
//            if(checkInStatus != null) {
//                when (checkInStatus) {
//                    MachineCheckinStatus.SUCCESS -> {
//
//                        if (keepEmptyClicked) {
////                            Toast.makeText(activity, "Station/Place has been emptied", Toast.LENGTH_SHORT).show()
//
//                            dismissPopup()
//                            navigateToMechanicHome(
//                                "keepEmpty",
//                                false,
//                                args.mfgLine,
//                                args.station
//                            )
//
//                        }
//
//                        navigateToReplaceMachine()
//
//                    }
//                }
//
//                machineViewModel.setMachineCheckInStatusComplete()
//            }
//        })

        machineViewModel.machineCheckOutStatus.observe(
            viewLifecycleOwner,
            Observer { checkInStatus ->
                if (checkInStatus != null) {
                    when (checkInStatus) {
                        MachineCheckoutStatus.SUCCESS -> {

                            if (keepEmptyClicked) {
//                            Toast.makeText(activity, "Station/Place has been emptied", Toast.LENGTH_SHORT).show()

                                dismissPopup()
                                navigateToMechanicHome(
                                    "keepEmpty",
                                    false,
                                    args.machine,
                                    args.station
                                )

                            }

//                        navigateToReplaceMachine()

                        }
                    }

                    machineViewModel.setMachineCheckOutStatusComplete()
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

    private fun dismissPopup() {
        findMachineBsDialog?.dismiss()
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun showConfirmationPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupKeepEmptyConfirmationMessageBinding.inflate(inflater)

//        binding.textView3.text = getString(R.string.keep_empty_confirmation_message, args.mfgLine, args.station)
        binding.textView3.text =
            "${languageJsonObject.getTranslation("Are you sure you want to empty Line")} ${args.mfgLine} ${args.station}?"

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
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

    private fun navigateToScanDetailsConfirm(
        mfgLineId: Long,
        mfgLine: String,
        machineId: Long,
        machine: String,
        station: String,
        machineIdToCheckIn: Long,
        machineToCheckIn: String,
        scannedMachineStation: String
    ) {
        /*val action =
            ReplaceMachineScanDetailsFragmentDirections.actionReplaceMachineScanDetailsFragmentToReplaceMachineScanDetailsConfirmFragment(
                mfgLineId,
                mfgLine,
                machineId,
                machine,
                station,
                machineIdToCheckIn,
                machineToCheckIn,
                scannedMachineStation
            )
        navigate(action)*/
    }

    private fun navigateToMechanicHome(
        replaceAction: String,
        replaceSuccess: Boolean,
        replaceMfgLine: String,
        replaceStation: String
    ) {
        val action = ReplaceMachineScanDetailsFragmentDirections
            .actionReplaceMachineScanDetailsFragmentToMechanicHomeFragment(
                replaceAction,
                replaceSuccess,
                replaceMfgLine,
                replaceStation
            )
        navigate(action)
    }

    private fun navigateToMoveMachine(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        location: String,
        station: String,
        mfgLine: String
    ) {
        val action = ReplaceMachineScanDetailsFragmentDirections
            .actionReplaceMachineScanDetailsFragmentToMoveMachineFragment(
                machineId,
                machine,
                rfid,
                subType,
                location,
                station,
                mfgLine
            )
        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = ReplaceMachineScanDetailsFragmentDirections
            .actionReplaceMachineScanDetailsFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }
}
