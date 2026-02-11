package co.ltlabs.ltmechanic.ui.main.mechanic.replacemachine

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentReplaceMachineScanDetailsBinding
import co.ltlabs.ltmechanic.databinding.FragmentReplaceMachineScanDetailsConfirmBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.ReplaceMachineScanDetailsConfirmViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.ReplaceMachineScanDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "RMDetailsConfirmFragment";

class ReplaceMachineScanDetailsConfirmFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: ReplaceMachineScanDetailsConfirmViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReplaceMachineScanDetailsConfirmViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    lateinit var progressBar: ProgressBar

    private val args: ReplaceMachineScanDetailsConfirmFragmentArgs by navArgs()

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentReplaceMachineScanDetailsConfirmBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.machineNoTextView.text = args.machine
        binding.mfgLineTextView.text = args.mfgLine
        binding.stationTextView.text = args.station
        binding.machineNoToCheckInTextView.text = args.machineToCheckIn

        coordinatorLayout = binding.coordinatorLayout

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
//                coordinatorLayout.showSnackbar("Machine to check in is already in place.")
//            }

        }

        binding.btnCancel.setOnClickListener {
//            navigateToReplaceMachine()
            activity?.onBackPressed()
        }

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            if(checkInStatus != null) {
                when (checkInStatus) {
                    MachineCheckinStatus.SUCCESS -> {

//                        navigateToReplaceMachine()
                        navigateToMechanicHome("replace", true, args.machine, args.station)

                    }
                    MachineCheckinStatus.MACHINE_NOT_WORKING -> {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This Machine is Retired/Not Available"))
                    }
                    MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE -> {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This machine number is already in place"))
                    }
                    MachineCheckinStatus.NOT_IN_FLOATING_AREA -> {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine is not in Floating area"))
                    }
                    MachineCheckinStatus.HAS_OPEN_TICKETS -> {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine has open tickets"))
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

    private fun navigateToReplaceMachine() {
        val action = ReplaceMachineScanDetailsConfirmFragmentDirections.actionReplaceMachineScanDetailsConfirmFragmentToReplaceMachineFragment(
            0,
            "",
            ""
        )
        navigate(action)
    }

    private fun navigateToMechanicHome(replaceAction: String, replaceSuccess: Boolean, replaceMfgLine: String, replaceStation: String) {
        val action = ReplaceMachineScanDetailsConfirmFragmentDirections
            .actionReplaceMachineScanDetailsConfirmFragmentToMechanicHomeFragment(replaceAction, replaceSuccess, replaceMfgLine, replaceStation)
        navigate(action)
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

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

}
