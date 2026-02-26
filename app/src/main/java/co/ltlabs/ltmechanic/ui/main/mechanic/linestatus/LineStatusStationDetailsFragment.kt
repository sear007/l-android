package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusStationDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupRemoveConfirmationMessageBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.ui.dialog.movemachine.MoveMCBSDialog
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusStationDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "StationDetailsFragment";

class LineStatusStationDetailsFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusStationDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusStationDetailsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val nfcViewModel: NFCViewModel by activityViewModels()

    private val args: LineStatusStationDetailsFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    private var remove = true

    private lateinit var binding: FragmentLineStatusStationDetailsBinding

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onResume() {
        super.onResume()

        if (MachineUtil.mchineNotFound) {
            if (MachineUtil.message.isNotBlank()) {
                coordinatorLayout.showSnackbar(
                    languageJsonObject.getTranslation(
                        "You do not have access to the machine's current location"
                    )
                )
            } else {
                coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine number not found"))
            }
            MachineUtil.mchineNotFound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLineStatusStationDetailsBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        machineViewModel.getNextMachineAByStation("${args.station}-A", args.mfgLineId)

        Log.d(TAG, "onCreateView: args.endStation: ${args.endStation}")

        coordinatorLayout = binding.coordinatorLayout

        if (args.rfid.isBlank()) {
            binding.placeTextView4.visibility = View.GONE
            binding.machineIDTextView.visibility = View.GONE

        }

        if (args.machine.isBlank()) {
            binding.btnRemove.text = getString(R.string.setup_line_button_cancel)
            remove = false
        }

        binding.btnConfirmOpenALineSetup3.visibility = if (args.showNextButton) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Start translation
        with(languageJsonObject) {
            kotlin.with(binding) {
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView6.text = getTranslation(placeTextView6.text.toString())
                btnInsertBefore.text = getTranslation(btnInsertBefore.text.toString())
                btnInsertAfter.text = getTranslation(btnInsertAfter.text.toString())
                btnRemove.text = getTranslation(btnRemove.text.toString())
                btnReplace.text =
                    getTranslation(btnReplace.text.toString())
            }
        }
        // End translation

//        machineViewModel.nextMachineA.observe(viewLifecycleOwner, Observer {
//            Log.d(TAG, "onCreateView: nextMachineA: $nextMachineA")
//            if (it != null) {
//                if (it.machine.isNotBlank()) {
//                    binding.btnConfirmOpenALineSetup3.visibility = View.INVISIBLE
//                    nextMachineA = it
//
//
//                } else {
//                    if (!args.station.contains("-A")) {
//                        binding.btnConfirmOpenALineSetup3.visibility = View.VISIBLE
//                    }
//                }
//
//                if (!args.station.contains("-A")) {
//                    if (!binding.btnConfirmOpenALineSetup3.isVisible) {
////                            binding.btnConfirmOpenALineSetup3.visibility = View.VISIBLE
//                    } else {
//                        binding.btnConfirmOpenALineSetup3.visibility = View.VISIBLE
//                    }
//
//                }
//            }
//        })


        if (args.machine.isBlank()) {
            binding.btnReplace.text =
                languageJsonObject.getTranslation(getString(R.string.add_machine))
            binding.btnReplace.tag = getString(R.string.add_machine)
        }

//        binding.toolBarTitleTextView.text = getString(R.string.line_status_place, args.station)
        binding.toolBarTitleTextView.text =
            "${languageJsonObject.getTranslation("STATION")} ${args.station}"

        binding.machineCodeTextView.text = args.machine
        binding.machineIDTextView.text = args.rfid
        binding.machineSubTypeTextView.text = args.subType
        binding.tvLineCode.text = args.mfgLine

        binding.btnConfirmOpenALineSetup3.text =
            "${languageJsonObject.getTranslation("OPEN")} ${args.station}-A"

        progressBar = binding.progressBar

        binding.btnConfirmOpenALineSetup3.setOnClickListener {
            navigateToNextMachineA()
        }

        binding.btnReplace.setOnClickListener {
            if (args.machine.isBlank()) {
                nfcViewModel.needMfgLine = args.mfgLine
                nfcViewModel.needMfgLineId = args.mfgLineId
                nfcViewModel.needStation = args.station
                nfcViewModel.setNFCAction(NFCAction.ADD_MACHINE)
            } else {
                navigateToReplaceMachineScanDetails(
                    args.mfgLineId,
                    args.mfgLine,
                    args.station,
                    args.machine,
                    args.machineId
                )
            }
        }

        binding.btnRemove.setOnClickListener {
//            if (args.station.isNotBlank()) {
//
//                machineViewModel.checkOutMachine(args.machineId)
//
//            } else {
//                Toast.makeText(activity, "Machine is not assigned to a place.", Toast.LENGTH_SHORT).show()
//            }

            if (remove) {
                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .28).toInt()

                Log.d(TAG, "onCreateView: height: $height")
                Log.d(TAG, "onCreateView: width: $width")

                dismissPopup()
                popupWindow = showPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            } else {
                activity?.onBackPressed()
            }
        }

        binding.btnInsertBefore.setOnClickListener {
            val station = args.station.toInt()
            val newStation = if (station == 1) "01" else "${station - 1}"
            navigateToInsertScanMachine(
                args.mfgLineId,
                args.mfgLine,
                newStation,
                "INSERT BEFORE",
                "insertBefore",
                false
            )
        }

        binding.btnInsertAfter.setOnClickListener {
            val station = args.station.toInt()
            val newStation = if ((station + 1) > 9) "${station + 1}" else "0${station + 1}"
            navigateToInsertScanMachine(
                args.mfgLineId,
                args.mfgLine,
                newStation,
                "INSERT AFTER",
                "insertAfter",
                args.endStation
            )
        }

        machineViewModel.machineCheckOutStatus.observe(
            viewLifecycleOwner,
            Observer { checkInStatus ->
                if (checkInStatus != null) {
                    when (checkInStatus) {
                        MachineCheckoutStatus.SUCCESS -> {
                            dismissPopup()
                            navigateToStations(true)
                        }
                    }

                    machineViewModel.setMachineCheckInStatusComplete()
                }
            })

        machineViewModel.machineCheckInStatus.observe(
            viewLifecycleOwner,
            Observer { checkInStatus ->
                if (checkInStatus != null) {
                    when (checkInStatus) {
                        MachineCheckinStatus.SUCCESS -> {

                            dismissPopup()
                            navigateToStations(false)

                        }
                    }

                    machineViewModel.setMachineCheckInStatusComplete()
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

        if (args.station.contains("-A")) {
            binding.btnInsertBefore.visibility = View.INVISIBLE
            binding.btnInsertAfter.visibility = View.INVISIBLE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLine(args.mfgLineId)
        lifecycleScope.launchWhenCreated {
            viewModel.line.collectLatest {
                if (it.isNotEmpty()) {
                    val item = it[0]
                    binding.tvLineName.text = item.mfgLineName.ifEmpty { "-" }
                    binding.tvLineCode.text = item.mfgLine.ifEmpty { "-" }
                }
            }
        }
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private var moveMCDialog: MoveMCBSDialog? = null
    private var moveToBuilding: String? = null
    private var moveToArea: String? = null
    private fun showMoveMachineDialog() {
        if (moveMCDialog == null) moveMCDialog =
            MoveMCBSDialog.newInstance(args.building, args.buildingId)
        if (moveMCDialog?.isAdded == false) {
            moveMCDialog?.show(childFragmentManager, moveMCDialog?.tag)
            moveMCDialog?.isCancelable = false
            moveMCDialog?.onDismissListener {
                moveMCDialog = null
            }

            moveMCDialog?.setOnOkClicked { _, areaId, buildingName, areaName ->
                moveToBuilding = buildingName
                moveToArea = areaName
                machineViewModel.checkOutMachine(args.machineId, areaId = areaId, remove = true)
            }
        }
    }

    private fun showPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupRemoveConfirmationMessageBinding.inflate(inflater)

//        binding.textView3.text = getString(R.string.remove_machine_confirmation_message, args.machine)
        binding.textView3.text =
            "${languageJsonObject.getTranslation("Are you sure you want to remove machine")} ${args.machine}"
        binding.btnConfirmLineSetup2.text =
            languageJsonObject.getTranslation(binding.btnConfirmLineSetup2.text.toString())
        binding.btnCancelLineSetup2.text =
            languageJsonObject.getTranslation(binding.btnCancelLineSetup2.text.toString())

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            dismissPopup()
            showMoveMachineDialog()
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToReplaceMachine(mfgLineId: Long, mfgLine: String, machine: String) {
        val action =
            LineStatusStationDetailsFragmentDirections.actionLineStatusStationDetailsFragmentToLineStatusReplaceMachineFragment(
                mfgLineId,
                mfgLine,
                machine
            )
        navigate(action)
    }

    private fun navigateToStations(isCheckout: Boolean) {
        val station = if (isCheckout) "$moveToBuilding - $moveToArea" else args.station
        val bundle = bundleOf(
            "mfgLineId" to args.mfgLineId,
            "mfgLine" to args.mfgLine,
            "addMachineSuccess" to false,
            "addMachineMachine" to args.machine,
            "addMachineStation" to station,
            "isRemoveMachineFromLineStationDetails" to isCheckout
        )
        findNavController().navigate(R.id.action_lineStatusStationDetailsFragment_to_lineStatusStationsFragment, bundle)

    }

    private fun navigateToReplaceMachineScanDetails(
        mfgLineId: Long,
        mfgLine: String,
        station: String,
        machine: String,
        machineId: Long
    ) {
        val action =
            LineStatusStationDetailsFragmentDirections.actionLineStatusStationDetailsFragmentToLineStatusReplaceMachineScanDetailsFragment(
                mfgLineId,
                mfgLine,
                station,
                machine,
                machineId
            )
        navigate(action)
    }

    private fun navigateToInsertScanMachine(
        mfgLineId: Long,
        mfgLine: String,
        station: String,
        actionTitle: String,
        actionName: String,
        endStation: Boolean
    ) {
        val action =
            LineStatusStationDetailsFragmentDirections.actionLineStatusStationDetailsFragmentToLineStatusInsertScanMachineFragment(
                mfgLineId,
                mfgLine,
                station,
                actionTitle,
                actionName,
                endStation
            )
        navigate(action)
    }

    private fun navigateToAddMachine(mfgLineId: Long, mfgLine: String, station: String) {
        val action =
            LineStatusStationDetailsFragmentDirections.actionLineStatusStationDetailsFragmentToLineStatusAddMachineScanMachineDetailsFragment(
                mfgLineId,
                mfgLine,
                station
            )
        navigate(action)
    }

    private fun navigateToNextMachineA() {
        val action =
            LineStatusStationDetailsFragmentDirections.actionLineStatusStationDetailsFragmentToLineStatusNextMachineAScanMachineFragment(
                args.mfgLineId,
                args.mfgLine,
                "${args.station}-A"
            )
        navigate(action)
    }
}
