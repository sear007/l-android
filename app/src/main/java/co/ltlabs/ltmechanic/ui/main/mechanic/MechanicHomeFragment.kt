package co.ltlabs.ltmechanic.ui.main.mechanic

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import co.ltlabs.ltmechanic.databinding.FragmentMechanicHomeBinding
import co.ltlabs.ltmechanic.databinding.PopupMechanicLineListBinding
import co.ltlabs.ltmechanic.domain.Machine
import co.ltlabs.ltmechanic.domain.asDatabaseModel
import co.ltlabs.ltmechanic.network.LineRequest
import co.ltlabs.ltmechanic.ui.adapter.MechanicLineListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.ui.main.SocketViewModel
import co.ltlabs.ltmechanic.ui.main.filter.FilterLineAndAreaDialog
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.UpdateManager.downloadNewApk
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicHomeViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MechanicHomeFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var searching = true
    private var searchCount = 0
    private var mOptionsMenu: Menu? = null

    private val viewModel: MechanicHomeViewModel by viewModels { providerFactory }
    private val lineViewModel: LineViewModel by viewModels { providerFactory }
    private val machineViewModel: MachineViewModel by viewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val socketViewModel: SocketViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }

    private var popupWindow: PopupWindow? = null

    private var mfgLineStr = ""
    private var mfgLineId = 0L

    private var selectedLinesStr = mutableListOf<String>()

    private lateinit var binding: FragmentMechanicHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMechanicHomeBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.jTranslate = languageJsonObject
        binding.usernameTextViewMC.text = AuthUtil.username
        val versionName = BuildConfig.VERSION_NAME
        binding.versionMC.text = "v$versionName"
        var connectedCount = 1
        ConnectionUtil.internetConnected.observe(viewLifecycleOwner) {
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
        }

        lineViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    binding.progressBar.showProgressBar(false)
                    binding.btnMCSetupLine.isEnabled = true
                }
            }
        }

        viewModel.reportedTicketsCount.observe(viewLifecycleOwner) {
            binding.reportedTicketsCountTextViewMC.text = it.toString()
        }
        viewModel.reopenedTicketsCount.observe(viewLifecycleOwner) {
            binding.reopenedTicketsCountTextViewMC.text = it.toString()
        }

        viewModel.coRequestCount.observe(viewLifecycleOwner) {
            binding.changeOverCountTextViewMC.text = it.toString()
        }

        viewModel.inRepairTicketsCount.observe(viewLifecycleOwner) {
            binding.inRepairTicketsCountTextViewMC.text = it.toString()
        }

        viewModel.repairedTicketsCount.observe(viewLifecycleOwner) {
            binding.repairedTicketsCountTextViewMC.text = it.toString()
        }

        viewModel.maintenanceTicketsCount.observe(viewLifecycleOwner) {
            binding.maintenanceTicketsCountTextViewMC.text = it.toString()
        }

        dashboardViewModel.selectedMfgAreas.observe(viewLifecycleOwner) { mfgAreas ->
            viewModel.insertToMfgAreasDatabase(mfgAreas.toTypedArray().asDatabaseModel())
        }

        lineViewModel.selectedMfgLines.observe(viewLifecycleOwner) { mfgLine ->
            val selectedLines = mfgLine.filter { it.checked == true }
            selectedLinesStr.clear()
            mfgLine.filter { it.checked == true }.forEach { selectedLinesStr.add(it.mfgLine) }

            if (selectedLines.isNotEmpty()) {
                LineUtil.selectedMfgLine = selectedLines[0].mfgLine
                LineUtil.selectedMfgLineId = selectedLines[0].mfgLineId
                mfgLineStr = selectedLines[0].mfgLine
                mfgLineId = selectedLines[0].mfgLineId
            }

            binding.btnMCSetupLine.isEnabled = selectedLines.isNotEmpty()

            mfgLine.forEach {
                it.username = AuthUtil.username
            }

            viewModel.insertToMfgLineDatabase(
                mfgLine.sortedBy { it.seq }.toTypedArray().asDatabaseModel()
            )
        }

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner) { machine ->
            successFindMachine(machine)
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner) { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }
        }

        lineViewModel.lineAssignStatus.observe(viewLifecycleOwner) {
            when (it) {
                LineAssignStatus.SUCCESS -> {

                    lineViewModel.getAssignedLinesByArea()
                    dismissPopup()
                }
            }
        }

        machineViewModel.machineStatus.observe(viewLifecycleOwner) { machineStatus ->
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
        }

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

        machineViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {

                    when (it[0].action) {
                        SNACK_BAR_ACTION_REPLACE_MACHINE -> {
                            if (it[0].show) {

                                val replaceMfgLine = arguments?.getString("replaceMfgLine") ?: ""
                                val replaceStation = arguments?.getString("replaceStation") ?: ""
//                                val message = "Line ${args.replaceMfgLine} ${args.replaceStation} has been replaced"
                                val message =
                                    "${languageJsonObject.getTranslation("Station")} $replaceStation ${
                                        languageJsonObject.getTranslation("has been replaced")
                                    }"
                                binding.coordinatorLayout.showSnackbar(
                                    languageJsonObject.getTranslation(
                                        message
                                    )
                                )
                            }
                        }

                        SNACK_BAR_ACTION_KEEP_EMPTY -> {
                            if (it[0].show) {
                                val replaceMfgLine = arguments?.getString("replaceMfgLine") ?: ""
                                val replaceStation = arguments?.getString("replaceStation") ?: ""
                                val message =
                                    "$replaceMfgLine has been removed to station $replaceStation"
                                binding.coordinatorLayout.showSnackbar(
                                    languageJsonObject.getTranslation(
                                        message
                                    )
                                )
                            }
                        }

                        SNACK_BAR_ACTION_MOVE_MACHINE -> {
                            if (it[0].show) {
                                val replaceMfgLine = arguments?.getString("replaceMfgLine") ?: ""
                                val replaceStation = arguments?.getString("replaceStation") ?: ""
                                val isFromMoveMC = arguments?.getBoolean("isFromMoveMC") ?: false
                                val msg =
                                    if (isFromMoveMC) "has been moved to" else "has been checked in to"
                                val message =
                                    "$replaceMfgLine ${languageJsonObject.getTranslation(msg)} $replaceStation"

                                binding.coordinatorLayout.showSnackbar(
                                    languageJsonObject.getTranslation(
                                        message
                                    )
                                )
                            }
                        }
                    }

                }
                machineViewModel.finishInsertToSnackBarActionDatabase()
            }
        })

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenerToRefresh()
        setupListener()
        handleStatisticCountObserve()

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
            R.id.updateNewApk -> downloadNewApk(requireActivity(), APK_LINK, NEW_VERSION)
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
                            binding.reportedTicketsCountTextViewMC.text = count.Reported?.toString()
                            binding.reopenedTicketsCountTextViewMC.text = count.Reopen?.toString()
                            binding.inRepairTicketsCountTextViewMC.text = count.InRepair.toString()
                            binding.repairedTicketsCountTextViewMC.text = count.Repaired.toString()
                            binding.maintenanceTicketsCountTextViewMC.text =
                                count.Maintenance.toString()
                            binding.changeOverCountTextViewMC.text = count.CORequest.toString()
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

    private fun setupListener() {

        binding.btnMCChangeOver.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_ChangeOverFragment)
        }

        binding.lineFilterMC.setOnClickListener {
            showFilterDialog()
        }

        binding.btnMCReportedTickets.setOnClickListener {
            navigateToReportedTickets(TicketType.REPORTED)
        }
        binding.btnMCReopenedTickets.setOnClickListener {
            navigateToReportedTickets(TicketType.REOPEN)
        }

        binding.btnMCInRepairTickets.setOnClickListener {
            navigateToInRepairTickets()
        }

        binding.btnMCRepairedTickets.setOnClickListener {
            navigateToRepairedTickets()
        }

        binding.btnMCLineOverview.setOnClickListener {
            navigateToLineStatus()
        }

        binding.btnMCReplaceMachine.setOnClickListener {
            nfcViewModel.setNFCAction(NFCAction.REPLACE_MACHINE)
        }

        binding.btnMCSetupLine.setOnClickListener {
            navigateToSetupLine()
        }

        binding.btnMCMaintenance.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_MaintFragment)
        }
    }

    private var filterDialog: FilterLineAndAreaDialog? = null
    private fun showFilterDialog() {
        if (filterDialog == null) filterDialog =
            FilterLineAndAreaDialog()
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

    private fun successFindMachine(machine: Machine) {
        val mfgLineId = machine.mfgLineId ?: 0
        MachineUtil.machineNo = machine.machine
        MachineUtil.machineArea = machine.area
        MachineUtil.machineLocation =
            if (machine.area.lowercase(Locale.getDefault()).contains("prod")) {
                "${machine.mfgLine} - ${machine.station}"
            } else {
                machine.area
            }
        MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

        if (machine.station.isNotBlank()) {
            navigateToScanDetails(
                mfgLineId,
                machine.mfgLine ?: "",
                machine.station,
                machine.machine,
                machine.id
            )
        } else {
            binding.coordinatorLayout.showSnackbar(
                languageJsonObject.getTranslation("Machine is not in Production Line"),
                languageJsonObject.getTranslation("OKAY")
            )
        }

        machineViewModel.machineDetailsByRfidComplete()
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

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun getPopupWindow(): PopupWindow {
        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_mechanic_line_list, null)
        val closeButton = view.findViewById<TextView>(R.id.closePopupMC)
        val labelSelectLinesMC = view.findViewById<TextView>(R.id.labelSelectLinesMC)
        val linesearchEditTextMC = view.findViewById<TextView>(R.id.linesearchEditTextMC)
        val labelShowAllLines = view.findViewById<TextView>(R.id.labelShowAllLines)
        val noResultsTextViewMc = view.findViewById<TextView>(R.id.noResultsTextViewMc)
        val btnMCSelectLine = view.findViewById<TextView>(R.id.btnMCSelectLine)


        val adapter = MechanicLineListAdapter(viewModel, lineViewModel)

        var recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMC)

        val binding = PopupMechanicLineListBinding.inflate(inflater)


        // Start translation
        with(languageJsonObject) {
            labelSelectLinesMC.post {
                labelSelectLinesMC.text = getTranslation(labelSelectLinesMC.text.toString())
            }

            linesearchEditTextMC.post {
                linesearchEditTextMC.hint = getTranslation(linesearchEditTextMC.hint.toString())
            }

            labelShowAllLines.post {
                labelShowAllLines.text = getTranslation(labelShowAllLines.text.toString())
            }

            noResultsTextViewMc.post {
                noResultsTextViewMc.text = getTranslation(noResultsTextViewMc.text.toString())
            }

            btnMCSelectLine.post {
                btnMCSelectLine.text = getTranslation(btnMCSelectLine.text.toString())
            }


        }
        // End translation

        val selectButton = view.findViewById<Button>(R.id.btnMCSelectLine)
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

            adapter.data = lineViewModel.mfgLinesTemp
            LineUtil.uncheckedLines.clear()

        }

        lineViewModel.mfgLinesAssignedByArea.observe(viewLifecycleOwner, Observer { mfgLines ->

            lineViewModel.selectedMfgLinesTemp = mfgLines.toMutableList()

            if (adapter.dataFull.isEmpty()) {
                adapter.dataFull = mfgLines
                lineViewModel.mfgLinesTemp = mfgLines.toMutableList()
            }

            adapter.data = mfgLines.sortedBy { it.seq }.toMutableList()

            val selectedLines = lineViewModel.mfgLinesTemp.filter { it.checked == true }

            selectButton.apply {

                if (searchCount == 0) {
                    if (selectedLines.isNotEmpty()) {
                        selectButton.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.button)
                    } else {
                        selectButton.setBackgroundColor(Color.GRAY)
                    }

                    //isEnabled = selectedLines.isNotEmpty()
                }
            }

            closeButton.visibility = if (selectedLines.isEmpty()) View.INVISIBLE else View.VISIBLE

        })

        lineViewModel.mfgLinesAssignedByArea.observe(viewLifecycleOwner, Observer { mfgLines ->
            if (mfgLines != null) {

                /*selectButton.isEnabled =
                    if (lineViewModel.mfgLinesTemp.none { it.checked == true }) {
                        selectButton.setBackgroundColor(Color.GRAY)
                        false
                    } else {
                        selectButton.background = resources.getDrawable(R.drawable.button, null)
                        true
                    }*/
            }
        })

        val searchField = view.findViewById<EditText>(R.id.linesearchEditTextMC)

        closeButton.setOnClickListener {
            viewModel.setEventLineListSearchResultNotFoundToFalse()
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
            adapter.filter.filter("")
            searchField.setText("")
        }

        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter

        viewModel.eventLineListSearchResultNotFound.observe(viewLifecycleOwner, Observer {
            val noResultTextView = view.findViewById<TextView>(R.id.noResultsTextViewMc)
            if (it) {
                noResultTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                noResultTextView.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
            }
        })

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(text)

                searching = text?.length!! > 0
                searchCount = text.length
            }

        })


        return PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToSetupLine() {
        val action = MechanicHomeFragmentDirections.actionMechanicHomeFragmentToSetupLineFragment(
            mfgLineStr,
            mfgLineId
        )
        navigate(action)
    }

    private fun navigateToLineStatus() {
        val action = MechanicHomeFragmentDirections.actionMechanicHomeFragmentToLineStatusFragment()
        navigate(action)
    }

    private fun navigateToScanDetails(
        mfgLineId: Long,
        mfgLine: String,
        station: String,
        machine: String,
        machineId: Long
    ) {
        val action =
            MechanicHomeFragmentDirections.actionMechanicHomeFragmentToReplaceMachineScanDetailsFragment(
                mfgLineId,
                mfgLine,
                station,
                machine,
                machineId
            )
        navigate(action)
    }

    private fun navigateToReportedTickets(ticketType: String) {
        val bundle = bundleOf("ticket_type" to ticketType)
        findNavController().navigate(
            R.id.action_mechanicHomeFragment_to_mechanicReportedTicketsFragment,
            bundle
        )
    }

    private fun navigateToInRepairTickets() {
        val action =
            MechanicHomeFragmentDirections.actionMechanicHomeFragmentToMechanicInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToRepairedTickets() {
        val action =
            MechanicHomeFragmentDirections.actionMechanicHomeFragmentToMechanicRepairedTicketsFragment()
        navigate(action)
    }
}
