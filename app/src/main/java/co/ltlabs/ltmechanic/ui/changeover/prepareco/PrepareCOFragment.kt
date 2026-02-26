package co.ltlabs.ltmechanic.ui.changeover.prepareco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.FragmentPrepareCoBinding
import co.ltlabs.ltmechanic.domain.changeover.AttachmentsItem
import co.ltlabs.ltmechanic.domain.changeover.CheckListItem
import co.ltlabs.ltmechanic.ui.changeover.AttImageAdapter
import co.ltlabs.ltmechanic.ui.changeover.COViewModel
import co.ltlabs.ltmechanic.ui.changeover.ViewAttachmentBSDialog
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.getTranslation
import com.bumptech.glide.RequestManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class PrepareCOFragment : BaseFragment() {

    @Inject
    lateinit var langJson: JSONObject

    @Inject
    lateinit var requestManager: RequestManager

    private val viewModel: PrepareCOViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(PrepareCOViewModel::class.java)
    }

    private val coViewModel: COViewModel by activityViewModels { providerFactory }

    private val args: PrepareCOFragmentArgs by navArgs()
    private lateinit var binding: FragmentPrepareCoBinding
    private var viewAttachmentBSDialog: ViewAttachmentBSDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrepareCoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = langJson
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBarTitleTextView.text =
            String.format("%s ${args.station}", langJson.getTranslation("Station"))
        bindDataToViews()
        setupListener()
    }

    private fun bindDataToViews() {
        val data = args.item ?: return
        binding.apply {
            ivCritical.setImageResource(data.isCritical?.imageRes ?: R.drawable.ic_un_critical)
            tvMacSubType.text = String.format("%s: %s", data.macSubType, data.macSubTypeName)
            tvRemarkValue.text = data.remarks ?: "-"
            tvNeedleTypeValue.text = data.needleType ?: "-"
            setupListAttachments(data.attachments)
            setupListCheckList(args.checkList?.toList())
        }
    }

    private fun setupListener() {
        binding.btnStartPrepare.setOnClickListener {
            lifecycleScope.launch {
                val coItemId = args.item?.id ?: return@launch
                val coRequestId = args.item?.coRequestId ?: return@launch
                viewModel.updateCOItem(
                    coItemId,
                    coRequestId
                ).collectLatest {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            loading.show(requireContext())
                        }
                        Resource.Status.ERROR -> {
                            loading.dismiss()
                        }
                        Resource.Status.SUCCESS -> {
                            loading.dismiss()
                            if (it.data != null) {
                                coViewModel.setReloadCODetail()
                                coViewModel.setReloadCOList()
                                val directions =
                                    PrepareCOFragmentDirections.actionFinalCOFragmentToReadyCOFragment(
                                        args.station,
                                        it.data.item,
                                        args.checkList,
                                        COStatusType.InProgress
                                    )
                                findNavController().navigate(directions)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupListAttachments(list: List<AttachmentsItem?>?) {
        binding.apply {
            tvAttachmentEmpty.isVisible = list.isNullOrEmpty()
            rvAttachmentImage.isVisible = !list.isNullOrEmpty()
            rvAttachmentDes.isVisible = !list.isNullOrEmpty()
        }

        list ?: return
        val imageAdapter = AttImageAdapter(requestManager, list)
        binding.rvAttachmentImage.adapter = imageAdapter
        imageAdapter.setItemClick {
            val item = list[it]
            showViewAttachmentDialog(item)
        }

        val desAdapter = PrepareAttDesAdapter(list)
        binding.rvAttachmentDes.adapter = desAdapter
    }

    private fun setupListCheckList(list: List<CheckListItem?>?) {
        binding.apply {
            tvCheckListEmpty.isVisible = list.isNullOrEmpty()
            rvCheckList.isVisible = !list.isNullOrEmpty()
        }
        list?.let {
            val adapter = PrepareCheckListAdapter(it)
            binding.rvCheckList.adapter = adapter
        }
    }

    private fun showViewAttachmentDialog(item: AttachmentsItem?) {
        item ?: return
        if (viewAttachmentBSDialog == null) {
            viewAttachmentBSDialog = ViewAttachmentBSDialog.newInstance(item)

            if (viewAttachmentBSDialog?.isAdded == false)
                viewAttachmentBSDialog?.show(childFragmentManager, viewAttachmentBSDialog?.tag)

            viewAttachmentBSDialog?.onDismissListener {
                viewAttachmentBSDialog = null
            }
        }
    }
}