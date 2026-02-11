package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentMaintAddChecklistBinding
import co.ltlabs.ltmechanic.ui.dialog.maint.MaintChecklistBSDialog
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class MaintAddCheckListFragment : BaseFragment() {

    @Inject
    lateinit var loadingIndicator: LoadingIndicator

    private lateinit var binding: FragmentMaintAddChecklistBinding
    private val args: MaintAddCheckListFragmentArgs by navArgs()
    private var checklistName: String? = null
    private var checklistId: Long? = 0

    private val viewModel: MaintenanceViewModel by viewModels { providerFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMaintAddChecklistBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBarTitleTextView.text = args.ticketNo
        binding.remarksEditText.setText(args.remark)
        setupListener()
        submitChecklist()
    }

    private fun submitChecklist() {
        lifecycleScope.launchWhenCreated {
            viewModel.addCheckList.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> loadingIndicator.show(
                        requireContext(),
                        event = LoadingIndicator.SUBMIT_CHECKLIST
                    )
                    Resource.Status.SUCCESS -> {
                        delay(1000)
                        loadingIndicator.dismiss()
                        navigateToChecklist()
                    }
                    else -> {
                        loadingIndicator.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupListener() {
        binding.btnComplete.setOnClickListener {
            Toast.makeText(requireContext(), binding.ticketNo2.text, Toast.LENGTH_SHORT).show()
        }
        binding.btnSave.setOnClickListener {
            Toast.makeText(requireContext(), binding.ticketNo2.text, Toast.LENGTH_SHORT).show()
        }
        binding.tvAddChecklist.setOnClickListener {
            showChecklistDialog()
        }
    }

    private var checklistDialog: MaintChecklistBSDialog? = null
    private fun showChecklistDialog() {
        if (checklistDialog == null)
            checklistDialog = MaintChecklistBSDialog.newInstance(
                checklistName, checklistId
            )

        if (checklistDialog?.isAdded == false) {
            checklistDialog?.show(childFragmentManager, checklistDialog?.tag)
            checklistDialog?.onDismissListener {
                checklistDialog = null
            }
            checklistDialog?.setOnOkClicked { name, id ->
                checklistName = name
                checklistId = id
                viewModel.attachChecklistToMachine(args.ticketId, checklistId ?: 0)
            }
        }
    }

    private fun navigateToChecklist() {
        val bundle = bundleOf(
            "ticketId" to args.ticketId,
            "ticketNo" to args.ticketNo,
            "ticketStatus" to args.ticketStatus,
            "remark" to binding.remarksEditText.text.toString().ifBlank { null }
        )
        findNavController().navigate(R.id.action_global_to_maintenanceChecklistFragment, bundle)
    }

}