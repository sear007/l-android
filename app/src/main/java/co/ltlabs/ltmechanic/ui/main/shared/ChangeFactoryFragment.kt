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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.databinding.FragmentChangeFactoryBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.domain.Factory
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.notification.NotificationClient
import co.ltlabs.ltmechanic.viewmodels.shared.ChangeFactoryViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "ChangeFactoryFragment";

class ChangeFactoryFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: ChangeFactoryViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ChangeFactoryViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private var selectedFactoryPos = 0

    private var action = ""

    private var factoryId = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentChangeFactoryBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        coordinatorLayout = binding.coordinatorLayout

        viewModel.getAssignedFactories()

//        Log.d(TAG, "onCreateView: AuthUtil.factoryId: ${AuthUtil.factoryId}")

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView2.text = getTranslation(toolBarTitleTextView2.text.toString())
                titleTextViewLL4.text = getTranslation(titleTextViewLL4.text.toString())
                btnSave.text = getTranslation(btnSave.text.toString())
            }
        }

        viewModel.factories.observe(viewLifecycleOwner, Observer { factories ->

            if (factories != null) {

                val tempList = mutableListOf<Factory>()

                factories.forEach { factoryTemp ->
                    if (!AuthUtil.role.toLowerCase()
                            .contains("admin") || !factoryTemp.name.toLowerCase().contains("admin")
                    ) {
                        tempList.add(factoryTemp)
                    }
                }

                activity?.let {
                    val list = mutableListOf<Factory>()
//                    list.add(Factory(0, ""))
                    tempList.forEachIndexed { index, factory ->

                        Log.d(TAG, "onCreateView: factory.factoryId: ${factory.factoryId}")
                        Log.d(TAG, "onCreateView: AuthUtil.factoryId: ${AuthUtil.factoryId}")

                        Log.d(TAG, "onCreateView: selectedFactoryPos: $selectedFactoryPos")

                        if (factory.factoryId == AuthUtil.factoryId) {
                            selectedFactoryPos = index
                        }
                        list.add(factory)

//                        try {
//                            if (!AuthUtil.role.toLowerCase().contains("admin") || !factory.name.toLowerCase().contains("admin")) {
//                                if (factory.factoryId == AuthUtil.factoryId) {
//                                    selectedFactoryPos = index
//                                }
//
//                                list.add(factory)
//                            } else {
//                                if (factory.factoryId == AuthUtil.factoryId) {
//                                    selectedFactoryPos = index + 1
//                                }
//                            }
//                        } catch (e: Exception) {
//
//                        }


                    }
                    val dataAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, list)
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerFactory.adapter = dataAdapter

                    binding.spinnerFactory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }

                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {

                                val factory = parent?.selectedItem as Factory
                                factoryId = factory.factoryId

                            }

                        }

                    try {
                        binding.spinnerFactory.setSelection(selectedFactoryPos)
                    } catch (e: Exception) {

                    }
                }


                viewModel.factoriesComplete()
            }

        })

        binding.btnSave.setOnClickListener {
            if (factoryId != 0L) {

                viewModel.switchFactory(requireContext(), factoryId)

            } else {
                binding.coordinatorLayout.showSnackbar(
                    languageJsonObject.getTranslation(
                        "Please select a factory."
                    )
                )
            }
        }

        viewModel.changeFactoryStatus.observe(viewLifecycleOwner, Observer {

            if (it != null) {


                when (it) {
                    ChangeFactoryStatus.SUCCESS -> {
                        NotificationClient.removeNotification(requireContext())

//                        Log.d(TAG, "onCreateView: factoryId: $factoryId")

                        AuthUtil.factoryId = factoryId

//                        Log.d(TAG, "onCreateView: AuthUtil.factoryId: ${AuthUtil.factoryId}")

                        if (AuthUtil.role == UserType.LINE_LEADER) {

                            navigateToLineLeaderHome()
                        } else {
                            navigateToMechanicHome()
                        }

                    }

                    else -> {

                    }
                }

                viewModel.changeFactoryStatusComplete()

            }

        })

        viewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {

                ApiStatus.LOADING -> {

                    binding.progressBar.showProgressBar(true)

                }

                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {
                val mfgLineId = machine.mfgLineId ?: 0

                when (action) {
                    "replace" -> {
                        if (machine.station.isNotBlank()) {
                            navigateToReplace(
                                mfgLineId,
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
            mainViewModel.insertToNfcDeviceDatabase(false)
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
        // End navigation setup

        return binding.root

    }

    private fun dismissPopup() {
        mainViewModel.insertToNfcDeviceDatabase(false)
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

    private fun navigateToMechanicHome() {
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToMechanicHomeFragment(
                "",
                false,
                "",
                ""
            )
        navigate(action)
    }

    private fun navigateToLineLeaderHome() {
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToLineLeaderHomeFragment()
        navigate(action)
    }

    private fun navigateToReplace(
        mfgLineId: Long,
        mfgLine: String,
        station: String,
        machine: String,
        machineId: Long
    ) {
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToReplaceMachineScanDetailsFragment(
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
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToMoveMachineFragment(
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
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToCreateTicketFragment(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
            )
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        mfgLineId: Long
    ) {
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToSendRequestFragment(
                machineId,
                machine,
                rfid,
                subType,
                mfgLineId
            )

        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = ChangeFactoryFragmentDirections
            .actionChangeFactoryFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }
}
