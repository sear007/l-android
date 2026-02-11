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
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentMoveMachineBinding
import co.ltlabs.ltmechanic.databinding.PopupMoveMachineMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.domain.NonLineArea
import co.ltlabs.ltmechanic.ui.dialog.movemachine.AreaDestinationBSDialog
import co.ltlabs.ltmechanic.ui.dialog.movemachine.BuildingDestinationBSDialog
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MoveMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.ReferenceViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "MoveMachineFragment";

/**
 * A simple [Fragment] subclass.
 */
class MoveMachineFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MoveMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MoveMachineViewModel::class.java)
    }

    private val referenceViewModel: ReferenceViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReferenceViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var action = ""

    private val args: MoveMachineFragmentArgs by navArgs()
    private lateinit var binding: FragmentMoveMachineBinding

    private var destinationId = 0L
    private var selectedLocation = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoveMachineBinding.inflate(inflater)

        progressBar = binding.progressBar
        coordinatorLayout = binding.coordinatorLayout

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                machineTextView.text = getTranslation(machineTextView.text.toString())
                rfidLabel.text = getTranslation(rfidLabel.text.toString())
                subTypeLabel.text = getTranslation(subTypeLabel.text.toString())
                locationLabel2.text = getTranslation(locationLabel2.text.toString())
                tvDestinationLabel.text = "${languageJsonObject.getTranslation("Destination Building")} *"
                tvAreaLabel.text = "${languageJsonObject.getTranslation("Destination Area")} *"
                tvArea.hint = getTranslation(tvArea.hint.toString())
                tvBuilding.hint = getTranslation(tvBuilding.hint.toString())
                btnCancelLineSetup.text = getTranslation(btnCancelLineSetup.text.toString())
                btnMoveMachine.text = getTranslation(btnMoveMachine.text.toString())
            }
        }

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    referenceViewModel.getNoneLineAreas()

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        referenceViewModel.getNoneLineAreas()

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.btnCancelLineSetup.setOnClickListener {
            findNavController().popBackStack()
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
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
                    "query_machine" -> {
                        navigateToQueryMachine(machine.id, machine.machine)
                    }
                }

                machineViewModel.machineDetailsByRfidComplete()
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

        referenceViewModel.nonLineAreas.observe(viewLifecycleOwner, Observer { nonLineAreas ->
            if (nonLineAreas != null) {

                activity?.let {
                    val list = mutableListOf<NonLineArea>()
//                    list.add(SolutionType(0, "", ""))
                    nonLineAreas.forEach { noneLineArea ->
                        list.add(noneLineArea)
                    }
                    val dataAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, list)
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    binding.noneLineAreaSpinner.adapter = dataAdapter
//
//                    binding.noneLineAreaSpinner.onItemSelectedListener =
//                        object : AdapterView.OnItemSelectedListener {
//                            override fun onNothingSelected(parent: AdapterView<*>?) {
//                            }
//
//                            override fun onItemSelected(
//                                parent: AdapterView<*>?,
//                                view: View?,
//                                position: Int,
//                                id: Long
//                            ) {
//
//                                val noneLineArea = parent?.selectedItem as NonLineArea
////                            solutionTypeId = solutionType.id.toString()
//                                destinationId = noneLineArea.id
//                                selectedLocation = noneLineArea.desc
//
//                            }
//
//                        }
                }

                referenceViewModel.nonLineAreasComplete()
            }
        })

        binding.btnMoveMachine.setOnClickListener {
            if (selectedLocation.isNotBlank()) {
                if (args.station.isNotBlank()) {
                    dismissPopup()

                    val dm = DisplayMetrics()
                    activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                    val width = (dm.widthPixels * .9).toInt()
                    val height = (dm.heightPixels * .38).toInt()


                    dismissPopup()
                    popupWindow = showPopupWindow()
                    popupWindow?.isOutsideTouchable = true
                    popupWindow?.isFocusable = true
                    popupWindow?.update(0, 0, width, height)
                    popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

                    DimUtil.dimBehind(popupWindow)

                    popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                } else {
                    lineViewModel.moveMachine(args.machineId, destinationId, buildingId)
                }
            } else {
                coordinatorLayout.showSnackbar(
                    languageJsonObject.getTranslation(
                        "Please select a destination area"
                    )
                )
            }


        }

        lineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        lineViewModel.machineMoved.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    navigateToHome()
                } else {
                    binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Move failed."))
                }
            }
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setDefaultData()

        // Listen error message of move machine
        lifecycleScope.launchWhenCreated {
            lineViewModel.message.collectLatest {
                showSnackBar(binding.root, it)
            }
        }
    }

    private var buildingDialog: BuildingDestinationBSDialog? = null
    private var building: String? = null
    private var buildingId: Int = 0
    private fun showBuildingDialog() {
        if (buildingDialog == null)
            buildingDialog =
                BuildingDestinationBSDialog.newInstance(building, buildingId)

        if (buildingDialog?.isAdded == false) {
            buildingDialog?.isCancelable = false
            buildingDialog?.show(
                childFragmentManager,
                buildingDialog?.tag
            )

            buildingDialog?.onDismissListener {
                buildingDialog = null
            }

            buildingDialog?.setOnOkClicked { buildingName, buildingId ->
                if (this.buildingId != buildingId) {
                    area = ""
                    areaId = 0
                    binding.tvArea.text = ""
                }
                this.building = buildingName
                this.buildingId = buildingId
                binding.tvBuilding.text = buildingName
                enabledArea()
            }
        }
    }

    private var areaDialog: AreaDestinationBSDialog? = null
    private var area: String? = null
    private var areaId: Int = 0
    private fun showAreaDialog() {
        if (areaDialog == null)
            areaDialog = AreaDestinationBSDialog.newInstance(
                area,
                areaId,
                buildingId,
                "Move Machine to"
            )

        if (areaDialog?.isAdded == false) {
            areaDialog?.isCancelable = false
            areaDialog?.show(childFragmentManager, areaDialog?.tag)
            areaDialog?.onDismissListener {
                areaDialog = null
            }

            areaDialog?.setOnOkClicked { areaName, areaId ->
                this.area = areaName
                this.areaId = areaId
                binding.tvArea.text = areaName
                destinationId = areaId.toLong()
                selectedLocation = "$building - $areaName"
            }
        }
    }

    private fun setupListener() {
        binding.tvBuilding.setOnClickListener {
            showBuildingDialog()
        }

        binding.tvArea.setOnClickListener {
            showAreaDialog()
        }
    }

    private fun enabledArea() {
        binding.tvArea.isEnabled = binding.tvBuilding.text.isNotEmpty()
        binding.tvArea.alpha = if (binding.tvArea.isEnabled) 1f else 0.8f
    }

    private fun setDefaultData() {
        building = args.buildingName
        buildingId = args.buildingId
        binding.apply {
            tvBuilding.text = args.buildingName
            machineNoTextView.text = args.machine
            rfidTextView.text = args.rfid
            subTypeTextView.text = args.subType
            tvArea.isEnabled = false
            locationTextView.text = if (args.station.isNotBlank()) {
                "${args.mfgLine} - ${args.station}"
            } else {
                args.location
            }

            if (args.rfid.isBlank()) {
                rfidLabel.visibility = View.GONE
                rfidTextView.visibility = View.GONE
            }
            enabledArea()
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

    private fun showPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupMoveMachineMessageBinding.inflate(inflater)

        val location = if (args.station.isNotBlank()) {
            "${args.mfgLine} - ${args.station}"
        } else {
            args.location
        }

        if (MachineUtil.machineHasOpenTickets) {
            binding.textView3.text =
                StrUtil.replaceStr(
                    languageJsonObject
                        .getTranslation(
                            "[] is checked in at [] and has open repair ticket [], would you like to proceed moving the machine to [] ?"
                        )
                )
                    .format(
                        args.machine,
                        location,
                        MachineUtil.machineOpenTicketNo,
                        selectedLocation
                    )
        } else {
            binding.textView3.text =
                StrUtil.replaceStr(
                    languageJsonObject
                        .getTranslation(
                            "[] is checked in at [], would you like to proceed moving the machine to [] ?"
                        )
                )
                    .format(args.machine, location, selectedLocation)
        }

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            dismissPopup()
            lineViewModel.moveMachine(args.machineId, destinationId, buildingId)
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    private fun navigateToHome() {
//        val action = MoveMachineFragmentDirections.actionMoveMachineFragmentToMechanicHomeFragment(
//            "",
//            false,
//            args.machine,
//            selectedLocation
//        )
        val bundle = bundleOf(
            "replaceAction" to "",
            "replaceSuccess" to false,
            "replaceMfgLine" to args.machine,
            "replaceStation" to selectedLocation,
            "isFromMoveMC" to true
        )
        findNavController().navigate(R.id.action_global_to_mechanicHomeFragment, bundle)
    }

    private fun navigateToInRepairTickets() {
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToMechanicInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToReportedTickets() {
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToMechanicReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToRepairedTickets() {
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToMechanicRepairedTicketsFragment()
        navigate(action)
    }

    private fun navigateToLineOverview() {
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToLineStatusFragment()
        navigate(action)
    }

    private fun navigateToSetupLine() {
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToSetupLineFragment(
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
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToReplaceMachineScanDetailsFragment(
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
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentSelf(
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
        val action = MoveMachineFragmentDirections
            .actionMoveMachineFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

}
