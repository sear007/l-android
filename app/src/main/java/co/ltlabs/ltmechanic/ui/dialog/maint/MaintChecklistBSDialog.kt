package co.ltlabs.ltmechanic.ui.dialog.maint

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.DialogMaintChecklistBinding
import co.ltlabs.ltmechanic.ui.dialog.BaseBSDialog
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class MaintChecklistBSDialog : BaseBSDialog<DialogMaintChecklistBinding>() {

    @Inject
    lateinit var maintChecklistAdapter: MaintChecklistAdapter

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintenanceViewModel by viewModels { providerFactory }

    private var onOkClicked: ((name: String?, id: Long?) -> Unit)? = null

    private var checklistName: String? = null
    private var checklistId: Long? = 0

    override fun getLayoutId() = R.layout.dialog_maint_checklist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            checklistName = args.getString(EXTRA_CHECKLIST)
            checklistId = args.getLong(EXTRA_CHECKLIST_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.jTranslate = languageJsonObject
        setupListener()
        setupList()
    }

    fun setOnOkClicked(clicked: (name: String?, id: Long?) -> Unit) = apply {
        this.onOkClicked = clicked
    }

    private fun setupList() {
        viewModel.getChecklist()

        binding.rv.adapter = maintChecklistAdapter
        maintChecklistAdapter.setOnItemClicked {
            val item = maintChecklistAdapter.currentList[it]
            checklistName = item.name
            checklistId = item.id
        }

        lifecycleScope.launch {
            viewModel.data.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> binding.progressBar.isVisible = true
                    Resource.Status.SUCCESS -> {
                        val list = it.data?.map { item ->
                            item.checked = item.id == checklistId
                            item
                        }
                        maintChecklistAdapter.submitList(list)
                        var selectedIndex = 0
                        list?.forEachIndexed { index, buildingItem ->
                            if (buildingItem.checked) selectedIndex = index
                        }
                        binding.rv.layoutManager?.scrollToPosition(selectedIndex)
                        binding.progressBar.isVisible = false
                        binding.tvNoData.isVisible = list?.isEmpty() ?: false
                    }

                    else -> binding.progressBar.isVisible = false
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
                if (checklistId == 0L) {
                    Toast.makeText(
                        requireContext(),
                        languageJsonObject.getTranslation("Please select checklist"),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
                onOkClicked?.invoke(checklistName, checklistId)
                dismiss()
            }
        }
    }

    companion object {
        private const val EXTRA_CHECKLIST = "EXTRA_CHECKLIST"
        private const val EXTRA_CHECKLIST_ID = "EXTRA_CHECKLIST_ID"

        fun newInstance(name: String?, id: Long?) = MaintChecklistBSDialog()
            .apply {
                arguments = bundleOf(
                    EXTRA_CHECKLIST to name,
                    EXTRA_CHECKLIST_ID to id
                )
            }
    }
}