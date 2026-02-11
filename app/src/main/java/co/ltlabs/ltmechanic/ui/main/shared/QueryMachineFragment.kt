package co.ltlabs.ltmechanic.ui.main.shared

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.databinding.*
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.QueryMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

private const val TAG = "QueryMachineFragment";

class QueryMachineFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: QueryMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(QueryMachineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val nfcViewModel: NFCViewModel by activityViewModels()

    private val args: QueryMachineFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()

    private lateinit var binding: FragmentQueryMachineBinding

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private var action = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentQueryMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        action = "load_machine_details"
        machineViewModel.getMachineByMachineNo(args.machine)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                machineNoLabel.text = getTranslation(machineNoLabel.text.toString())
                subTypeLabel.text = getTranslation(subTypeLabel.text.toString())
                locationLabel.text = getTranslation(locationLabel.text.toString())
                lpmDateLabel.text = getTranslation(lpmDateLabel.text.toString())
                maintenanceFreqLabel.text = getTranslation(maintenanceFreqLabel.text.toString())
                rentalLabel.text = getTranslation(rentalLabel.text.toString())
                supplierDateLabel.text = getTranslation(supplierDateLabel.text.toString())
                lprDateLabel.text = getTranslation(lprDateLabel.text.toString())
                machineStatusLabel.text = getTranslation(machineStatusLabel.text.toString())
                machineConditionLabel.text = getTranslation(machineConditionLabel.text.toString())
                rfidLabel.text = getTranslation(rfidLabel.text.toString())
                btnOptions.text = getTranslation(btnOptions.text.toString())
                btnAttachNFC.text = getTranslation(btnAttachNFC.text.toString())
                brandLabel.text = getTranslation(brandLabel.text.toString())
            }
        }
        // End translation

        binding.btnAttachNFC.setOnClickListener {
            if (!binding.rfid.tag.toString().contains("NOT AVAILABLE")) {
                if (AuthUtil.role.toLowerCase(Locale.ROOT) == "administrator" || AuthUtil.role == UserType.ADMIN) {
                    reassignedConfirmedDialog(binding.root)
                } else {
                    binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Only admin can reassign the RFID"))
                }
            } else {
                startAttachNFC(binding.root)
            }
        }

        binding.btnOptions.setOnClickListener {
            showPopupWindow(binding.root, showOptionsPopupWindow())
        }

        coordinatorLayout = binding.coordinatorLayout

        lifecycleScope.launchWhenCreated {
            nfcViewModel.scanRfid.collectLatest {
                if (action == "attach_nfc") {
                    dismissPopup()
                    machineViewModel.attachMachineNFC(args.machineId, it)
                } else {
                    machineViewModel.getMachineByRfid(it)
                }
                mainViewModel.insertToNfcDeviceDatabase(false)
                mainViewModel.insertToNfcDatabase("", false)
            }
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                if (action == "attach_nfc") {
                    dismissPopup()
//                    machineViewModel.attachMachineNFC(args.machineId, "attach1234567")
                    machineViewModel.attachMachineNFC(args.machineId, nfc.rfid)
                } else {
                    machineViewModel.getMachineByRfid(nfc.rfid)
                }

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.attachNFCStatus.observe(viewLifecycleOwner, Observer {
            when (it) {

                AttachNFCStatus.SUCCESS -> {
                    action = "load_machine_details"
                    machineViewModel.getMachineByMachineNo(args.machine)
                    binding.coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "Successfully attached RFID"
                        )
                    )
                }

                AttachNFCStatus.DUPLICATE -> {
                    binding.coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "RFID is already existing"
                        )
                    )
                }

                AttachNFCStatus.ALREADY_ATTACHED -> {
                    binding.coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "MachFine has existing RFID assignment"
                        )
                    )
                }

            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                when (action) {
                    "replace" -> {
                        if (machine.station.isNotBlank()) {
                            navigateToReplace(
                                mfgLineId.toLong(),
                                machine.mfgLine ?: "",
                                machine.station,
                                machine.machine,
                                machine.id
                            )
                        } else {
                            coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not in Production Line"))
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
                    "create_ticket" -> {
                        loadedMachine = machine.machine
                        loadedMachineId = machine.id
                        loadedMachineMfgLine = machine.mfgLine ?: ""
                        loadedMachineStation = machine.station

                        if (MachineUtil.machineFound) {
                            if (selectedLinesStr.any { it == machine.mfgLine }) {

                                if (machine.hasOpenTicket) {
                                    dismissPopup()
                                    binding.coordinatorLayout.showSnackbar(
                                        languageJsonObject.getTranslation(
                                            "This machine has an active ticket"
                                        )
                                    )
                                } else {
                                    ticketViewModel.getMachineProblems(machine.id)
                                }

                            } else {

                                val dm = DisplayMetrics()
                                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                                val width = (dm.widthPixels * .9).toInt()
                                val height = (dm.heightPixels * .5).toInt()

                                dismissPopup()
                                popupWindow = showErrorPopupWindow()
                                popupWindow?.isOutsideTouchable = true
                                popupWindow?.isFocusable = true
                                popupWindow?.update(0, 0, width, height)
                                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))


                            }
                        } else {
                            dismissPopup()
                        }
                    }
                    "send_request" -> {
                        dismissPopup()

                        navigateToSendRequest(
                            machine.id,
                            machine.machine,
                            machine.rfid ?: "",
                            machine.subtype ?: "",
                            (machine.mfgLineId ?: 0).toLong()
                        )

//                        if (selectedLinesStr.any { it == machine.mfgLine }) {
//
//                        } else {
//                            val dm = DisplayMetrics()
//                            activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//
//                            val width = (dm.widthPixels * .9).toInt()
//                            val height = (dm.heightPixels * .5).toInt()
//
//                            dismissPopup()
//                            popupWindow = showErrorPopupWindow()
//                            popupWindow?.isOutsideTouchable = true
//                            popupWindow?.isFocusable = true
//                            popupWindow?.update(0, 0, width, height)
//                            popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)
//
//                            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//                        }
                    }
                    "query_machine" -> {
                        dismissPopup()
                        navigateToQueryMachine(machine.id, machine.machine)
                    }

                    "load_machine_details" -> {

                        binding.apply {
                            machineNo.text = machine.machine
                            subType.text = if (machine.subtype?.isNotBlank() == true) {
                                machine.subtype
                            } else {
                                "-"
                            }
                            brand.text = languageJsonObject.getTranslation(
                                if (machine.brand.isNotBlank()) {
                                    machine.brand
                                } else {
                                    "-"
                                }
                            )
                            location.text = languageJsonObject.getTranslation(
                                if (machine.station.isNotBlank()) {
                                    "${machine.mfgLine} - ${machine.station}"
                                } else {
                                    machine.area
                                }
//                                if (machine.area.toLowerCase().contains("prod")) {
//                                    "${machine.mfgLine} - ${machine.station}"
//                                } else {
//                                    machine.area
//                                }
                            )
                            lpmDate.text = DateUtil.formatToDate(machine.lastPMDt)
                            maintenanceFreq.text = languageJsonObject.getTranslation(
                                machine.maintenanceFreq.ifBlank { "-" }
                            )
                            rental.text = languageJsonObject.getTranslation(
                                machine.rental
                            )
                            supplierDate.text = DateUtil.formatToDate(machine.supplierDt)
                            lprDate.text = DateUtil.formatToDate(machine.lastRepairedDt)

                            machineStatus.text = languageJsonObject.getTranslation(
                                machine.status
                            )
                            when (machine.status) {
                                "NOT AVAIL/RETIRED", "INACTIVE" -> {
                                    machineStatus.setTextColor(Color.parseColor("#FB460E"))
                                }

                                "ACTIVE", "READY", "AVAILABLE", "IN USE" -> {
                                    machineStatus.setTextColor(Color.parseColor("#95F204"))
                                }

                                "REPAIR" -> {
                                    machineStatus.setTextColor(Color.parseColor("#F59A23"))
                                }

                                "MAINTENANCE" -> {
                                    machineStatus.setTextColor(Color.parseColor("#0F75BC"))
                                }

                            }

                            machineCondition.text = languageJsonObject.getTranslation(
                                machine.condition
                            )
                            if (machine.rfid?.isNotBlank() == true) {
                                rfid.text = machine.rfid
                                rfid.tag = machine.rfid
//                                btnAttachNFC.isEnabled = false
                            } else {
//                                rfidLabel.text = "NFC"
                                rfid.tag = "NOT AVAILABLE"
                                rfid.text = languageJsonObject.getTranslation(
                                    "NOT AVAILABLE"
                                )
//                                btnAttachNFC.isEnabled = true
                            }
                        }

                    }
                }

                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner, Observer { mfgLines ->

            mfgLines.filter { it.checked ?: false }.forEach {
                selectedLinesStr.add(it.mfgLine)
                selectedLinesIdStr.add(it.mfgLineId.toString())
            }

            ticketViewModel.getReportedTickets(selectedLinesIdStr.joinToString(","))

        })

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            //mainViewModel.insertToNfcDeviceDatabase(false)
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                when (action) {
                    "replace" -> {
                        if (machine.station.isNotBlank()) {
                            navigateToReplace(
                                mfgLineId.toLong(),
                                machine.mfgLine ?: "",
                                machine.station,
                                machine.machine,
                                machine.id
                            )
                        } else {
                            coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine is not in Production Line"))
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
                    "create_ticket" -> {
                        loadedMachine = machine.machine
                        loadedMachineId = machine.id
                        loadedMachineMfgLine = machine.mfgLine ?: ""
                        loadedMachineStation = machine.station

                        if (MachineUtil.machineFound) {
                            if (selectedLinesStr.any { it == machine.mfgLine }) {

                                if (machine.hasOpenTicket) {
                                    dismissPopup()
                                    binding.coordinatorLayout.showSnackbar(
                                        languageJsonObject.getTranslation(
                                            "This machine has an active ticket"
                                        )
                                    )
                                } else {
                                    ticketViewModel.getMachineProblems(machine.id)
                                }

                            } else {

                                val dm = DisplayMetrics()
                                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                                val width = (dm.widthPixels * .9).toInt()
                                val height = (dm.heightPixels * .5).toInt()

                                dismissPopup()
                                popupWindow = showErrorPopupWindow()
                                popupWindow?.isOutsideTouchable = true
                                popupWindow?.isFocusable = true
                                popupWindow?.update(0, 0, width, height)
                                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))


                            }
                        } else {
                            dismissPopup()
                        }
                    }
                    "send_request" -> {
                        dismissPopup()

                        navigateToSendRequest(
                            machine.id,
                            machine.machine,
                            machine.rfid ?: "",
                            machine.subtype ?: "",
                            (machine.mfgLineId ?: 0).toLong()
                        )

//                        if (selectedLinesStr.any { it == machine.mfgLine }) {
//
//                        } else {
//                            val dm = DisplayMetrics()
//                            activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//
//                            val width = (dm.widthPixels * .9).toInt()
//                            val height = (dm.heightPixels * .5).toInt()
//
//                            dismissPopup()
//                            popupWindow = showErrorPopupWindow()
//                            popupWindow?.isOutsideTouchable = true
//                            popupWindow?.isFocusable = true
//                            popupWindow?.update(0, 0, width, height)
//                            popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)
//
//                            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//                        }
                    }
                    "query_machine" -> {
                        dismissPopup()
                        navigateToQueryMachine(machine.id, machine.machine)
                    }
                }

                machineViewModel.machineDetailsByRfidComplete()
            }
        })

        ticketViewModel.commonProblems.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                dismissPopup()
                if (it.isNotEmpty()) {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        it.size.toLong(),
                        "home"
                    )
                } else {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        0,
                        "home"
                    )
                }

                ticketViewModel.commonProblemsComplete()
            }
        })

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
            when (machineStatus) {
                MachineStatus.FOUND -> {

                }
                MachineStatus.NOT_FOUND -> {
                    dismissPopup()

                    if (MachineUtil.message.isNotBlank()) {
                        binding.coordinatorLayout.showSnackbar(
                            languageJsonObject.getTranslation(
                                MachineUtil.message.replace(".", "")
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
        })

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

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.rfid?.isNotBlank() == true) {
            binding.rfid.text = args.rfid
            binding.rfid.tag = args.rfid
        } else {
            binding.rfid.tag = "NOT AVAILABLE"
            binding.rfid.text = languageJsonObject.getTranslation(
                "NOT AVAILABLE"
            )
        }
    }

    private fun reassignedConfirmedDialog(root: View) {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_assign_rfid, null)
        val builder = AlertDialog.Builder(requireContext()).setView(layout)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val dialogTitle = dialog.findViewById<TextView>(R.id.dialog_title)
        val dialogContent = dialog.findViewById<TextView>(R.id.dialog_content)
        val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
        val okButton = dialog.findViewById<Button>(R.id.btnOk)

        with(languageJsonObject) {
            dialogTitle?.text = getTranslation(dialogTitle?.text.toString())
            dialogContent?.text = getTranslation(dialogContent?.text.toString())
            cancelButton?.text = getTranslation(cancelButton?.text.toString())
            okButton?.text = getTranslation(okButton?.text.toString())
        }

        layout.apply {
            cancelButton?.setOnClickListener { dialog.dismiss() }
            okButton?.setOnClickListener {
                dialog.dismiss()
                startAttachNFC(root)
            }
        }

    }

    private fun startAttachNFC(root: View) {
        action = "attach_nfc"

        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .2).toInt()

        dismissPopup()
        popupWindow = showAttachNFCPopupWindow()
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(root, Gravity.CENTER, 0, -25)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun showScanPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            startCameraScan()
        }

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            dismissPopup()
        }

        binding.btnSubmitMachine.setOnClickListener {
//            progressBar.showProgressBar(true)
            machineViewModel.getMachineByMachineNo(binding.machineEditText.text.toString())

        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showAttachNFCPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)
        nfcViewModel.isObserveOutsideMainActivity = true

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupAttachRfidBinding.inflate(inflater)
        val closeButton = binding.closePopup

        with(languageJsonObject) {
            with(binding) {
                labelScanningNFC.text =
                    getTranslation(labelScanningNFC.text.toString().replace("...", ""))
                labelCancel2.text = getTranslation(labelCancel2.text.toString())

            }
        }

        binding.labelCancel2.text =
            languageJsonObject.getTranslation(binding.labelCancel2.text.toString())
        binding.labelScanningNFC.text = languageJsonObject.getTranslation(
            binding.labelScanningNFC.text.toString()
        )

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            dismissPopup()
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showErrorPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupLineLeaderErrorPopupNotOnLineBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                labelMachineNotAssigned.text =
                    getTranslation(labelMachineNotAssigned.text.toString())
                labelWhatDoYouWant.text = getTranslation(labelWhatDoYouWant.text.toString())
                btnScanAgain.text = getTranslation(btnScanAgain.text.toString())
                btnCancelLineSetup3.text = getTranslation(btnCancelLineSetup3.text.toString())
            }
        }
        // End translation

        binding.btnScanAgain.setOnClickListener {
            dismissPopup()


            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .7).toInt()

            dismissPopup()
            popupWindow = showScanPopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            DimUtil.dimBehind(popupWindow)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        binding.btnCancelLineSetup3.setOnClickListener {
            dismissPopup()
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //if (MainUtil.googlePlayAvailable) {
        //  if (requestCode == 0) {
        //  if (resultCode == CommonStatusCodes.SUCCESS) {
        //      dismissPopup()
        //    if (data != null) {

        //       var barcode: Barcode? = data.getParcelableExtra("barcode")
        //        machineViewModel.getMachineByMachineNo(barcode?.displayValue.toString())

        //    } else {
        //       coordinatorLayout.showSnackbar(
        //           languageJsonObject.getTranslation(
        //               "No QR code found"
        //          )
        //     )
        //  }
        //  }
        // } else {

        //   }
//} else {
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        val barCode = result?.contents
        machineViewModel.getMachineByMachineNo(barCode.toString())

//}

        super.onActivityResult(requestCode, resultCode, data)


    }

    private fun showOptionsPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupMaintenanceOptionMenuBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                btnMaintenanceHistory.text = getTranslation(btnMaintenanceHistory.text.toString())
                btnRepairHistory.text = getTranslation(btnRepairHistory.text.toString())
            }
        }
        // End translation

        binding.btnMaintenanceHistory.setOnClickListener {
            dismissPopup()
            navigateToMaintenanceHistory(
                0,
                "",
                args.machineId
            )
        }

        binding.btnRepairHistory.setOnClickListener {
            dismissPopup()
            navigateToRepairHistory(
                args.machineId
            )
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showPopupWindow(view: View, popupWindowType: PopupWindow) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .25).toInt()

        dismissPopup()
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = false

//        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                event?.let {
//                    if (it.x < 0 || it.x > width) return true
//                    if (it.y < 0 || it.y > height) return true
//                }
//
//                return false
//            }
//
//        })

        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, height * 2)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun navigateToMechanicHome() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToMechanicHomeFragment(
                "",
                false,
                "",
                ""
            )
        navigate(action)
    }

    private fun navigateToLineLeaderHome() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToLineLeaderHomeFragment()
        navigate(action)
    }

    private fun navigateToMechanicInRepairTickets() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToMechanicInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToMechanicReportedTickets() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToMechanicReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToMechanicRepairedTickets() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToMechanicRepairedTicketsFragment()
        navigate(action)
    }

    private fun navigateToLineLeaderInRepairTickets() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToLineLeaderInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToLineLeaderReportedTickets() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToLineLeaderReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToLineLeaderRepairedTickets() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToLineLeaderRepairedTicketsFragment()
        navigate(action)
    }

    private fun navigateToLineOverview() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToLineStatusFragment()
        navigate(action)
    }

    private fun navigateToSetupLine() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToSetupLineFragment(
                LineUtil.selectedMfgLine,
                LineUtil.selectedMfgLineId
            )
        navigate(action)
    }

    private fun navigateToReplace(
        mfgLineId: Long,
        mfgLine: String,
        station: String,
        machine: String,
        machineId: Long
    ) {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToReplaceMachineScanDetailsFragment(
                mfgLineId,
                mfgLine,
                station,
                machine,
                machineId
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
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToMoveMachineFragment(
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

    private fun navigateToCreateTicket(
        machineId: Long,
        machine: String,
        station: String,
        mfgLine: String,
        commonProblems: Long,
        origin: String
    ) {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToCreateTicketFragment(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
            )
        navigate(action)
    }

    private fun navigateToNotifications() {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToNotificationFragment()
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        mfgLineId: Long
    ) {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentToSendRequestFragment(
                machineId,
                machine,
                rfid,
                subType,
                mfgLineId
            )

        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = QueryMachineFragmentDirections
            .actionQueryMachineFragmentSelf(machineId, machine)
        navigate(action)
    }

    private fun navigateToRepairHistory(
        machineId: Long
    ) {
        TicketUtil.isQueryMachine = true
        val bundle = bundleOf("machineId" to machineId)
        findNavController().navigate(
            R.id.action_queryMachineFragment_to_repairHistoryFragment,
            bundle
        )
    }

    private fun navigateToMaintenanceHistory(
        ticketId: Long,
        ticketNo: String,
        machineId: Long
    ) {
        TicketUtil.maintenanceTicketStatus = "all"
        TicketUtil.isQueryMachine = true
        val bundle = bundleOf(
            "ticketId" to ticketId,
            "ticketNo" to ticketNo,
            "machineId" to machineId
        )
        findNavController().navigate(
            R.id.action_queryMachineFragment_to_maintenanceHistoryFragment,
            bundle
        )
    }

}
