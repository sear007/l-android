package co.ltlabs.ltmechanic.ui.main.lineleader.createticket

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentCreateTicketBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderMachineProblemsAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.CreateTicketViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "CreateTicketFragment";

class CreateTicketFragment : BaseFragment() {

    private val viewModel: CreateTicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(CreateTicketViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private var action = ""

    private lateinit var progressBar: ProgressBar
    private lateinit var coordinatorLayout: CoordinatorLayout

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var popupWindow: PopupWindow? = null

    private val args: CreateTicketFragmentArgs by navArgs()

    private var selectedProblemTypeId = 0L

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentCreateTicketBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.machineNoTextView.text = args.machine

        progressBar = binding.progressBar
        coordinatorLayout = binding.coordinatorLayout

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            if (machine != null) {

                when (action) {
                    "create_ticket" -> {
                        loadedMachine = machine.machine
                        loadedMachineId = machine.id
                        loadedMachineMfgLine = machine.mfgLine ?: ""
                        loadedMachineStation = machine.station

                        Log.d(TAG, "onCreateView: machine.mfgLine: ${machine.mfgLine}")
                        Log.d(TAG, "onCreateView: selectedLinesStr: $selectedLinesStr")
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
                    "query_machine" -> {
                        dismissPopup()
                        navigateToQueryMachine(machine.id, machine.machine)
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
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machine != null) {

                when (action) {
                    "create_ticket" -> {
                        loadedMachine = machine.machine
                        loadedMachineId = machine.id
                        loadedMachineMfgLine = machine.mfgLine ?: ""
                        loadedMachineStation = machine.station

                        Log.d(TAG, "onCreateView: machine.mfgLine: ${machine.mfgLine}")
                        Log.d(TAG, "onCreateView: selectedLinesStr: $selectedLinesStr")
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

                                DimUtil.dimBehind(popupWindow)

                                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))


                            }
                        } else {
                            dismissPopup()
                        }
                    }
                    "query_machine" -> {
                        dismissPopup()
                        navigateToQueryMachine(machine.id, machine.machine)
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
                }

                machineViewModel.machineDetailsByRfidComplete()
            }
        })

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
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

//                        val dm = DisplayMetrics()
//                        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//
//                        val width = (dm.widthPixels * .9).toInt()
//                        val height = (dm.heightPixels * .5).toInt()
//
//                        dismissPopup()
//                        popupWindow = showErrorPopupWindow()
//                        popupWindow?.isOutsideTouchable = true
//                        popupWindow?.isFocusable = true
//                        popupWindow?.update(0, 0, width, height)
//                        popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)
//
//                        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    }
                }

                machineViewModel.machineStatusComplete()
            }
        })

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        ticketViewModel.commonProblems2.observe(viewLifecycleOwner, Observer {
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

                ticketViewModel.commonProblems2Complete()
            }
        })

        when (args.commonProblems) {

            0L -> {
                binding.commonProblem1TextView.visibility = View.GONE
                binding.commonProblem2TextView.visibility = View.GONE
                binding.commonProblem3TextView.visibility = View.GONE
                binding.lastReportedProblemTextView.visibility = View.GONE
            }

            1L -> {
                binding.commonProblem2TextView.visibility = View.GONE
                binding.commonProblem3TextView.visibility = View.GONE
            }

            2L -> {
                binding.commonProblem3TextView.visibility = View.GONE
            }

        }

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                labelScanMachine.text = getTranslation(labelScanMachine.text.toString())
                labelSelectedProblem.text = getTranslation(labelSelectedProblem.text.toString()) + " *"
                problemTextView.text = getTranslation(problemTextView.text.toString())
                btnSeeAllProblems.text = getTranslation(btnSeeAllProblems.text.toString())
                btnCancel.text = getTranslation(btnCancel.text.toString())
                btnNext.text = getTranslation(btnNext.text.toString())
            }
        }
        // End translation

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    ticketViewModel.getMachineProblems(args.machineId)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        ticketViewModel.getMachineProblems(args.machineId)

        binding.btnCancel.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.commonProblem1TextView.setOnClickListener {
            binding.problemTextView.text = binding.commonProblem1TextView.text
            selectedProblemTypeId = binding.commonProblem1TextView.tag.toString().toLong()
        }

        binding.commonProblem2TextView.setOnClickListener {
            binding.problemTextView.text = binding.commonProblem2TextView.text
            selectedProblemTypeId = binding.commonProblem2TextView.tag.toString().toLong()
        }

        binding.commonProblem3TextView.setOnClickListener {
            binding.problemTextView.text = binding.commonProblem3TextView.text
            selectedProblemTypeId = binding.commonProblem3TextView.tag.toString().toLong()
        }

        binding.lastReportedProblemTextView.setOnClickListener {
            binding.problemTextView.text = binding.lastReportedProblemTextView.text
            selectedProblemTypeId = binding.lastReportedProblemTextView.tag.toString().toLong()
        }

        binding.btnNext.setOnClickListener {
            navigateToAttach(
                args.machineId,
                args.machine,
                binding.problemTextView.text.toString(),
                if (selectedProblemTypeId == 0L) 0L else selectedProblemTypeId,
                args.station,
                args.mfgLine,
                args.origin
            )
        }

        ticketViewModel.commonProblems.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                if (it.isNotEmpty()) {
                    when (it.size) {

                        3 -> {
                            binding.commonProblem1TextView.text =
                                languageJsonObject.getTranslation(it[0].desc1)
                            binding.commonProblem1TextView.tag = it[0].problemTypeId
                            binding.commonProblem2TextView.text =
                                languageJsonObject.getTranslation(it[1].desc1)
                            binding.commonProblem2TextView.tag = it[1].problemTypeId
                            binding.commonProblem3TextView.text =
                                languageJsonObject.getTranslation(it[2].desc1)
                            binding.commonProblem3TextView.tag = it[2].problemTypeId
                        }

                        2 -> {
                            binding.commonProblem1TextView.text =
                                languageJsonObject.getTranslation(it[0].desc1)
                            binding.commonProblem1TextView.tag = it[0].problemTypeId
                            binding.commonProblem2TextView.text =
                                languageJsonObject.getTranslation(it[1].desc1)
                            binding.commonProblem2TextView.tag = it[1].problemTypeId
//                            binding.commonProblem3TextView.visibility = View.GONE
                        }

                        else -> {
                            binding.commonProblem1TextView.text =
                                languageJsonObject.getTranslation(it[0].desc1)
                            binding.commonProblem1TextView.tag = it[0].problemTypeId
//                            binding.commonProblem2TextView.visibility = View.GONE
//                            binding.commonProblem3TextView.visibility = View.GONE
                        }

                    }
                }
//                else {
//                    binding.commonProblem1TextView.visibility = View.GONE
//                    binding.commonProblem2TextView.visibility = View.GONE
//                    binding.commonProblem3TextView.visibility = View.GONE
//                }


            }
        })

        ticketViewModel.latestProblems.observe(viewLifecycleOwner, Observer { latestProblems ->
            Log.d(TAG, "onCreateView: it: $latestProblems")
            if (latestProblems != null) {
                if (latestProblems.isNotEmpty()) {
//                    binding.lastReportedProblemTextView.text = latestProblems[0].desc1
                    binding.lastReportedProblemTextView.tag = latestProblems[0].problemTypeId

                    binding.lastReportedProblemTextView.setOnClickListener {
                        binding.problemTextView.text = latestProblems[0].desc1
                        selectedProblemTypeId = latestProblems[0].problemTypeId
                        Log.d(TAG, "onCreateView: selectedProblemTypeId: $selectedProblemTypeId")
                    }
                }
            }
        })


        binding.btnSeeAllProblems.setOnClickListener {
            showPopupWindow(binding.root)
        }

        viewModel.selectedProblem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.problemTextView.text = it.desc1
                selectedProblemTypeId = it.problemTypeId
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

//        LanguageUtil.languageSelected.observe(viewLifecycleOwner, Observer {
//            Log.d(TAG, "onCreateView: languageSelected: $it")
//            if (it != null) {
//                if (it) {
        // Start translation
//                    if (LanguageUtil.selectedLanguage != "en") {
        with(languageJsonObject) {
            with(binding) {

                toolBarTitleTextView.post {
                    toolBarTitleTextView.text =
                        getTranslation(toolBarTitleTextView.text.toString())
                }

                labelScanMachine.post {
                    labelScanMachine.text =
                        getTranslation(labelScanMachine.text.toString())
                }

                labelSelectedProblem.post {
                    labelSelectedProblem.text =
                        getTranslation(labelSelectedProblem.text.toString())
                }

                commonProblem1TextView.post {
                    commonProblem1TextView.text =
                        getTranslation(commonProblem1TextView.text.toString())
                }

                commonProblem2TextView.post {
                    commonProblem2TextView.text =
                        getTranslation(commonProblem2TextView.text.toString())
                }

                commonProblem3TextView.post {
                    commonProblem3TextView.text =
                        getTranslation(commonProblem3TextView.text.toString())
                }

                lastReportedProblemTextView.post {
                    lastReportedProblemTextView.text =
                        getTranslation(lastReportedProblemTextView.text.toString())
                }

                btnSeeAllProblems.post {
                    btnSeeAllProblems.text =
                        getTranslation(btnSeeAllProblems.text.toString())
                }

                btnCancel.post {
                    btnCancel.text =
                        getTranslation(btnCancel.text.toString())
                }

                btnNext.post {
                    btnNext.text =
                        getTranslation(btnNext.text.toString())
                }

//                            titleTextViewMC.text = getTranslation(titleTextViewMC.text.toString())
//                            reportedTicketsTextViewMC.text = getTranslation(reportedTicketsTextViewMC.text.toString())
//                            inRepairTicketsTextViewMC.text = getTranslation(inRepairTicketsTextViewMC.text.toString())
//                            repairedTicketsTextViewMC.text = getTranslation(repairedTicketsTextViewMC.text.toString())
//                            lineOverviewTextViewMC.text = getTranslation(lineOverviewTextViewMC.text.toString())
//                            replaceMachineTextViewMC.text = getTranslation(replaceMachineTextViewMC.text.toString())
//                            setupLineTextViewMC.text = getTranslation(setupLineTextViewMC.text.toString())
//                            maintenanceTextViewMC.text = getTranslation(maintenanceTextViewMC.text.toString())
            }
        }
//                    }
        // End translation

//                    LanguageUtil.languageSelected.value = null
//                }
//
//            }
//        })

        return binding.root
    }

    private fun getPopupWindow(): PopupWindow {
        viewModel.popupFirstOpen = true
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.popup_spinner_with_search, null)
        val labelSelectMachineProblem = view.findViewById<TextView>(R.id.labelSelectItem)
        val searchField = view.findViewById<EditText>(R.id.itemSearchEditText)
        val selectButton = view.findViewById<Button>(R.id.selectButton)
        val closeButton = view.findViewById<ImageView>(R.id.closePopup)
        val noProblemFound = view.findViewById<TextView>(R.id.noResultsTextView)

        labelSelectMachineProblem.text = resources.getString(R.string.select_machine_problem)

        closeButton.setOnClickListener {
            dismissPopup()
        }

        with(languageJsonObject) {
            labelSelectMachineProblem.text =
                getTranslation(labelSelectMachineProblem.text.toString())
            searchField.hint = getTranslation(searchField.hint.toString())
            selectButton.text = getTranslation(selectButton.text.toString())
            noProblemFound.text = getTranslation(noProblemFound.text.toString())
        }

        val adapter = LineLeaderMachineProblemsAdapter(viewModel, ticketViewModel)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        closeButton.setOnClickListener {
            dismissPopup()
            ticketViewModel.resetProblems(ticketViewModel.problemTemp)
        }
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter

        viewModel.eventLineListSearchResultNotFound.observe(viewLifecycleOwner, Observer {
            if (it) {
                noProblemFound.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                noProblemFound.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
            }
        })

        selectButton.setOnClickListener {
            viewModel.setSelectedProblem(ticketViewModel.selectedProblemTemp)
            ticketViewModel.resetProblems(ticketViewModel.problemTemp)
            dismissPopup()
        }

        ticketViewModel.problems.observe(viewLifecycleOwner, Observer { problems ->

//            ticketViewModel.selectedProblemTemp = problems

            if (adapter.dataFull.isEmpty()) {
                adapter.dataFull = problems
                ticketViewModel.problemTemp = problems.toMutableList()

            }

            adapter.data = problems.toMutableList()

            val selectedProblem = problems.filter { it.checked == true }
            selectButton.apply {
                if (selectedProblem.isNotEmpty()) {
                    background = resources.getDrawable(R.drawable.button, null)
                } else {
                    setBackgroundColor(Color.GRAY)
                }

                isEnabled = selectedProblem.isNotEmpty()
            }

//            if (closeButtonHidden) {
//                closeButton.visibility = View.INVISIBLE
//            } else {
//                closeButton.visibility = if (selectedProblem.isEmpty()) View.INVISIBLE else View.VISIBLE
//            }

        })

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(text)
            }

        })

        return PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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
        popupWindow?.isOutsideTouchable = false

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

//        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun showScanPopupWindow(): PopupWindow {

//        if (nfcAdapter == null) {
//            Toast.makeText(activity, "NFC feature is not supported for this device", Toast.LENGTH_SHORT).show()
//        }


//        nfcAdapter?.let {
//            activity?.let { it1 -> NFCUtil.enableNFCInForeground(it, it1, javaClass) }
//        }

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
//            nfcAdapter?.let {
//                activity?.let { it1 -> NFCUtil.disableNFCInForeground(it, it1) }
//            }
            mainViewModel.insertToNfcDeviceDatabase(false)
            dismissPopup()
        }

        binding.btnSubmitMachine.setOnClickListener {
            progressBar.showProgressBar(true)
            machineViewModel.getMachineByMachineNo(binding.machineEditText.text.toString())

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
//        if (requestCode == 0) {
//            if (resultCode == CommonStatusCodes.SUCCESS) {
//                dismissPopup()
//                if (data != null) {
//                    var barcode: Barcode? = data.getParcelableExtra("barcode")
//                    Log.d(TAG, "onActivityResult: barcode: ${barcode?.displayValue.toString()}")
//                    machineViewModel.getMachineByMachineNo(barcode?.displayValue.toString())
//                } else {
//                    coordinatorLayout.showSnackbar(
//                        languageJsonObject.getTranslation(
//                            "No QR code found"
//                        )
//                    )
//                }
//            }
//        } else {
//
//        }

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        val barCode = result?.contents
        machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
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

    private fun navigateToAttach(
        machineId: Long,
        machine: String,
        problem: String,
        problemTypeId: Long,
        station: String,
        mfgLine: String,
        origin: String
    ) {
        val action =
            CreateTicketFragmentDirections.actionCreateTicketFragmentToCreateTicketAttachFragment(
                machineId,
                machine,
                problemTypeId,
                problem,
                station,
                mfgLine,
                origin
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
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentSelf(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
            )
        navigate(action)
    }

    private fun navigateToReportedTickets() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToLineLeaderReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToInRepairTickets() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToLineLeaderInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToRepairedTickets() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToLineLeaderRepairedTicketsFragment()
        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

    private fun navigateToNotification() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToNotificationFragment()
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        mfgLineId: Long
    ) {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToSendRequestFragment(
                machineId,
                machine,
                rfid,
                subType,
                mfgLineId
            )

        navigate(action)
    }

    private fun navigateToHome() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToLineLeaderHomeFragment()
        navigate(action)
    }

    private fun navigateToChangePassword() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToChangePasswordFragment()
        navigate(action)
    }

    private fun navigateToChangeFactory() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToChangeFactoryFragment()
        navigate(action)
    }

    private fun navigateToChangeLanguage() {
        val action = CreateTicketFragmentDirections
            .actionCreateTicketFragmentToChangeLanguageFragment()
        navigate(action)
    }

}
