package co.ltlabs.ltmechanic.ui.dialog.movemachine

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.DialogBuildingDestionationBinding
import co.ltlabs.ltmechanic.ui.dialog.BaseBSDialog
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class BuildingDestinationBSDialog : BaseBSDialog<DialogBuildingDestionationBinding>() {

    @Inject
    lateinit var buildingAdapter: BuildingAdapter

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var onOkClicked: ((buildingName: String?, buildingId: Int) -> Unit)? = null

    private val viewModel: MoveMCViewModel by viewModels { providerFactory }
    private var building: String? = null
    private var buildingId: Int = 0

    override fun getLayoutId() = R.layout.dialog_building_destionation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            building = args.getString(EXTRA_BUILDING)
            buildingId = args.getInt(EXTRA_BUILDING_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.jTranslate = languageJsonObject
        setupListener()
        setupListDestination()
    }

    fun setOnOkClicked(clicked: (buildingName: String?, buildingId: Int) -> Unit) = apply {
        this.onOkClicked = clicked
    }

    private fun setupListDestination() {
        viewModel.getBuilding()

        binding.rvDestinationBuilding.adapter = buildingAdapter
        buildingAdapter.setOnItemClicked {
            val item = buildingAdapter.currentList[it]
            building = item.buildingName
            buildingId = item.id ?: 0
        }

        lifecycleScope.launch {
            viewModel.building.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> binding.progressBar.isVisible = true
                    Resource.Status.SUCCESS -> {
                        val list = it.data?.map { item ->
                            item.isChecked = item.id == buildingId
                            item
                        }
                        buildingAdapter.submitList(list)
                        var selectedIndex = 0
                        list?.forEachIndexed { index, buildingItem ->
                            if (buildingItem.isChecked) selectedIndex = index
                        }
                        binding.rvDestinationBuilding.layoutManager?.scrollToPosition(selectedIndex)
                        binding.progressBar.isVisible = false
                        binding.tvNoData.isVisible = list?.isEmpty() ?: false
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListener() {
        binding.apply {
            tvCancel.setOnClickListener {
                dismiss()
            }

            tvOk.setOnClickListener {
                if (buildingId == 0) {
                    Toast.makeText(
                        requireContext(),
                        languageJsonObject.getTranslation("Please select building"),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                onOkClicked?.invoke(building, buildingId)
                dismiss()
            }
        }
    }

    companion object {
        private const val EXTRA_BUILDING = "EXTRA_BUILDING"
        private const val EXTRA_BUILDING_ID = "EXTRA_BUILDING_ID"

        fun newInstance(building: String?, buildingId: Int) = BuildingDestinationBSDialog()
            .apply {
                arguments = bundleOf(EXTRA_BUILDING to building, EXTRA_BUILDING_ID to buildingId)
            }
    }
}