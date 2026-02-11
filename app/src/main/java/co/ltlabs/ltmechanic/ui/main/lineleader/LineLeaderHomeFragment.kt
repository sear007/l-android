package co.ltlabs.ltmechanic.ui.main.lineleader

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.AppConfig.APK_LINK
import co.ltlabs.ltmechanic.constant.AppConfig.NEW_VERSION
import co.ltlabs.ltmechanic.constant.type.TicketType
import co.ltlabs.ltmechanic.databinding.FragmentLineLeaderHomeBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.databinding.PopupLineleaderLineListBinding
import co.ltlabs.ltmechanic.domain.asDatabaseModel
import co.ltlabs.ltmechanic.network.LineRequest
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderLineListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.ui.main.SocketViewModel
import co.ltlabs.ltmechanic.ui.main.filter.FilterLineAndAreaDialog
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderHomeViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class LineLeaderHomeFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var popupWindow: PopupWindow? = null

    private val viewModel: LineLeaderHomeViewModel by viewModels { providerFactory }
    private val lineViewModel: LineViewModel by viewModels { providerFactory }
    private val machineViewModel: MachineViewModel by viewModels { providerFactory }
    private val ticketViewModel: TicketViewModel by viewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val socketViewModel: SocketViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }

    lateinit var progressBar: ProgressBar

    private var selectedLinesStr = mutableListOf<String>()

    private var closeButtonHidden = false

    private var selfAssignLines = false

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var searchCount = 0

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private var mOptionsMenu: Menu? = null

    var action = ""

    private lateinit var binding: FragmentLineLeaderHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        binding = FragmentLineLeaderHomeBinding.inflate(inflater)

        binding.jTranslate = languageJsonObject
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        binding.usernameTextViewLL.text = AuthUtil.username

        val versionName = BuildConfig.VERSION_NAME
        binding.versionLL.text = "v$versionName"

        var connectedCount = 1

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    if (connectedCount == 1) {
                        dashboardViewModel.getDashboardStatistic()
                    }

                    connectedCount++

                    ConnectionUtil.setInternetConnected(false)
                } else {
                    connectedCount = 1
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        showProgressBar(true)

        binding.lineFilterLL.setOnClickListener {
            showFilterDialog()
        }

        coordinatorLayout = binding.coordinatorLayout

        binding.btnLLCreateTicket.setOnClickListener {
            action = "create_ticket"
            nfcViewModel.setNFCAction(NFCAction.CREATE_TICKET)
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        ticketViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {

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

                }
                ticketViewModel.finishInsertToSnackBarActionDatabase()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {


                when (action) {
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
                    }
                }

                machineViewModel.machineDetailsByMachineNoComplete()
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

        binding.btnLLReportedTickets.setOnClickListener {
            navigateToReportedTickets(TicketType.REPORTED)
        }

        binding.btnMCReopenedTickets.setOnClickListener {
            navigateToReportedTickets(TicketType.REOPEN)
        }

        binding.btnLLInRepairTickets.setOnClickListener {
            navigateToInRepairTickets()
        }

        binding.btnLLRepairedTickets.setOnClickListener {
            navigateToRepairedTickets()
        }

        binding.btnLLQueryMachine.setOnClickListener {
            action = "query_machine"
            nfcViewModel.setNFCAction(NFCAction.QUERY_MACHINE)
        }

        binding.btnLLSendRequest.setOnClickListener {
            action = "send_request"
            nfcViewModel.setNFCAction(NFCAction.SEND_REQUEST)
        }

        viewModel.reportedTicketsCount.observe(viewLifecycleOwner, Observer {
            binding.reportedTicketsCountTextViewLL.text = it.toString()
        })

        viewModel.reopenedTicketsCount.observe(viewLifecycleOwner) {
            binding.reopenedTicketsCountTextViewMC.text = it.toString()
        }

        viewModel.coRequestCount.observe(viewLifecycleOwner) {
            binding.changeOverCountTextView.text = it.toString()
        }

        viewModel.inRepairTicketsCount.observe(viewLifecycleOwner, Observer {
            binding.inRepairTicketsCountTextView.text = it.toString()
        })

        viewModel.repairedTicketsCount.observe(viewLifecycleOwner, Observer {
            binding.repairTicketsCountTextView.text = it.toString()
        })

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

        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    showProgressBar(false)
                }
            }
        })

        lineViewModel.noAssignedLines.observe(viewLifecycleOwner, Observer {
            closeButtonHidden = it
            selfAssignLines = it
            if (it) {
                showFilterDialog()
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
                    }
                }
                machineViewModel.machineStatusComplete()
            }
        })

        dashboardViewModel.selectedMfgAreas.observe(viewLifecycleOwner) { mfgAreas ->
            viewModel.insertToMfgAreasDatabase(mfgAreas.toTypedArray().asDatabaseModel())
        }

        lineViewModel.selectedMfgLines.observe(viewLifecycleOwner, Observer { mfgLine ->
            selectedLinesStr.clear()
            mfgLine.filter { it.checked == true }.forEach { selectedLinesStr.add(it.mfgLine) }
            mfgLine.forEach {
                it.username = AuthUtil.username
            }
            viewModel.insertToMfgLineDatabase(mfgLine.toTypedArray().asDatabaseModel())

        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenerToRefresh()
        handleStatisticCountObserve()

        binding.btnLLChangeOver.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_ChangeOverFragment)
        }

        // Set filter
        lifecycleScope.launchWhenCreated {
            dashboardViewModel.filter.collectLatest {
                if (dashboardViewModel.isFilterAll) binding.tvFilter.text =
                    "All Lines & Areas" else binding.tvFilter.text = it
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        mOptionsMenu = menu
        if (NEW_VERSION > BuildConfig.VERSION_NAME) {
            activity?.menuInflater?.inflate(R.menu.rocket_menu, menu)
            mOptionsMenu = null
        }
    }

    fun showRocketIcon() {
        if (NEW_VERSION > BuildConfig.VERSION_NAME) {
            mOptionsMenu?.let { activity?.menuInflater?.inflate(R.menu.rocket_menu, mOptionsMenu) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.updateNewApk -> UpdateManager.downloadNewApk(
                requireActivity(), APK_LINK, NEW_VERSION
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleStatisticCountObserve() {
        dashboardViewModel.getDashboardStatistic()

        lifecycleScope.launchWhenCreated {
            dashboardViewModel.statisticCount.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> binding.progressBar.showProgressBar(true)
                    Resource.Status.SUCCESS -> {
                        binding.progressBar.showProgressBar(false)
                        it.data?.let { count ->
                            binding.reportedTicketsCountTextViewLL.text = count.Reported?.toString()
                            binding.reopenedTicketsCountTextViewMC.text = count.Reopen?.toString()
                            binding.inRepairTicketsCountTextView.text = count.InRepair.toString()
                            binding.repairTicketsCountTextView.text = count.Repaired.toString()
                            binding.changeOverCountTextView.text = count.CORequest.toString()
                        }
                    }
                    else -> {
                        binding.progressBar.showProgressBar(false)
                        showSnackBar(binding.root, it.message.toString())
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            dashboardViewModel.areasNoLines.collectLatest {
                if (it != null) {
                    val areas = it.filter { _data -> _data.isSelected }
                    dashboardViewModel.setSelectedMfgArea(areas)
                    dashboardViewModel.selectedAreaNoLines.clear()
                    dashboardViewModel.selectedAreaNoLines.addAll(it)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            dashboardViewModel.assignedLines.collectLatest { line ->
                if (line.isNotEmpty()) {
                    line.map {
                        it.checked = true
                        selectedLinesStr.add(it.mfgLine)
                    }
                    lineViewModel.mfgLinesTemp = line.toMutableList()
                    lineViewModel.setSelectedMfgLines(line)
                    lineViewModel.selectedMfgLinesTemp.clear()
                    lineViewModel.selectedMfgLinesTemp.addAll(line)
                }
            }
        }
    }

    private fun listenerToRefresh() {
        lifecycleScope.launchWhenCreated {
            socketViewModel.refreshDashboard.collectLatest {
                if (it) {
                    dashboardViewModel.refreshDashboardStatistic(lineViewModel.selectedMfgLinesTemp)
                }
            }
        }
    }

    private fun dismissPopup() {
        findMachineBsDialog?.dismiss()
        viewModel.setEventLineListSearchResultNotFoundToFalse()
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private var filterDialog: FilterLineAndAreaDialog? = null
    private fun showFilterDialog() {
        if (filterDialog == null) filterDialog = FilterLineAndAreaDialog()
        if (filterDialog?.isAdded == false) {
            filterDialog?.show(childFragmentManager, filterDialog?.tag)
            filterDialog?.onDismissListener {
                filterDialog = null
            }

            filterDialog?.onSelect { areas, lines, checkAllFilter ->
                dashboardViewModel.isFilterAll = checkAllFilter
                areas?.let {
                    dashboardViewModel.setSelectedMfgArea(it)
                }
                dashboardViewModel.selectedAreaNoLines.clear()
                areas?.let { dashboardViewModel.selectedAreaNoLines.addAll(it) }

                lines?.let { lineViewModel.setSelectedMfgLines(it) }
                lineViewModel.selectedMfgLinesTemp.clear()
                if (lines != null) {
                    lineViewModel.selectedMfgLinesTemp.addAll(lines)
                }
                val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
                val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                val date = simpleDateFormat.format(Date())

                val linesRequest = mutableListOf<LineRequest>()
                lines?.filter { it.checked ?: false }?.forEach { mfgLine ->
                    linesRequest.add(
                        LineRequest(mfgLine.mfgLineId, date)
                    )
                }
                LineUtil.uncheckedLines.clear()

                dashboardViewModel.saveAreasNoLines(areas)
                lineViewModel.assignLines(linesRequest)
                dashboardViewModel.refreshDashboardStatistic(lineViewModel.selectedMfgLinesTemp)
            }
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
    }

    private fun getPopupWindow(): PopupWindow {

        viewModel.popupFirstOpen = true
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_lineleader_line_list, null)
        val closeButton = view.findViewById<TextView>(R.id.closePopup)
        val labelSelectLinesLL = view.findViewById<TextView>(R.id.labelSelectLinesLL)
        val linesearchEditTextLL = view.findViewById<TextView>(R.id.linesearchEditTextLL)
        val labelShowAllLines = view.findViewById<TextView>(R.id.labelShowAllLines)
        val noResultsTextViewLL = view.findViewById<TextView>(R.id.noResultsTextViewLL)
        val btnLLSelectLine = view.findViewById<TextView>(R.id.btnLLSelectLine)

        val binding = PopupLineleaderLineListBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {

                labelSelectLinesLL.post {
                    labelSelectLinesLL.text = getTranslation(labelSelectLinesLL.text.toString())
                }

                linesearchEditTextLL.post {
                    linesearchEditTextLL.hint = getTranslation(linesearchEditTextLL.hint.toString())
                }

                labelShowAllLines.post {
                    labelShowAllLines.text = getTranslation(labelShowAllLines.text.toString())
                }

                noResultsTextViewLL.post {
                    noResultsTextViewLL.text = getTranslation(noResultsTextViewLL.text.toString())
                }

                btnLLSelectLine.post {
                    btnLLSelectLine.text = getTranslation(btnLLSelectLine.text.toString())
                }
            }
        }
        // End translation

        val adapter = LineLeaderLineListAdapter(viewModel, lineViewModel)

        var recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        closeButton.setOnClickListener {
            lineViewModel.mfgLinesTemp.map { mfgLine ->
                LineUtil.uncheckedLines.map {
                    if (it.mfgLineId == mfgLine.mfgLineId) {
                        val checked: Boolean = mfgLine.checked ?: false
                        mfgLine.checked = !checked
                    }
                }
            }
            adapter.data = lineViewModel.mfgLinesTemp
            LineUtil.uncheckedLines.clear()
            dismissPopup()
            viewModel.setEventLineListSearchResultNotFoundToFalse()

            adapter.filter.filter("")
//            searchField.setText("")
//            lineViewModel.resetMfgLine(lineViewModel.mfgLinesTemp)
        }
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter

        viewModel.eventLineListSearchResultNotFound.observe(viewLifecycleOwner, Observer {
            val noResultTextView = view.findViewById<TextView>(R.id.noResultsTextViewLL)
            if (it) {
                noResultTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                noResultTextView.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
            }
        })

        val selectButton = view.findViewById<Button>(R.id.btnLLSelectLine)
        selectButton.setOnClickListener {
            val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date = simpleDateFormat.format(Date())

            val linesRequest = mutableListOf<LineRequest>()
            lineViewModel.mfgLinesTemp.filter { it.checked ?: false }.forEach { mfgLine ->
                linesRequest.add(
                    LineRequest(mfgLine.mfgLineId, date)
                )
            }
            lineViewModel.assignLines(linesRequest)

            dismissPopup()
            LineUtil.uncheckedLines.clear()
            adapter.data = lineViewModel.mfgLinesTemp
        }

        lineViewModel.mfgLinesAssignedByArea.observe(viewLifecycleOwner, Observer { mfgLines ->
            lineViewModel.selectedMfgLinesTemp = mfgLines.toMutableList()

            if (adapter.dataFull.isEmpty()) {
                adapter.dataFull = mfgLines
                lineViewModel.mfgLinesTemp = mfgLines.toMutableList()

            }

            adapter.data = mfgLines.toMutableList()

            val selectedLines = lineViewModel.mfgLinesTemp.filter { it.checked == true }
            selectButton.apply {
                if (selectedLines.isNotEmpty()) {
                    background = resources.getDrawable(R.drawable.button, null)
                } else {
                    setBackgroundColor(Color.GRAY)
                }

                isEnabled = selectedLines.isNotEmpty()
            }

            if (closeButtonHidden) {
                closeButton.visibility = View.INVISIBLE
            } else {
                closeButton.visibility =
                    if (selectedLines.isEmpty()) View.INVISIBLE else View.VISIBLE
            }

        })

        val searchField = view.findViewById<EditText>(R.id.linesearchEditTextLL)
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(text)

                searchCount = text?.length ?: 0
            }

        })

        lineViewModel.mfgLinesAssignedByArea.observe(viewLifecycleOwner, Observer { mfgLines ->
            if (mfgLines != null) {
//                if (searchCount == 0) {
                selectButton.isEnabled =
                    if (lineViewModel.mfgLinesTemp.none { it.checked == true }) {
                        selectButton.setBackgroundColor(Color.GRAY)
                        false
                    } else {
                        selectButton.background = resources.getDrawable(R.drawable.button, null)
                        true
                    }
//                }
            }
        })

        return PopupWindow(
            view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun showErrorPopupWindow(): PopupWindow {

        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            showFindMachineDialog { machineNo ->
                machineViewModel.getMachineByMachineNo(machineNo)
            }
        }

        binding.btnCancelLineSetup3.setOnClickListener {
            dismissPopup()
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
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

    private fun navigateToCreateTicket(
        machineId: Long,
        machine: String,
        station: String,
        mfgLine: String,
        commonProblems: Long,
        origin: String
    ) {
        val action =
            LineLeaderHomeFragmentDirections.actionLineLeaderHomeFragmentToCreateTicketFragment(
                machineId, machine, station, mfgLine, commonProblems, origin
            )
        navigate(action)
    }


    private fun navigateToReportedTickets(ticketType: String) {
        val bundle = bundleOf("ticket_type" to ticketType)
        findNavController().navigate(
            R.id.action_lineLeaderHomeFragment_to_lineLeaderReportedTicketsFragment, bundle
        )
    }

    private fun navigateToInRepairTickets() {
        val action =
            LineLeaderHomeFragmentDirections.actionLineLeaderHomeFragmentToLineLeaderInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToRepairedTickets() {
        val action =
            LineLeaderHomeFragmentDirections.actionLineLeaderHomeFragmentToLineLeaderRepairedTicketsFragment()
        navigate(action)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action =
            LineLeaderHomeFragmentDirections.actionLineLeaderHomeFragmentToQueryMachineFragment(
                machineId, machine
            )
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long, machine: String, rfid: String, subType: String, mfgLineId: Long
    ) {
        val action =
            LineLeaderHomeFragmentDirections.actionLineLeaderHomeFragmentToSendRequestFragment(
                machineId, machine, rfid, subType, mfgLineId
            )

        navigate(action)
    }

}
