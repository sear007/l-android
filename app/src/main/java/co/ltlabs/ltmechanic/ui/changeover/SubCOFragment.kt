package co.ltlabs.ltmechanic.ui.changeover

import androidx.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.FragmentSubChangeOverBinding
import co.ltlabs.ltmechanic.repository.paging.PagingLoadingAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.util.getTranslation
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubCOFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var coAdapter: COAdapter
    private var selectedLinesIdStr = mutableListOf<String>()
    private var selectedLinesStr = mutableListOf<String>()
    private val format: SimpleDateFormat by lazy {
        SimpleDateFormat(
            "H:m, EEEE, dd MMM yyyy",
            Locale.getDefault()
        )
    }
    private lateinit var binding: FragmentSubChangeOverBinding
    private var type = COStatusType.NEW
    private val viewModel: COViewModel by viewModels { providerFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(EXTRA_TYPE, type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubChangeOverBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        updatedDateFetchData()
    }

    private fun updatedDateFetchData() {
        binding.tvUpdated.text =
            String.format(
                "%s %s",
                languageJsonObject.getTranslation("Updated at"),
                format.format(Date(System.currentTimeMillis()))
            )
    }

    fun refresh() {
        updatedDateFetchData()
        lifecycleScope.launchWhenCreated {
            viewModel.getCOList(type, selectedLinesIdStr.joinToString(",")).collectLatest {
                coAdapter.submitData(it)
            }
        }
    }

    private fun setupList() {
        binding.rvChangeOver.adapter = coAdapter.withLoadStateHeaderAndFooter(
            header = PagingLoadingAdapter { coAdapter.retry() },
            footer = PagingLoadingAdapter { coAdapter.retry() }
        )

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner) { mfgLines ->
            mfgLines.filter { it.checked ?: false }.forEach {
                selectedLinesStr.add(it.mfgLine)
                selectedLinesIdStr.add(it.mfgLineId.toString())

            }

            lifecycleScope.launchWhenCreated {
                if (coAdapter.itemCount == 0) {
                    viewModel.getCOList(type, selectedLinesIdStr.joinToString(",")).collectLatest {
                        coAdapter.submitData(it)
                    }
                }
            }
        }



        lifecycleScope.launchWhenCreated {
            coAdapter.loadStateFlow.collectLatest { loadState ->
                binding.progressBar.isVisible =
                    loadState.source.refresh is LoadState.Loading
            }
        }

        coAdapter.setOnItemClick { pos ->
            val item = coAdapter.snapshot()[pos] ?: return@setOnItemClick
            val direct = COFragmentDirections.actionChangeOverFragmentToCoDetailFragment(
                item.coRequestNo ?: ""
            )
            findNavController().navigate(direct)
        }
    }

    companion object {
        private const val EXTRA_TYPE = "EXTRA_TYPE"
        fun newInstance(type: String) = SubCOFragment().apply {
            arguments = bundleOf(EXTRA_TYPE to type)
        }
    }
}