package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.databinding.FragmentSubMaintenanceBinding
import co.ltlabs.ltmechanic.repository.paging.PagingLoadingAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.mechanic.maintenance.adapter.MaintAdapter
import co.ltlabs.ltmechanic.util.getTranslation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubMaintFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private lateinit var maintAdapter: MaintAdapter

    private var clearFilter: (() -> Unit)? = null
    private val viewModel: MaintViewModel by viewModels { providerFactory }
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }
    private var linesSelected: String? = null
    private var areasSelected: String? = null

    private val format: SimpleDateFormat by lazy {
        SimpleDateFormat(
            "H:m, EEEE, dd MMM yyyy", Locale.getDefault()
        )
    }

    private var type = MaintType.CLOSED

    private lateinit var binding: FragmentSubMaintenanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(EXTRA_TYPE) ?: type
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubMaintenanceBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.jTranslate = languageJsonObject
        maintAdapter = MaintAdapter(languageJsonObject, type)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tv7Days.isVisible = type == MaintType.CLOSED
        updatedDateFetchData()
        setupList()
        binding.tvNoData.isVisible = false
        binding.ivClearFilter.setOnClickListener {
            clearFilter?.invoke()
        }
    }

    fun onClearFilter(item: () -> Unit) = apply {
        this.clearFilter = item
    }

    fun refresh(machine: String? = null) {
        updatedDateFetchData()
        binding.llFilter.isVisible = machine != null
        binding.tvMachineFilter.text = "${languageJsonObject.getTranslation("Filter")}: $machine"
        lifecycleScope.launchWhenCreated {
            viewModel.getMaints(type, machine, linesSelected, areasSelected).collectLatest {
                maintAdapter.submitData(it)
            }
        }
    }

    private fun updatedDateFetchData() {
        binding.tvUpdated.text = String.format(
            "%s %s",
            languageJsonObject.getTranslation("Updated at"),
            format.format(Date(System.currentTimeMillis()))
        )
    }

    private fun setupList() {
        binding.rvMaintenance.adapter =
            maintAdapter.withLoadStateHeaderAndFooter(header = PagingLoadingAdapter { maintAdapter.retry() },
                footer = PagingLoadingAdapter { maintAdapter.retry() })

        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.IO) {
                val lines =
                    viewModel.database.mfgLineDao.getLinesAsync().filter { it.checked }.map {
                            it.mfgLineId.toString()
                        }
                linesSelected = lines.joinToString(",")
            }

            withContext(Dispatchers.IO) {
                val areas = dashboardViewModel.sharedAreasNoLines.map { area ->
                    area.id ?: ""
                }
                areasSelected = areas.joinToString(",")
            }


            if (maintAdapter.itemCount == 0) {
                viewModel.getMaints(
                    type = type, lineSelected = linesSelected, areaSelected = areasSelected
                ).collectLatest {
                    maintAdapter.submitData(it)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            maintAdapter.loadStateFlow.collectLatest { loadState ->
                binding.tvNoData.isVisible =
                    loadState.source.refresh is LoadState.NotLoading && maintAdapter.itemCount == 0
                binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            }
        }

        maintAdapter.setOnItemClick { pos ->
            val item = maintAdapter.snapshot()[pos]
            val bundle = bundleOf(
                "machineId" to (item?.machineId ?: 0).toLong(),
                "ticketId" to (item?.id ?: 0).toLong(),
                "ticketNo" to item?.ticketNo
            )
            findNavController().navigate(
                R.id.action_maintenanceFragment_to_maintenancePreviewFragment, bundle
            )
        }
    }

    companion object {
        private const val EXTRA_TYPE = "EXTRA_TYPE"

        fun newInstance(type: String) = SubMaintFragment().apply {
            arguments = bundleOf(EXTRA_TYPE to type)
        }
    }

}