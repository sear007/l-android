package co.ltlabs.ltmechanic.ui.main.mechanic.setupline

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentSetupLinePlacesBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.ui.adapter.SetupLineMachinePlacesListAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLinePlacesViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.StationViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "SetupLinePlacesFragment";

/**
 * A simple [Fragment] subclass.
 */
class SetupLinePlacesFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var setupLineAdapter: SetupLineMachinePlacesListAdapter

    val viewModel: SetupLinePlacesViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupLinePlacesViewModel::class.java)
    }

    private val stationViewModel: StationViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(StationViewModel::class.java)
    }

    private val args: SetupLinePlacesFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var selectedMachineStation = MachineInStation(0, "", "", "", "")
    private lateinit var binding: FragmentSetupLinePlacesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupLinePlacesBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        var connectedCount = 1
        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    if (connectedCount == 1) {
                        stationViewModel.getMachinesInStation(
                            args.selectedLineId,
                            module = "setup_line"
                        )
                    }
                    connectedCount++
                    ConnectionUtil.setInternetConnected(false)
                } else {
                    connectedCount = 1
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        setupListLine()
        setupListener()
        subscribeLiveData()
    }

    private fun setup() {
        binding.viewModel = viewModel
        binding.jTranslate = languageJsonObject
        progressBar = binding.progressBar
        binding.toolBarTitleTextView.text = args.selectedLine
        stationViewModel.getMachinesInStation(args.selectedLineId, module = "setup_line")
    }

    private fun setupListener() {
        binding.tvCheckInNewMachineTextView.text = getString(
            R.string.check_in_new_machine,
            languageJsonObject.getTranslation("CHECK-IN NEW MACHINE")
        )
        binding.tvCheckInNewMachineTextView.setOnClickListener {
            try {
                LineUtil.fromLinePlaces = true
                val machines = stationViewModel.machinesInStation.value ?: return@setOnClickListener
                val nextStation = machines[machines.size - 1]
                    .station.replace("-A", "")
                    .toInt() + 1
                val station = if (nextStation > 9) "$nextStation" else "0$nextStation"
                navigateToScanMachine(
                    args.selectedLine,
                    station,
                    args.selectedLineId,
                    origin = "add_new_machine"
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
            }
        }

        binding.btnEditLineSetup.setOnClickListener {
            if (selectedMachineStation.machine.isBlank()) {
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Select Station/Place first"))
            }

            if (selectedMachineStation.machine.isNotBlank()) {
                val existingMachine = selectedMachineStation.empty
                selectedMachineStation.machine =
                    if (!selectedMachineStation.empty) selectedMachineStation.machine else ""

                val nextStation = "${selectedMachineStation.station}-A"

                var showNextButton = if (selectedMachineStation.station.contains("-A")) {
                    false
                } else !stationViewModel.stationsTemp.any { nextStation == it.station }
                if (selectedMachineStation.machine.isNotBlank()) {
                    navigateToPlaceDetails(
                        existingMachine,
                        selectedMachineStation.id,
                        selectedMachineStation.machine,
                        selectedMachineStation.station,
                        selectedMachineStation.rfid,
                        args.selectedLine,
                        args.selectedLineId,
                        selectedMachineStation.subType,
                        showNextButton
                    )
                } else {
                    navigateToScanMachine(
                        args.selectedLine,
                        selectedMachineStation.station,
                        args.selectedLineId
                    )
                }
            }
            viewModel.setSelectedMachineStationComplete()
        }

        binding.btnConfirmLineSetup.setOnClickListener {
            navigateToSetupLine()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToSetupLine()
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupListLine() {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = setupLineAdapter

        setupLineAdapter.setOnItemClickListener {
            val item = setupLineAdapter.currentList[it]
            viewModel.setSelectedMachineStation(item)
            setupLineAdapter.notifyDataSetChanged()
        }
    }

    private fun subscribeLiveData() {
        stationViewModel.machinesInStation.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {
                setupLineAdapter.submitList(machine)
            }
        })

        viewModel.selectedMachineStation.observe(viewLifecycleOwner, Observer {
            selectedMachineStation = it ?: MachineInStation(0, "", "", "", "")
            val bg = if (it != null) {
                R.drawable.button
            } else {
                R.drawable.ic_line_setup_edit_button
            }
            binding.btnEditLineSetup.setBackgroundResource(bg)

        })

        viewModel.navigateToScanMachine.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                navigateToScanMachine(
                    args.selectedLine,
                    it.station,
                    args.selectedLineId,
                    origin = "add_new_machine"
                )

                viewModel.navigateToScanMachineComplete()
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


    private fun navigateToSetupLine() {
        val action =
            SetupLinePlacesFragmentDirections.actionSetupLinePlacesFragmentToSetupLineFragment(
                args.selectedLine,
                args.selectedLineId
            )
        navigate(action)
    }

    private fun navigateToPlaceDetails(
        existingMachine: Boolean,
        machineID: Long,
        machine: String,
        station: String,
        rfid: String,
        mfgLine: String,
        mfgLineId: Long,
        subType: String,
        showNextButton: Boolean
    ) {
        val action =
            SetupLinePlacesFragmentDirections.actionSetupLinePlacesFragmentToSetupLineMachineDetailsWithScanFragment(
                existingMachine,
                machineID,
                machine,
                station,
                rfid,
                mfgLine,
                mfgLineId,
                subType,
                showNextButton
            )
        navigate(action)
    }

    private fun navigateToScanMachine(
        mfgLine: String,
        station: String,
        mfgLineId: Long,
        origin: String = "linePlaces"
    ) {
        val action =
            SetupLinePlacesFragmentDirections.actionSetupLinePlacesFragmentToSetupLineScanMachineFragment(
                mfgLine,
                station,
                mfgLineId,
                origin
            )
        navigate(action)
    }

}
