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
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusReplaceMachineScanDetailsConfirmBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusReplaceMachineScanDetailsConfirmViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class LineStatusReplaceMachineScanDetailsConfirmFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusReplaceMachineScanDetailsConfirmViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusReplaceMachineScanDetailsConfirmViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private var popupWindow: PopupWindow? = null

    lateinit var progressBar: ProgressBar

    private val args: LineStatusReplaceMachineScanDetailsConfirmFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding = FragmentLineStatusReplaceMachineScanDetailsConfirmBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.machineNoTextView.text = args.machine
        binding.mfgLineTextView.text = args.mfgLine
        binding.stationTextView.text = args.station
        binding.machineNoToCheckInTextView.text = args.machineToCheckIn

        progressBar = binding.progressBar

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView7.text = getTranslation(placeTextView7.text.toString())
                btnCancel.text = getTranslation(btnCancel.text.toString())
                btnConfirm.text = getTranslation(btnConfirm.text.toString())
            }
        }

        binding.btnConfirm.setOnClickListener {

//            if (args.scannedMachineStation.isBlank()) {

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
                    machineViewModel.replaceMachine(
                        args.machineId,
                        args.machineIdToCheckIn,
                        args.station,
                        args.mfgLineId
                    )
                }

//            } else {
//                binding.coordinatorLayout.showSnackbar("Machine to check in is already in place.")
//            }

        }

        binding.btnCancel.setOnClickListener {
//            navigateToStations()
            activity?.onBackPressed()
        }

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            if(checkInStatus != null) {
                when (checkInStatus) {
                    MachineCheckinStatus.SUCCESS -> {
                        navigateToStations(args.machine, args.station)
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

                }

                machineViewModel.setMachineCheckInStatusComplete()
            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    showProgressBar(false)
                }
            }
        })

        return binding.root

    }

    private fun showProgressBar(visible: Boolean) {
        with(progressBar) {
            visibility = if (visible) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun navigateToStations(machine: String, station: String) {
        val action = LineStatusReplaceMachineScanDetailsConfirmFragmentDirections.actionLineStatusReplaceMachineScanDetailsConfirmFragmentToLineStatusStationsFragment(
            args.mfgLineId,
            args.mfgLine,
            false,
            machine,
            station
        )
        navigate(action)
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
            machineViewModel.replaceMachine(
                args.machineId,
                args.machineIdToCheckIn,
                args.station,
                args.mfgLineId
            )
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

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

}
