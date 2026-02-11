package co.ltlabs.ltmechanic.ui.main.mechanic.repairedtickets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.databinding.FragmentMechanicRepairedTicketsBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicRepairedTicketsListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicRepairedTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "MRepairedTickets";

class MechanicRepairedTicketsFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MechanicRepairedTicketsViewModel by viewModels { providerFactory }
    private val machineViewModel: MachineViewModel by viewModels { providerFactory }
    private val ticketViewModel: TicketViewModel by viewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }

    private var popupWindow: PopupWindow? = null

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var binding: FragmentMechanicRepairedTicketsBinding

    private var action = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMechanicRepairedTicketsBinding.inflate(inflater)
        binding.jTranslate = languageJsonObject

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner) { machine ->
            if (machine != null) {
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
                machineViewModel.machineDetailsByMachineNoComplete()
            }
        }

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

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        coordinatorLayout = binding.coordinatorLayout

        val adapter = MechanicRepairedTicketsListAdapter(viewModel, languageJsonObject)

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        ticketViewModel.repairedTickets.observe(viewLifecycleOwner, Observer { repairedTickets ->
            if (repairedTickets != null) {
                adapter.data = repairedTickets
            }
        })

        binding.btnCreateTicket.setOnClickListener {
            action = "create_ticket"
            nfcViewModel.setNFCAction(NFCAction.CREATE_TICKET)
        }

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner, Observer { mfgLines ->

            mfgLines.filter { it.checked ?: false }.forEach {
                selectedLinesStr.add(it.mfgLine)
                selectedLinesIdStr.add(it.mfgLineId.toString())
            }

            val areas = dashboardViewModel.sharedAreasNoLines.map {
                it.id ?: ""
            }
            ticketViewModel.getRepairedTickets(selectedLinesIdStr.joinToString(","), areas.joinToString(","))

        })

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    ticketViewModel.getRepairedTickets(selectedLinesIdStr.joinToString(","))

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
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
                        "repaired"
                    )
                } else {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        0,
                        "repaired"
                    )
                }

                ticketViewModel.commonProblemsComplete()
            }
        })

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        viewModel.navigateToTicketPreview.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                navigateToChecklist(it.id, it.ticketNo)

                viewModel.navigateToTicketPreviewComplete()
            }
        })


        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        ticketViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {

            if (it != null && it.isNotEmpty()) {

                when (it[0].action) {

                    SNACK_BAR_ACTION_CREATE_TICKET -> {
                        if (it[0].show) {
                            val message = "Successfully created ticket"
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    message
                                )
                            )
                        }
                    }
                }

                ticketViewModel.finishInsertToSnackBarActionDatabase()
            }

        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCreateTicket.isVisible = UserType.convertToType(AuthUtil.role) !is UserType.Mechanic
    }

    private fun navigateToChecklist(ticketId: Long, ticketNo: String) {
        val action = MechanicRepairedTicketsFragmentDirections
            .actionMechanicRepairedTicketsFragmentToMechanicRepairedTicketsChecklistFragment(
                ticketId,
                ticketNo
            )
        navigate(action)
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
            mainViewModel.insertToNfcDeviceDatabase(true)
            showFindMachineDialog {
                machineViewModel.getMachineByMachineNo(it)
            }
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
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("")
        integrator.initiateScan()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        val barCode = result?.contents
        machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showScanPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        // Start translation
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
        // End translation

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

    private fun navigateToCreateTicket(
        machineId: Long,
        machine: String,
        station: String,
        mfgLine: String,
        commonProblems: Long,
        origin: String
    ) {
        val action =
            MechanicRepairedTicketsFragmentDirections.actionMechanicRepairedTicketsFragmentToMechanicCreateTicketFragment(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
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
        val action = MechanicRepairedTicketsFragmentDirections
            .actionMechanicRepairedTicketsFragmentToReplaceMachineScanDetailsFragment(
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
        val action = MechanicRepairedTicketsFragmentDirections
            .actionMechanicRepairedTicketsFragmentToMoveMachineFragment(
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
        val action = MechanicRepairedTicketsFragmentDirections
            .actionMechanicRepairedTicketsFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

}
