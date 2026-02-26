package co.ltlabs.ltmechanic.ui.main.mechanic.setupline

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentSetupLineBinding
import co.ltlabs.ltmechanic.databinding.PopupClearLineConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.ui.adapter.MechanicClearLineMachineListAdapter
import co.ltlabs.ltmechanic.ui.adapter.SetupLinePopupSelectLineListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "SetupLineFragment";

/**
 * A simple [Fragment] subclass.
 */
class SetupLineFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: SetupLineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupLineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    private val args: SetupLineFragmentArgs by navArgs()

    private var mfgLineId: Long = 0L

    private var machine = MachineInStation(0, "", "", "", "")

    private var mfgLine = ""

    private var machinesPlacedInStation = false

    private lateinit var coordinatorLayout: CoordinatorLayout
    private var action = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSetupLineBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        coordinatorLayout = binding.coordinatorLayout
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                confirmLineTextView.text = getTranslation(confirmLineTextView.text.toString())
                btnChangeLineSetup.text = getTranslation(btnChangeLineSetup.text.toString())
                btnClearLineSetup.text = getTranslation(btnClearLineSetup.text.toString())
                btnConfirmLineSetup.text = getTranslation(btnConfirmLineSetup.text.toString())
            }
        }

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
//                        navigateToReplace(mfgLineId.toLong(), machine.mfgLine ?: "", machine.station, machine.machine, machine.id)
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

        mfgLineId =
            try {
                if (LineUtil.selectedMfgLineId != 0L) LineUtil.selectedMfgLineId else args.mfgLineId
            } catch (e: Exception) {
                0L
            }

        machineViewModel.getMachinesInStation(mfgLineId)

        var connectedCount = 1

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    if (connectedCount == 1) {
                        machineViewModel.getMachinesInStation(mfgLineId)
                    }

                    connectedCount++

                    ConnectionUtil.setInternetConnected(false)
                } else {
                    connectedCount = 1
                }

                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        machineViewModel.machinesInStation.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "onCreateView: machinesInStation: $it")
            machinesPlacedInStation = it != null
        })

        binding.sewingLineTextView.text = LineUtil.selectedMfgLine

        mfgLine = binding.sewingLineTextView.text.toString()

        binding.btnClearLineSetup.setOnClickListener {
            Log.d(TAG, "onCreateView: machinesPlacedInStation: $machinesPlacedInStation")
            if (machinesPlacedInStation) {
//                lineViewModel.clearLine(mfgLineId)
                confirmToClearLine()
            } else {
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Line has already have 0 machines"))
            }

        }

        lineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    showProgressBar(false)
                }
            }
        })

        lineViewModel.clearLineStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    ClearLineStatus.CLEARED -> {
                        machineViewModel.getMachinesInStation(mfgLineId)
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Line successfully cleared."))
                    }
                    ClearLineStatus.SUCCESS -> {
                        lineViewModel.clearLine(mfgLineId)
                    }
                    ClearLineStatus.WITH_TICKET -> {
//                        binding.coordinatorLayout.showSnackbar("Clear failed. A machine in line has an existing ticket.")

                        val dm = DisplayMetrics()
                        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                        val width = (dm.widthPixels * .9).toInt()
                        val height = (dm.heightPixels * .6).toInt()

                        dismissPopup()
                        popupWindow = showClearLinePopupWindow()
                        popupWindow?.isOutsideTouchable = true
                        popupWindow?.isFocusable = true
                        popupWindow?.update(0, 0, width, height)
                        popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                        DimUtil.dimBehind(popupWindow)

                        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    }
                    else -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Something went wrong."))
                    }
                }
                lineViewModel.setClearLineStatusComplete()
            }
        })

        binding.btnConfirmLineSetup.setOnClickListener {
            Log.d(TAG, "onCreateView: machinesPlacedInStation: $machinesPlacedInStation")

//            if (LineUtil.finishedSetupLine) {
            if (machinesPlacedInStation) {
//                navigateToSetLineMachineDetails(binding.sewingLineTextView.text.toString(),
////                    machine.station,
////                    machine.machine,
////                    machine.machine,
////                    machine.id.toString(),
////                    machine.rfid)

                navigateToSetupLinePlaces()
            } else {
                navigateToSetupLineScanMachine(binding.sewingLineTextView.text.toString())
            }
//            } else {
//                navigateToSetupLineScanMachine(binding.sewingLineTextView.text.toString())
//            }
        }

        progressBar = binding.progressBar

        machineViewModel.machine.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                machine = it
                binding.btnConfirmLineSetup.isEnabled = true
            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    showProgressBar(false)
                }
            }
        })

        if (binding.sewingLineTextView.text.isNotBlank()) {
            machineViewModel.getMachineByStation("01", mfgLineId)
            binding.btnConfirmLineSetup.isEnabled = true
        }

        Log.d(TAG, "onCreateView: LineUtil.lastSelectedStation: ${LineUtil.lastSelectedStation}")

        machineViewModel.selectedMfgLine.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                dismissPopup()
                LineUtil.lastSelectedStation = ""
                binding.sewingLineTextView.text = it.mfgLine
                mfgLineId = it.mfgLineId
                mfgLine = it.mfgLine
                LineUtil.selectedMfgLineId = it.mfgLineId
                LineUtil.selectedMfgLine = it.mfgLine
                machineViewModel.getMachineByStation("01", mfgLineId)
                machineViewModel.getMachinesInStation(mfgLineId)
                binding.btnConfirmLineSetup.isEnabled = true

                machineViewModel.selectedMfgLineComplete()
            }

        })

        binding.btnChangeLineSetup.setOnClickListener {
            showPopupWindow(binding.root)
        }

        viewModel.eventLinesChanged.observe(viewLifecycleOwner, Observer {
            binding.sewingLineTextView.text = viewModel.selectedLine
        })

        return binding.root
    }

    private fun confirmToClearLine() {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_clear_setup_line, null)
        val builder = AlertDialog.Builder(requireContext()).setView(layout)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialog_title)
        val dialogContent = dialog.findViewById<TextView>(R.id.dialog_content)
        val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
        val okButton = dialog.findViewById<Button>(R.id.btnOk)

        with(languageJsonObject) {
            dialogTitle?.text = "${getTranslation(dialogTitle?.text.toString())}?"
            dialogContent?.text = "${getTranslation(dialogContent?.text.toString())}?"
            cancelButton?.text = getTranslation(cancelButton?.text.toString())
            okButton?.text = getTranslation(okButton?.text.toString())
        }

        layout.apply {
            cancelButton?.setOnClickListener { dialog.dismiss() }
            okButton?.setOnClickListener {
                lineViewModel.clearLineValidate(mfgLineId)
                dialog.dismiss()
            }
        }
    }

    private fun dismissPopup() {
        viewModel.setEventLineListSearchResultNotFoundToFalse()
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun showPopupWindow(view: View) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .93).toInt()

        dismissPopup()
        popupWindow = getPopupWindow()
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, -25)

        DimUtil.dimBehind(popupWindow)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun getPopupWindow(): PopupWindow {
        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_setup_line, null)

        val labelSelectLines = view.findViewById<TextView>(R.id.textView3)
        val linesearchEditText = view.findViewById<TextView>(R.id.linesearchEditTextSL)
        val noResultsTextViewSL = view.findViewById<TextView>(R.id.noResultsTextViewSL)

        val closeButton = view.findViewById<TextView>(R.id.closePopupSL)

        val adapter = SetupLinePopupSelectLineListAdapter(viewModel, machineViewModel)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        with(languageJsonObject) {
            labelSelectLines.text = getTranslation(labelSelectLines.text.toString())
            linesearchEditText.hint = getTranslation(linesearchEditText.hint.toString())
            noResultsTextViewSL.text = getTranslation(noResultsTextViewSL.text.toString())
        }

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner, Observer { mfgLines ->
            adapter.data = mfgLines.filter { it.checked == true }.toMutableList()
        })


        val searchField = view.findViewById<EditText>(R.id.linesearchEditTextSL)
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(text)
            }

        })

        viewModel.eventLineListSearchResultNotFound.observe(viewLifecycleOwner, Observer {
            val noResultTextView = view.findViewById<TextView>(R.id.noResultsTextViewSL)
            if (it) {
                noResultTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                noResultTextView.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
            }
        })

        viewModel.popupFirstOpen = true

        closeButton.setOnClickListener {
            dismissPopup()
            adapter.filter.filter("")
        }

        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        return PopupWindow(
            view,
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

    private fun showClearLinePopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupClearLineConfirmationMessageBinding.inflate(inflater)

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
                labelCancel.text = "${getTranslation(labelCancel.text.toString())}:"
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            dismissPopup()
            lineViewModel.clearLine(mfgLineId)
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

        LineUtil.machinesHasTickets.forEach {
            it.station = "$mfgLine - ${it.station}"
        }

        val adapter = MechanicClearLineMachineListAdapter()
        val linearLayoutManager = LinearLayoutManager(activity)
        adapter.data = LineUtil.machinesHasTickets
        binding.clearLineMachinesRecyclerView.layoutManager = linearLayoutManager
        binding.clearLineMachinesRecyclerView.adapter = adapter

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

//    private fun navigateToSetLineMachineDetails(selectedLine: String, place: String, machineNo: String, machineCode: String, machineID: String, rfid: String, subType: String) {
//        val action = SetupLineFragmentDirections.actionSetupLineFragmentToSetupLineMachineInPlaceDetailsFragment(selectedLine,
//            place,
//            machineNo,
//            machineCode,
//            machineID,
//            rfid,
//            mfgLineId,
//            true, "", subType)
//        navigate(action)
//    }

    private fun navigateToSetupLineScanMachine(mfgLine: String) {
        val selectedPlace = "01"
//        val selectedPlace = if (LineUtil.lastSelectedStation == "") "01" else LineUtil.lastSelectedStation
//        Log.d(TAG, "navigateToSetupLineScanMachine: selectedPlace: $selectedPlace")
        val action =
            SetupLineFragmentDirections.actionSetupLineFragmentToSetupLineScanMachineFragment(
                mfgLine,
                selectedPlace,
                mfgLineId,
                ""
            )
        navigate(action)
    }

    private fun navigateToSetupLinePlaces() {
        val action = SetupLineFragmentDirections.actionSetupLineFragmentToSetupLinePlacesFragment(
            mfgLineId,
            mfgLine
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
        val action = SetupLineFragmentDirections
            .actionSetupLineFragmentToReplaceMachineScanDetailsFragment(
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
        val action = SetupLineFragmentDirections
            .actionSetupLineFragmentToMoveMachineFragment(
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
        val action = SetupLineFragmentDirections
            .actionSetupLineFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

}
