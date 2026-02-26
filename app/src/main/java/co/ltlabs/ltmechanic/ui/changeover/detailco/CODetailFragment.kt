package co.ltlabs.ltmechanic.ui.changeover.detailco

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.databinding.DialogCloseCoTicketBinding
import co.ltlabs.ltmechanic.databinding.FragmentCoDetailBinding
import co.ltlabs.ltmechanic.domain.changeover.CORequest
import co.ltlabs.ltmechanic.ui.changeover.COViewModel
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.getTranslation
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CODetailFragment : BaseFragment() {

    @Inject
    lateinit var operatorAdapter: OperatorAdapter

    @Inject
    lateinit var langJson: JSONObject

    private var coRequest: CORequest? = null
    private val args: CODetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentCoDetailBinding

    private val coViewModel: COViewModel by activityViewModels { providerFactory }

    private val viewModel: CODetailViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(CODetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBarTitleTextView.title = args.coNo
        setupList()
        setupDetail()

        coViewModel.reloadCODetail.observe(viewLifecycleOwner) {
            if (it) viewModel.getCODetail(args.coNo, true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_ready) {
            showCloseCOTicketDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDetail() {
        viewModel.getCODetail(args.coNo)
        lifecycleScope.launchWhenCreated {
            viewModel.coDetail.collectLatest {
                binding.isLoading = it.status == Resource.Status.LOADING
                binding.isSuccess = it.status == Resource.Status.SUCCESS
                binding.isError = it.status == Resource.Status.ERROR

                if (it.status == Resource.Status.SUCCESS) {
                    val detail = it.data ?: return@collectLatest
                    coRequest = detail
                    val coFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val updateFormat = SimpleDateFormat("yyyy-MM-dd h:m", Locale.getDefault())
                    with(binding) {
                        tvDate.text = String.format(
                            "%s: %s",
                            langJson.getTranslation("CO Date"),
                            coFormat.format(detail.coRequestDt ?: Date())
                        )
                        tvStyle.text = String.format(
                            "%s: %s",
                            langJson.getTranslation("Style"),
                            detail.style
                        )
                        tvLine.text = String.format(
                            "%s: %s",
                            langJson.getTranslation("Line"),
                            detail.mfgLine
                        )
                        tvSummary.text = langJson.getTranslation(tvSummary.text.toString()).replace("\\n","\n")
                        tvUpdated.text = updateFormat.format(detail.updatedDt ?: Date())
                        tvMachineCount.text = (detail.mcQty ?: 0).toString()
                        tvCriticalCount.text = (detail.criticalMcQty ?: 0).toString()
                        tvProductType.text =
                            String.format(
                                "%s: %s",
                                langJson.getTranslation("CO Type"),
                                detail.type
                            )
                        tvStatus.text = langJson.getTranslation("${detail.status?.status}")
                        tvStatus.setTextColor(Color.parseColor("${detail.status?.colorCode}"))

                        operatorAdapter.submitList(detail.items)

                        val isReady =
                            (UserType.convertToType(AuthUtil.role) is UserType.LineLeader && detail.status is COStatusType.Ready)
                        setHasOptionsMenu(isReady)
                    }
                }
            }
        }
    }

    private fun setupList() {
        binding.rvChangeOver.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.rvChangeOver.adapter = operatorAdapter

        operatorAdapter.setOnItemClick { parentPos, childPos ->
            val item = operatorAdapter.currentList[parentPos]
            val operationStatus = item.operations?.get(childPos)

            val direction = if (operationStatus?.status == null || operationStatus.status is COStatusType.New) {
                CODetailFragmentDirections.actionCoDetailFragmentToPrepareCOFragment(
                    item.station.toString(),
                    item.operations?.get(childPos),
                    coRequest?.checkList?.toTypedArray()
                )
            } else {
                CODetailFragmentDirections.actionCoDetailFragmentToReadyCOFragment(
                    item.station.toString(),
                    item.operations[childPos],
                    coRequest?.checkList?.toTypedArray(),
                    operationStatus.status
                )
            }
            findNavController().navigate(direction)
        }
    }

    private fun showCloseCOTicketDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view = DialogCloseCoTicketBinding.inflate(LayoutInflater.from(requireContext()))
        builder.setView(view.root)
        view.tvTitle.text = "${langJson.getTranslation("Close Changeover Ticket")}?"

        builder.setPositiveButton(langJson.getTranslation("OK")) { dialog, _ ->
            dialog.dismiss()
            lifecycleScope.launchWhenCreated {
                viewModel.updateCOStatus(coRequest?.id ?: 0, COStatusType.CLOSED_CODE)
                    .collectLatest {
                        when (it.status) {
                            Resource.Status.SUCCESS -> {
                                loading.dismiss()
                                viewModel.getCODetail(args.coNo, true)
                                coViewModel.setReloadCOList()
                            }

                            Resource.Status.LOADING -> {
                                loading.show(requireContext())
                            }

                            Resource.Status.ERROR -> {
                                loading.dismiss()
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
            }
        }

        builder.setNegativeButton(langJson.getTranslation("CANCEL")) { dialog, _ ->
            dialog.dismiss()
        }

        // translate captions
        with(view) {
            tvTitle.text = langJson.getTranslation(tvTitle.text.toString())
            tvLineTag.text = langJson.getTranslation(tvLineTag.text.toString())
            tvStyleTag.text = langJson.getTranslation(tvStyleTag.text.toString())
            tvMachinesTag.text = langJson.getTranslation(tvMachinesTag.text.toString())
            tvCriticalCountTag.text = langJson.getTranslation(tvCriticalCountTag.text.toString())
        }

        // bind data into dialog
        coRequest?.let { data ->
            with(view) {
                tvLine.text = String.format(":\t %s", data.mfgLine)
                tvStyle.text = String.format(":\t %s", data.style)
                tvMachines.text = String.format(":\t %s", data.mcQty?.toString())
                tvCriticalCount.text = String.format(":\t %s", data.criticalMcQty?.toString())
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
}