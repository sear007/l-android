package co.ltlabs.ltmechanic.ui.changeover.readyco

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.DialogUnmatchedMacsubtypeBinding
import co.ltlabs.ltmechanic.databinding.FragmentReadyCoBinding
import co.ltlabs.ltmechanic.domain.changeover.AttachmentsItem
import co.ltlabs.ltmechanic.domain.changeover.CheckListItem
import co.ltlabs.ltmechanic.domain.changeover.OperationItem
import co.ltlabs.ltmechanic.ui.changeover.AttImageAdapter
import co.ltlabs.ltmechanic.ui.changeover.COViewModel
import co.ltlabs.ltmechanic.ui.changeover.ViewAttachmentBSDialog
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import com.bumptech.glide.RequestManager
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReadyCOFragment : BaseFragment() {

    @Inject
    lateinit var langJson: JSONObject

    @Inject
    lateinit var requestManager: RequestManager

    private lateinit var binding: FragmentReadyCoBinding
    private var viewAttachmentBSDialog: ViewAttachmentBSDialog? = null

    private val args: ReadyCOFragmentArgs by navArgs()
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val viewModel: ReadyViewModel by viewModels { providerFactory }
    private val coViewModel: COViewModel by activityViewModels { providerFactory }

    private var machineId: Long? = null
    private var coItem: OperationItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadyCoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = langJson
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTranslation()
        bindDataToViews()
        setupListener()
        setStatusInformation()
        doReadyStatus()
        listenMachineObserve()
        mainViewModel.insertToNfcDeviceDatabase(true)
        nfcViewModel.setNFCAction(NFCAction.NONE)
        nfcViewModel.isObserveOutsideMainActivity = true

        lifecycleScope.launchWhenCreated {
            nfcViewModel.scanRfid.collectLatest {
                if (it.isNotEmpty()) {
                    viewModel.getMachineByRfid(it)
                    nfcViewModel.isObserveOutsideMainActivity = true
                    mainViewModel.insertToNfcDeviceDatabase(true)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.insertToNfcDeviceDatabase(false)
        nfcViewModel.isObserveOutsideMainActivity = false
    }

    private fun bindDataToViews() {
        binding.btnReady.isEnabled = false
        binding.toolBarTitleTextView.text =
            String.format("%s ${args.station}", langJson.getTranslation("Station"))

        val data = args.item ?: return
        coItem = data
        binding.apply {
            ivCritical.setImageResource(data.isCritical?.imageRes ?: R.drawable.ic_un_critical)
            tvMacSubType.text = String.format("%s: %s", data.macSubType, data.macSubTypeName)
            tvRemarkValue.text = data.remarks ?: "-"
            tvNeedleTypeValue.text = data.needleType ?: "-"
            viewModel.updateStatus(data.status)
            setupListAttachments(data.status, data.attachments)
            setupListCheckList(data.status, args.checkList)
        }
    }

    private fun setTranslation() {
        binding.apply {
            tvMachineNoTitle.text = "${langJson.getTranslation("Machine No.")} *"
        }
    }

    private lateinit var imageAdapter: AttImageAdapter
    private fun setupListAttachments(status: COStatusType?, list: List<AttachmentsItem?>?) {
        binding.apply {
            tvAttachmentEmpty.isVisible = list.isNullOrEmpty()
            rvAttachmentImage.isVisible = !list.isNullOrEmpty()
            rvAttachmentDes.isVisible = !list.isNullOrEmpty()
        }

        list ?: return
        status ?: return
        list.forEach {
            it?.isChecked = status == COStatusType.Ready
        }
        imageAdapter = AttImageAdapter(requestManager, list)
        binding.rvAttachmentImage.adapter = imageAdapter

        imageAdapter.setItemClick {
            val item = list[it]
            showViewAttachmentDialog(item)
        }

        val desAdapter = ReadyAttDesAdapter(list)
        binding.rvAttachmentDes.adapter = desAdapter
        desAdapter.setItemClick {
            validateData()
        }
    }

    private lateinit var checkListAdapter: ReadyCheckListAdapter
    private fun setupListCheckList(status: COStatusType?, array: Array<CheckListItem?>?) {
        val list = array?.toList()
        binding.apply {
            tvCheckListEmpty.isVisible = list.isNullOrEmpty()
            rvCheckList.isVisible = !list.isNullOrEmpty()
        }
        list ?: return
        status ?: return
        list.forEach {
            it?.isChecked = status == COStatusType.Ready
        }
        checkListAdapter = ReadyCheckListAdapter(status, list)
        binding.rvCheckList.adapter = checkListAdapter
        checkListAdapter.setItemClick {
            validateData()
        }
    }

    private fun listenMachineObserve() {
        viewModel.machine.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.LOADING -> loading.show(requireContext())
                Resource.Status.ERROR -> loading.dismiss()
                Resource.Status.SUCCESS -> {
                    loading.dismiss()
                    if (it.data?.isNotEmpty() == true) {
                        val machine = it.data[0]
                        // check if given macSubType and machine macSubType not match
                        if (machine.macSubTypeId?.toInt() != args.item?.macSubTypeId) {
                            showUnmatchedMacSubTypeDialog()
                            return@observe
                        }
                        binding.tvMachineNoValue.text = machine.machine
                        binding.llTabToScan.visibility = View.GONE
                        binding.tvMachineNoValue.visibility = View.VISIBLE
                        binding.ivDelete.visibility = binding.tvMachineNoValue.visibility
                        binding.tvLine.visibility = binding.tvMachineNoValue.visibility
                        binding.tvLine.text = machine.mfgLineId_desc ?: machine.areaId_desc
                        binding.tvStatus.isVisible = true
                        binding.tvStatus.text = langJson.getTranslation(machine.statusId_desc)
                        machineId = machine.id
                        validateData()
                    } else {
                        showSnackBar(binding.root, langJson.getTranslation("Machine not found"))
                        binding.edtMachine.setText("")
                        binding.tvMachineNoValue.text = "-"
                        binding.tvLine.text = ""
                        binding.tvStatus.text = ""
                        binding.llTabToScan.visibility = View.VISIBLE
                        binding.tvMachineNoValue.visibility = View.INVISIBLE
                        binding.ivDelete.visibility = binding.tvMachineNoValue.visibility
                        binding.tvLine.visibility = binding.tvMachineNoValue.visibility
                        binding.tvStatus.isVisible = false
                    }
                }
            }
        }
    }

    private var dialog: AlertDialog.Builder? = null
    private fun showUnmatchedMacSubTypeDialog() {
        if (dialog == null)
            dialog = AlertDialog.Builder(requireContext())
        val view = DialogUnmatchedMacsubtypeBinding.inflate(layoutInflater)
        view.tvTitle.text = langJson.getTranslation("Unmatched\nMachine Sub-type")
        view.tvMsg.text = "${langJson.getTranslation("Please check and try again")}."
        dialog?.apply {
            setView(view.root)
            setPositiveButton(langJson.getTranslation("OK")) { d, _ ->
                d.dismiss()
                dialog = null
            }
            show()
        }
    }

    private fun setupListener() {
        binding.ivCamera.setOnClickListener {
            startCameraScan()
        }

        binding.ivDelete.setOnClickListener {
            binding.tvMachineNoValue.text = "-"
            binding.llTabToScan.visibility = View.VISIBLE
            binding.tvMachineNoValue.visibility = View.INVISIBLE
            binding.tvLine.text = ""
            binding.tvLine.visibility = binding.tvMachineNoValue.visibility
            binding.tvStatus.text = ""
            binding.tvStatus.isVisible = false
            it.visibility = binding.tvMachineNoValue.visibility
            binding.edtMachine.setText("")
            validateData()

        }

        binding.ivCheckNeedleType.setOnClickListener {
            (it as ImageCheckBox).setToggleCheck()
            validateData()
        }

        binding.btnReady.setOnClickListener {
            val coItemId = args.item?.id ?: 0
            val coId = args.item?.coRequestId ?: 0
            val note = binding.edtRemark.text.toString()
            lifecycleScope.launch {
                viewModel.updateCOItem(coItemId, coId, machineId, note).collectLatest {
                    when (it.status) {
                        Resource.Status.LOADING -> loading.show(requireContext())
                        Resource.Status.ERROR -> {
                            loading.dismiss()
                            showSnackBar(binding.root, it.message.toString())
                        }
                        Resource.Status.SUCCESS -> {
                            loading.dismiss()
                            if (it.data != null) {
                                coItem = it.data.item
                                viewModel.updateStatus(COStatusType.Ready)
                                coViewModel.setReloadCODetail()
                                coViewModel.setReloadCOList()
                            }
                        }
                    }
                }
            }
        }

        binding.edtMachine.setOnEditorActionListener { text, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                requireActivity().hideKeyboard()
                viewModel.getMachineByMachineNo(text.text.toString())
                true
            }
            false
        }
    }

    private fun setStatusInformation() {
        val format = SimpleDateFormat("yyyy-MM-dd H:mm", Locale.getDefault())
        val logs = coItem?.logs?.find {
            it?.status == COStatusType.IN_PROGRESS_CODE.toString()
        }
        binding.tvPrepareStatusValue.text = if (logs != null) {
            String.format(
                "${langJson.getTranslation("Start Prepare")}: %s by %s",
                format.format(logs.updatedDt ?: Date()),
                logs.updatedBy
            )
        } else {
            String.format(
                "${langJson.getTranslation("Start Prepare")}: %s by %s",
                format.format(Date()),
                AuthUtil.username
            )
        }
    }

    private fun doReadyStatus() {
        viewModel.status.observe(viewLifecycleOwner) {
            if (it == COStatusType.Ready) {
                binding.apply {
                    mainViewModel.insertToNfcDeviceDatabase(false)
                    llTabToScan.visibility = View.GONE
                    tvMachineNoValue.visibility = View.VISIBLE
                    tvLine.visibility = tvMachineNoValue.visibility
                    tvStatus.isVisible = true
                    tvMachineNoValue.text = coItem?.machine
                    tvStatus.text = langJson.getTranslation("${coItem?.machineStatus}")
                    tvLine.text = coItem?.line ?: coItem?.area
                    ivDelete.isVisible = false
                    ivCheckNeedleType.setReady()
                    setupListAttachments(it, args.item?.attachments)
                    setupListCheckList(it, args.checkList)
                    edtRemark.isVisible = false
                    tvRemarkValue.text =
                        if (args.item?.remarks.isNullOrEmpty() && coItem?.note?.isEmpty() == true
                        ) {
                            "-"
                        } else if (args.item?.remarks.isNullOrEmpty()) {
                            coItem?.note
                        } else if (coItem?.note == null || coItem?.note?.isEmpty() == true) {
                            args.item?.remarks
                        } else {
                            "${args.item?.remarks}\n\n${coItem?.note}"
                        }

                    val format = SimpleDateFormat("yyyy-MM-dd H:mm", Locale.getDefault())
                    tvReadyStatusValue.isVisible = true
                    tvReadyStatusValue.text =
                        String.format(
                            "${langJson.getTranslation("Ready")}: %s by %s",
                            format.format(Date()),
                            AuthUtil.username
                        )
                    val logs = coItem?.logs?.find { item ->
                        item?.status == COStatusType.READY_CODE.toString()
                    }
                    if (logs != null) {
                        binding.tvReadyStatusValue.text = String.format(
                            "${langJson.getTranslation("Ready")}: %s by %s",
                            format.format(logs.updatedDt ?: Date()),
                            logs.updatedBy
                        )
                    }


                    btnReady.isVisible = false
                }
            }
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

    private fun validateData() {
        binding.apply {
            var isAttachmentChecked = true
            run loopAttachment@{
                imageAdapter.attachments.forEach {
                    isAttachmentChecked = it?.isChecked ?: false
                    if (!isAttachmentChecked) return@loopAttachment
                }
            }

            var isCheckList = true
            run loopCheckList@{
                checkListAdapter.list.forEach {
                    isCheckList = it?.isChecked ?: false
                    if (!isCheckList) return@loopCheckList
                }
            }

            val valid = (tvMachineNoValue.text != "-" &&
                    ivCheckNeedleType.isChecked &&
                    isAttachmentChecked && isCheckList
                    )

            val res = if (valid) {
                R.drawable.button
            } else {
                R.drawable.button_disabled
            }
            btnReady.setBackgroundResource(res)
            btnReady.isEnabled = valid
        }

    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.getMachineByMachineNo(result.contents)
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                barcodeLauncher.launch(ScanOptions())
            }
        }

    private fun startCameraScan() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            barcodeLauncher.launch(ScanOptions())
        }
    }
}