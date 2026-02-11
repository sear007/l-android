package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusInsertNextMachineScanMachineDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.network.MachineCheckInRequest
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusInsertNextMachineScanMachineDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "NextMachineScanDetails";

class LineStatusInsertNextMachineScanMachineDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusInsertNextMachineScanMachineDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusInsertNextMachineScanMachineDetailsViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val args: LineStatusInsertNextMachineScanMachineDetailsFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLineStatusInsertNextMachineScanMachineDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        if (args.rfid.isBlank()) {
            binding.placeTextView4.visibility = View.GONE
            binding.machineIDTextView.visibility = View.GONE
        }

        binding.toolBarTitleTextView.text = getString(R.string.line_status_place, args.station)
        binding.toolBarSelectedLineTextView2.text = args.mfgLine
        binding.placeNoTextView.text = args.station
        binding.machineNoTextView.text = args.machine
        binding.machineCodeTextView.text = args.machine
        binding.machineIDTextView.text = args.rfid
        binding.machineSubTypeTextView.text = args.subType

        binding.btnConfirmInsert.setOnClickListener {

            if (!MachineUtil.machineArea.toLowerCase().contains("floating")) {
                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .4).toInt()

                dismissPopup()
                popupWindow = showCheckInConfirmationPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            } else {
                val machineCheckInRequest = MachineCheckInRequest(
                    args.machineId,
                    binding.placeNoTextView.text.toString(),
                    args.mfgLineId,
                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkInMachine(machineCheckInRequest, insertAction = true)
            }


        }

        binding.btnCancelInsert.setOnClickListener {
            activity?.onBackPressed()
        }

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }
                else -> {
                    progressBar.showProgressBar(false)
                }
            }
        })

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            if(checkInStatus != null) {
                when (checkInStatus) {
                    MachineCheckinStatus.SUCCESS -> {

                        navigateToStations()

                    }
                    MachineCheckinStatus.MACHINE_NOT_WORKING -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This Machine is Retired/Not Available"))
                    }
                    MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This machine number is already in place"))
                    }
                    MachineCheckinStatus.NOT_IN_FLOATING_AREA -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine is not in Floating area"))
                    }
                    MachineCheckinStatus.HAS_OPEN_TICKETS -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine has open tickets"))
                    }
                    else -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Check in: Something went wrong"))
                    }

                }

                machineViewModel.setMachineCheckInStatusComplete()
            }
        })

        return binding.root
    }

    private fun showCheckInConfirmationPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupCheckInConfirmationMessageBinding.inflate(inflater)

        if (MachineUtil.machineHasOpenTickets) {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(languageJsonObject
                    .getTranslation(
                        "This machine has open Repair Ticket [] and is currently in []. Do you want to move this machine on []?"
                    ))
                    .format(MachineUtil.machineOpenTicketNo, MachineUtil.machineLocation, "${args.mfgLine} - ${args.station}")
        } else {
            binding.labelConfirmation.text =
                StrUtil.replaceStr(languageJsonObject
                    .getTranslation(
                        "This machine is currently in []. Do you want to move this machine on [] ?"
                    ))
                    .format(MachineUtil.machineLocation, "${args.mfgLine} - ${args.station}")
        }

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            dismissPopup()
            val machineCheckInRequest = MachineCheckInRequest(
                args.machineId,
                args.station,
                args.mfgLineId,
                DateTime(DateTimeZone.UTC).toString())
            machineViewModel.checkInMachine(machineCheckInRequest, insertAction = true)
            MachineUtil.clear()
        }

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()
        }


        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToStations() {
        val action = LineStatusInsertNextMachineScanMachineDetailsFragmentDirections.actionLineStatusInsertNextMachineScanMachineDetailsFragmentToLineStatusStationsFragment(
            args.mfgLineId,
            args.mfgLine,
            false,
            "",
            ""
        )
        navigate(action)
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

}
