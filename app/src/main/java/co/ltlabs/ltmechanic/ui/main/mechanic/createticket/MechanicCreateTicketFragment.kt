package co.ltlabs.ltmechanic.ui.main.mechanic.createticket

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentMechanicCreateTicketBinding
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderMachineProblemsAdapter
import co.ltlabs.ltmechanic.ui.adapter.MechanicMachineProblemsAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicCreateTicketViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "MCreateTicketFragment";

class MechanicCreateTicketFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MechanicCreateTicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicCreateTicketViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val args: MechanicCreateTicketFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    private var selectedProblemTypeId = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMechanicCreateTicketBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.machineNoTextView.text = args.machine



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

        ticketViewModel.getMachineProblems(args.machineId)

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    ticketViewModel.getMachineProblems(args.machineId)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        binding.btnCancel.setOnClickListener {
            activity?.onBackPressed()

//            activity?.supportFragmentManager?.popBackStackImmediate()
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
                    when(it.size) {

                        3 -> {
                            binding.commonProblem1TextView.text = languageJsonObject.getTranslation(it[0].desc1)
                            binding.commonProblem1TextView.tag = it[0].problemTypeId
                            binding.commonProblem2TextView.text = languageJsonObject.getTranslation(it[1].desc1)
                            binding.commonProblem2TextView.tag = it[1].problemTypeId
                            binding.commonProblem3TextView.text = languageJsonObject.getTranslation(it[2].desc1)
                            binding.commonProblem3TextView.tag = it[2].problemTypeId
                        }

                        2 -> {
                            binding.commonProblem1TextView.text = languageJsonObject.getTranslation(it[0].desc1)
                            binding.commonProblem1TextView.tag = it[0].problemTypeId
                            binding.commonProblem2TextView.text = languageJsonObject.getTranslation(it[1].desc1)
                            binding.commonProblem2TextView.tag = it[1].problemTypeId
//                            binding.commonProblem3TextView.visibility = View.GONE
                        }

                        else -> {
                            binding.commonProblem1TextView.text = languageJsonObject.getTranslation(it[0].desc1)
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

        ticketViewModel.latestProblems.observe(viewLifecycleOwner, Observer {latestProblems ->
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
            when(it) {
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

                            Log.d(TAG, "onCreateView: getTranslation(toolBarTitleTextView.text.toString()): ${getTranslation(toolBarTitleTextView.text.toString())}")

                            toolBarTitleTextView.post {
                                toolBarTitleTextView.text =
                                    getTranslation(toolBarTitleTextView.text.toString())
                            }

                            placeTextView2.post {
                                placeTextView2.text =
                                    getTranslation(placeTextView2.text.toString())
                            }

                            placeTextView7.post {
                                placeTextView7.text =
                                    getTranslation(placeTextView7.text.toString()) + " *"
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

        labelSelectMachineProblem.text =  resources.getString(R.string.select_machine_problem)

        closeButton.setOnClickListener {
            dismissPopup()
        }

        with(languageJsonObject) {
            labelSelectMachineProblem.text = getTranslation(labelSelectMachineProblem.text.toString())
            searchField.hint = getTranslation(searchField.hint.toString())
            selectButton.text = getTranslation(selectButton.text.toString())
            noProblemFound.text = getTranslation(noProblemFound.text.toString())
        }

        val adapter = MechanicMachineProblemsAdapter(viewModel, ticketViewModel)

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

        ticketViewModel.problems.observe(viewLifecycleOwner, Observer {problems ->

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

        searchField.addTextChangedListener (object : TextWatcher {
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

        DimUtil.dimBehind(popupWindow)

//        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
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
        val action = MechanicCreateTicketFragmentDirections
            .actionMechanicCreateTicketFragmentToMechanicCreateTicketAttachFragment(
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

}
