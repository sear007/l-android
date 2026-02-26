package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusStationsBinding
import co.ltlabs.ltmechanic.ui.adapter.LineStatusStationListAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusStationsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.StationViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "LSStationsFragment";

class LineStatusStationsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusStationsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusStationsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private val stationViewModel: StationViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(StationViewModel::class.java)
    }

    private val args: LineStatusStationsFragmentArgs by navArgs()

    private lateinit var progressBar: ProgressBar

    var addMachineSuccess = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLineStatusStationsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        addMachineSuccess = args.addMachineSuccess

        progressBar = binding.progressBar

        stationViewModel.getMachinesInStation(args.mfgLineId)

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    stationViewModel.getMachinesInStation(args.mfgLineId)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        val adapter = LineStatusStationListAdapter(viewModel)

        binding.toolBarTitleTextView.text =
            getString(R.string.line_status_line_count, args.mfgLine, "0")

        stationViewModel.machinesInStation.observe(
            viewLifecycleOwner,
            Observer { machinesInStation ->

                val machinesCount = machinesInStation?.size?.toString() ?: "0"

                binding.toolBarTitleTextView.text =
                    getString(R.string.line_status_line_count, args.mfgLine, machinesCount)

                if (machinesInStation != null) {
                    adapter.data = machinesInStation
                    viewModel.stationsTemp = machinesInStation
                }

            })

        machineViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {

            if (it != null && it.isNotEmpty()) {

                when (it[0].action) {

                    SNACK_BAR_ACTION_ADD_MACHINE -> {
                        if (it[0].show) {
                            binding.coordinatorLayout.showSnackbar(
//                                StrUtil.replaceStr(
                                languageJsonObject
                                    .getTranslation(
                                        "${languageJsonObject.getTranslation("Station")} ${args.addMachineMachine} ${
                                            languageJsonObject.getTranslation(
                                                "has been added"
                                            )
                                        }"
                                    )
//                                ).format(args.addMachineStation, args.addMachineMachine)
                            )

                        }
                    }

                    SNACK_BAR_ACTION_INSERT -> {
                        if (it[0].show) {
                            val message = languageJsonObject.getTranslation(
                                "${args.addMachineMachine} ${
                                    languageJsonObject.getTranslation("has been inserted to")
                                } ${args.addMachineStation}"
                            )
                            binding.coordinatorLayout.showSnackbar(message)

                        }
                    }

                    SNACK_BAR_ACTION_REMOVE_MACHINE -> {
                        if (it[0].show) {
                            val message = if (!args.isRemoveMachineFromLineStationDetails) {
                                languageJsonObject.getTranslation(
                                    "${args.addMachineMachine} ${
                                        languageJsonObject.getTranslation(
                                            "has been removed to station"
                                        )
                                    } ${args.addMachineStation}"
                                )
                            } else {
                                languageJsonObject.getTranslation(
                                    "${args.addMachineMachine} ${
                                        languageJsonObject.getTranslation(
                                            "has been removed to "
                                        )
                                    } ${args.addMachineStation}"
                                )
                            }

                            binding.coordinatorLayout.showSnackbar(message)

                        }
                    }

                    SNACK_BAR_ACTION_REPLACE_MACHINE -> {
                        if (it[0].show) {
//                            val message = "Line ${args.addMachineMachine} ${args.addMachineStation} has been replaced"
                            val message =
                                "${languageJsonObject.getTranslation("Station")} ${args.addMachineStation} ${
                                    languageJsonObject.getTranslation("has been replaced")
                                }"

                            binding.coordinatorLayout.showSnackbar(message)
                        }
                    }

                    SNACK_BAR_ACTION_KEEP_EMPTY_INSERT -> {
                        if (it[0].show) {
//                            val message = "Line ${args.addMachineMachine} ${args.addMachineStation} has been replaced"
                            val message =
                                languageJsonObject.getTranslation("Empty station has been inserted")

                            binding.coordinatorLayout.showSnackbar(message)
                        }
                    }

                    SNACK_BAR_ACTION_KEEP_EMPTY -> {
                        if (it[0].show) {
                            val replaceMfgLine = arguments?.getString("replaceMfgLine") ?: ""
                            val replaceStation = arguments?.getString("replaceStation") ?: ""
//                                val message = "Line ${args.replaceMfgLine} ${args.replaceStation} has been emptied"
                            val message = StrUtil.replaceStr(
                                languageJsonObject.getTranslation("[] has been removed to station []")
                                    .format(args.addMachineMachine, args.addMachineStation)
                            )
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    message
                                )
                            )
                        }
                    }
                }

                machineViewModel.finishInsertToSnackBarActionDatabase()
            }

        })

        lineViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {

            if (it != null && it.isNotEmpty()) {

                when (it[0].action) {

                    SNACK_BAR_ACTION_ADD_MACHINE -> {
                        if (it[0].show) {
                            val snackbar = Snackbar.make(
                                binding.coordinatorLayout,
                                "${args.addMachineStation} ${args.addMachineMachine} ${
                                    languageJsonObject.getTranslation(
                                        "has been added"
                                    )
                                }",
                                Snackbar.LENGTH_LONG
                            )

                            snackbar.setAction(languageJsonObject.getTranslation("OKAY")) {
                                snackbar.dismiss()
                            }

                            snackbar.show()

                        }
                    }

                    SNACK_BAR_ACTION_INSERT -> {
                        if (it[0].show) {
                            val message = StrUtil.replaceStr(
                                "Station [] has been added"
                            ).format(args.addMachineMachine)
                            val snackbar = Snackbar.make(
                                binding.coordinatorLayout,
                                message,
                                Snackbar.LENGTH_LONG
                            )

                            snackbar.setAction(languageJsonObject.getTranslation("OKAY")) {
                                snackbar.dismiss()
                            }

                            snackbar.show()

                        }
                    }
                }

                lineViewModel.finishInsertToSnackBarActionDatabase()
            }

        })

        viewModel.navigateToStationDetails.observe(
            viewLifecycleOwner,
            Observer { machineInStation ->

                if (machineInStation != null) {

                    Log.d(
                        TAG,
                        "onCreateView: viewModel.stationsTemp size: ${viewModel.stationsTemp.size}"
                    )

                    val nextStation = "${machineInStation.station}-A"

                    var showNextButton = if (machineInStation.station.contains("-A")) {
                        false
                    } else !viewModel.stationsTemp.any { nextStation == it.station }

//                val showNextButton = !viewModel.stationsTemp.any { nextStation == it.station }

//                viewModel.stationsTemp.forEach {
//                    Log.d(TAG, "onCreateView: nextStation == it.station: ${nextStation == it.station}")
//                }

                    Log.d(TAG, "onCreateView: showNextButton: $showNextButton")

                    val machine = if (machineInStation.empty) "" else machineInStation.machine
                    navigateToStationDetails(
                        args.mfgLineId,
                        args.mfgLine,
                        machine,
                        machineInStation.rfid,
                        machineInStation.subType,
                        machineInStation.id,
                        machineInStation.station,
                        showNextButton,
                        viewModel.endLineStation,
                        machineInStation.building,
                        machineInStation.buildingId
                    )

                    viewModel.setNavigateToStationDetailsComplete()
                }
            })

        stationViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    showProgressBar(false)
                }
            }
        })

        binding.recyclerView.adapter = adapter

        return binding.root
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

    private fun navigateToStationDetails(
        mfgLineId: Long,
        mfgLine: String,
        machine: String,
        rfid: String,
        subType: String,
        machineId: Long,
        station: String,
        showNextButton: Boolean,
        endStation: Boolean,
        building: String?,
        buildingId: Int?
    ) {
        val action = LineStatusStationsFragmentDirections
            .actionLineStatusStationsFragmentToLineStatusStationDetailsFragment(
                mfgLineId,
                mfgLine,
                machine,
                rfid,
                subType,
                machineId,
                station,
                showNextButton,
                endStation,
                building,
                buildingId ?: 0
            )
        navigate(action)
    }

}
