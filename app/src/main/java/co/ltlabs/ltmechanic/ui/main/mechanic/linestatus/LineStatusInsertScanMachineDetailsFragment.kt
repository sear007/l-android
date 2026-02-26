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
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusInsertScanMachineDetailsBinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.network.MachineCheckInRequest
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusInsertScanMachineDetailsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class LineStatusInsertScanMachineDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusInsertScanMachineDetailsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusInsertScanMachineDetailsViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val args: LineStatusInsertScanMachineDetailsFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var nextMachineAClicked = false

    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLineStatusInsertScanMachineDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        if (args.rfid.isBlank()) {
            binding.placeTextView4.visibility = View.GONE
            binding.machineIDTextView.visibility = View.GONE
        }

//        binding.toolBarTitleTextView.text = getString(R.string.line_status_place, args.station)
        binding.toolBarTitleTextView.text = "${languageJsonObject.getTranslation("PLACE")} ${args.station}"
        binding.toolBarSelectedLineTextView2.text = args.mfgLine
        binding.placeNoTextView.text = args.station
        binding.machineNoTextView.text = args.machine
        binding.machineCodeTextView.text = args.machine
        binding.machineIDTextView.text = args.rfid
        binding.machineSubTypeTextView.text = args.subType
        binding.btnConfirmOpenALineSetup2.text = "${languageJsonObject.getTranslation("CONFIRM AND OPEN")} ${args.station}-A"

        with(languageJsonObject) {
            with(binding) {
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView6.text = getTranslation(placeTextView6.text.toString())
                btnCancelInsert.text = getTranslation(btnCancelInsert.text.toString())
                btnConfirmInsert.text = getTranslation(btnConfirmInsert.text.toString())
            }
        }

        binding.btnConfirmInsert.setOnClickListener {
            nextMachineAClicked = false

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
                lineViewModel.insertBetweenMachines(args.machineId, args.station, args.mfgLineId)
            }
        }

        binding.btnCancelInsert.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnConfirmOpenALineSetup2.setOnClickListener {
            nextMachineAClicked = true

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
                lineViewModel.insertBetweenMachines(args.machineId, args.station, args.mfgLineId)
            }

        }

        lineViewModel.machineInsertStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    MachineInsertStatus.SUCCESS -> {

                        if (nextMachineAClicked) {
                            navigateToNextMachine()
                        } else {
                            navigateToStations()
                        }

                    }

                    MachineInsertStatus.MACHINE_NOT_WORKING -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This Machine is Retired/Not Available"))
                    }
                    MachineInsertStatus.MACHINE_CURRENTLY_IN_PLACE -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("This machine number is already in place"))
                    }
                    MachineInsertStatus.NOT_IN_FLOATING_AREA -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine is not in Floating area"))
                    }
                    MachineInsertStatus.HAS_OPEN_TICKETS -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot check in. Machine has open tickets"))
                    }

                    else -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Something went wrong"))
                    }

                }
                lineViewModel.insertBetweenMachinesComplete()
            }
        })

        lineViewModel.status.observe(viewLifecycleOwner, Observer {
            when(it) {

                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }
                else -> {
                    progressBar.showProgressBar(false)
                }

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
            lineViewModel.insertBetweenMachines(args.machineId, args.station, args.mfgLineId)
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
        val action = LineStatusInsertScanMachineDetailsFragmentDirections.actionLineStatusInsertScanMachineDetailsFragmentToLineStatusStationsFragment(
            args.mfgLineId,
            args.mfgLine,
            false,
            args.machine,
            args.station
        )
        navigate(action)
    }

    private fun navigateToNextMachine() {
        val action = LineStatusInsertScanMachineDetailsFragmentDirections.actionLineStatusInsertScanMachineDetailsFragmentToLineStatusInsertNextMachineAScanMachineFragment(
            args.mfgLineId,
            args.mfgLine,
            "${args.station}-A"
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
