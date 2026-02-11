package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.databinding.FragmentMaintenancePreviewBinding
import co.ltlabs.ltmechanic.databinding.PopupMaintenanceOptionMenuBinding
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenancePreviewViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_maintenance_preview.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MaintenancePreviewFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintenancePreviewViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MaintenancePreviewViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val args: MaintenancePreviewFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    private var machineId = 0L
    private var status: String = ""
    private var checklistCount: Int = 0
    private var remark: String? = null

    private lateinit var binding: FragmentMaintenancePreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMaintenancePreviewBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            kotlin.with(binding) {
                binding.toolBarTitleTextView.text = getTranslation(args.ticketNo)
                machineNoLabel.text = getTranslation(machineNoLabel.text.toString())
                brandLabel.text = getTranslation(brandLabel.text.toString())
                subTypeLabel2.text = getTranslation(subTypeLabel2.text.toString())
                lpmDateLabel.text = getTranslation(lpmDateLabel.text.toString())
                maintenanceFreqLabel.text = getTranslation(maintenanceFreqLabel.text.toString())
                rentalLabel.text = getTranslation(rentalLabel.text.toString())
                statusLabel.text = getTranslation(statusLabel.text.toString())
                locationLabel.text = getTranslation(locationLabel.text.toString())
                btnOptions.text = getTranslation(btnOptions.text.toString())
                btnNextAction.text = getTranslation(btnNextAction.text.toString())
            }
        }
        // End translation



        binding.btnOptions.setOnClickListener {
            showPopupWindow(binding.root, showOptionsPopupWindow())
        }

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

//                    ticketViewModel.getTicketDetailsById(args.ticketId)
                    ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

//        ticketViewModel.getTicketDetailsById(args.ticketId)
//        ticketViewModel.getTicketDetailsById(1279)
        ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

//        if (binding.status.text != "SCHEDULED") {
//            binding.btnNextAction.text = "VIEW CHECKLIST"
//        }

        binding.btnNextAction.setOnClickListener {

            if (btnNextAction.tag == "VIEW CHECKLIST") {
                if (status == MaintType.WIP && checklistCount == 0) {
                    val direction = MaintenancePreviewFragmentDirections.actionMaintenancePreviewFragmentToMaintAddCheckListFragment(
                        args.machineId,
                        args.ticketId,
                        args.ticketNo,
                        status,
                        remark
                    )
                    findNavController().navigate(direction)
                } else {
                    navigateToChecklist(
                        args.ticketId,
                        args.ticketNo,
                        binding.status.text.toString()
                    )
                }
            } else {
//                ticketViewModel.updateTicketStatus(args.ticketNo, StatusIdUtil.MT_IN_PROGRESS.toString(), type = "M")
                ticketViewModel.getStatusIdAndUpdateTicketStatus(
                    TicketsStatus.IN_PROGRESS, TicketModule.MAINTENANCE, args.ticketNo, type = "M"
                )
            }

        }

        ticketViewModel.ticket.observe(viewLifecycleOwner, Observer { ticket ->

            if (ticket != null) {
                status = ticket.status
                remark = ticket.remarks

                ticketViewModel.checkList.observe(viewLifecycleOwner) {
                    checklistCount = it.size
                }

                binding.apply {
                    machineId = ticket.machineId
                    machineNo.text = ticket.machineNo
                    brand.text = ticket.brand
                    subType.text = ticket.subType
                    lpmDate2.text = if (ticket.lpmDate != null) {
                        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        format.format(ticket.lpmDate)
                    } else {
                        ""
                    }
                    maintenanceFreq.text = ticket.maintenanceFreq
                    rental.text = ticket.rental
                    status.text = languageJsonObject.getTranslation(ticket.status)
                    location.text = ticket.place

                    status.setTextColor(Color.parseColor(
                        when (ticket.status) {

                            "SCHEDULED", "COMPLETED" -> {
                                "#95F204"
                            }

                            else -> {
                                "#F59A23"
                            }

                        }
                    ))

                    btnNextAction.text = if (ticket.status == "SCHEDULED") {
                        btnNextAction.tag = "START MAINTENANCE"
                        languageJsonObject.getTranslation("START MAINTENANCE")
                    } else {
                        btnNextAction.tag = "VIEW CHECKLIST"
                        languageJsonObject.getTranslation("VIEW CHECKLIST")
                    }
                }

//                ticketViewModel.ticketComplete()
            }

        })

        ticketViewModel.ticketStatus.observe(viewLifecycleOwner, Observer { ticketStatus ->
            if (ticketStatus != null) {

                when (ticketStatus) {

                    TicketStatus.COMPLETED -> {
                        status = MaintType.COMPLETED
                        binding.btnNextAction.tag = "VIEW CHECKLIST"
                        binding.btnNextAction.text = languageJsonObject.getTranslation("VIEW CHECKLIST")
                    }

                    TicketStatus.IN_PROGRESS -> {
                        status = MaintType.WIP
                        binding.status.setTextColor(Color.parseColor("#F59A23"))
                        binding.status.text = languageJsonObject.getTranslation("IN PROGRESS")
                        binding.btnNextAction.text = languageJsonObject.getTranslation("VIEW CHECKLIST")
                        binding.btnNextAction.tag = "VIEW CHECKLIST"
                    }

                }

                ticketViewModel.ticketStatusComplete()
            }
        })

        ticketViewModel.ticketUpdateStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    TicketUpdateStatus.HAS_OPEN_TICKETS -> {
                        binding.coordinatorLayout.showSnackbar(
                            languageJsonObject.getTranslation(
                                "Cannot Start Maintenance. Machine has open repair ticket"
                            )
                        )
                    }
                }
                ticketViewModel.ticketUpdateComplete()
            }
        })

        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {

                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }

                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        return binding.root

    }

    private fun showPopupWindow(view: View, popupWindowType: PopupWindow) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .25).toInt()

        dismissPopup()
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = false

//        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                event?.let {
//                    if (it.x < 0 || it.x > width) return true
//                    if (it.y < 0 || it.y > height) return true
//                }
//
//                return false
//            }
//
//        })

        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, height * 2)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun showOptionsPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupMaintenanceOptionMenuBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                btnMaintenanceHistory.text = getTranslation(btnMaintenanceHistory.text.toString())
                btnRepairHistory.text = getTranslation(btnRepairHistory.text.toString())
            }
        }
        // End translation

        binding.btnMaintenanceHistory.setOnClickListener {
            dismissPopup()
            navigateToMaintenanceHistory(
                args.ticketId,
                args.ticketNo,
                machineId
            )
        }

        binding.btnRepairHistory.setOnClickListener {
            dismissPopup()
            navigateToRepairHistory(
                machineId
            )
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToChecklist(
        ticketId: Long,
        ticketNo: String,
        ticketStatus: String
    ) {
        val bundle = bundleOf(
            "ticketId" to ticketId,
            "ticketNo" to ticketNo,
            "ticketStatus" to ticketStatus,
            "remark" to remark
        )
        findNavController().navigate(R.id.action_global_to_maintenanceChecklistFragment, bundle)
    }

    private fun navigateToRepairHistory(
        machineId: Long
    ) {
        val bundle = bundleOf("machineId" to machineId)
        findNavController().navigate(
            R.id.action_maintenancePreviewFragment_to_repairHistoryFragment,
            bundle
        )
    }

    private fun navigateToMaintenanceHistory(
        ticketId: Long,
        ticketNo: String,
        machineId: Long
    ) {
        TicketUtil.isQueryMachine = false
        TicketUtil.maintenanceTicketStatus = "completed"
        val bundle =
            bundleOf("ticketId" to ticketId, "ticketNo" to ticketNo, "machineId" to machineId)
        findNavController().navigate(
            R.id.action_maintenancePreviewFragment_to_maintenanceHistoryFragment,
            bundle
        )
    }

}
