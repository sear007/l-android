package co.ltlabs.ltmechanic.ui.dialog.movemachine

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.DialogAreaDestionationBinding
import co.ltlabs.ltmechanic.ui.dialog.BaseBSDialog
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class AreaDestinationBSDialog : BaseBSDialog<DialogAreaDestionationBinding>() {

    @Inject
    lateinit var areaAdapter: AreaAdapter

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var onOkClicked: ((areaName: String?, areaId: Int) -> Unit)? = null

    private val viewModel: MoveMCViewModel by viewModels { providerFactory }
    private var area: String? = null
    private var areaId: Int = 0
    private var buildingId: Int = 0
    private var title: String = "Remove Machine to"

    override fun getLayoutId() = R.layout.dialog_area_destionation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            area = args.getString(EXTRA_AREA)
            areaId = args.getInt(EXTRA_AREA_ID)
            buildingId = args.getInt(EXTRA_BUILDING_ID)
            title = args.getString(EXTRA_TITLE, title)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.jTranslate = languageJsonObject
        setupListener()
        setupListDestination()
        binding.tvTitle.text = title
    }

    fun setOnOkClicked(clicked: (areaName: String?, areaId: Int) -> Unit) = apply {
        this.onOkClicked = clicked
    }

    private fun setupListDestination() {
        viewModel.getAreaByBuilding(buildingId)

        binding.rvDestinationArea.adapter = areaAdapter
        areaAdapter.setOnItemClicked {
            val item = areaAdapter.currentList[it]
            area = item.name
            areaId = item.id ?: 0
        }

        lifecycleScope.launch {
            viewModel.area.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> binding.progressBar.isVisible = true
                    Resource.Status.SUCCESS -> {
                        val list = it.data?.map { item ->
                            item.isSelected = item.id == areaId
                            item
                        }
                        areaAdapter.submitList(list)
                        var selectedIndex = 0
                        list?.forEachIndexed { index, buildingItem ->
                            if (buildingItem.isSelected) selectedIndex = index
                        }
                        binding.rvDestinationArea.layoutManager?.scrollToPosition(selectedIndex)
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
                if (areaId == 0) {
                    Toast.makeText(
                        requireContext(),
                        languageJsonObject.getTranslation("Please select area"),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
                onOkClicked?.invoke(area, areaId)
                dismiss()
            }
        }
    }

    companion object {
        private const val EXTRA_AREA = "EXTRA_AREA"
        private const val EXTRA_AREA_ID = "EXTRA_AREA_ID"
        private const val EXTRA_BUILDING_ID = "EXTRA_BUILDING_ID"
        private const val EXTRA_TITLE = "EXTRA_TITLE"

        fun newInstance(
            area: String?,
            areaId: Int,
            buildingId: Int,
            title: String = "Remove Machine to"
        ) = AreaDestinationBSDialog()
            .apply {
                arguments = bundleOf(
                    EXTRA_AREA to area,
                    EXTRA_AREA_ID to areaId,
                    EXTRA_BUILDING_ID to buildingId,
                    EXTRA_TITLE to title
                )
            }
    }
}