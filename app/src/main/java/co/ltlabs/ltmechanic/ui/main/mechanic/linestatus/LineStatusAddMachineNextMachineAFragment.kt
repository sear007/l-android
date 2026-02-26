package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.databinding.FragmentLineStatusAddMachineNextMachineABinding
import co.ltlabs.ltmechanic.databinding.PopupCheckInConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.network.MachineCheckInRequest
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusAddMachineNextMachineAViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import javax.inject.Inject


private const val TAG = "MachineNextMachineAFragment";

class LineStatusAddMachineNextMachineAFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusAddMachineNextMachineAViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusAddMachineNextMachineAViewModel::class.java)
    }

    private val  machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private lateinit var progressBar: ProgressBar

    private val args: LineStatusAddMachineNextMachineAFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    private var machineAlreadyInStation = false

    private var machineId = 0L
    private var machine = ""

    private lateinit var coordinatorLayout: CoordinatorLayout

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLineStatusAddMachineNextMachineABinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        showPopupWindow(binding.root)

        binding.toolBarSelectedLineTextView2.text = args.mfgLine
        binding.placeNoTextView.text = args.station
        binding.placeTextView4.visibility = View.GONE
        binding.machineIDTextView.visibility = View.GONE

        coordinatorLayout = binding.coordinatorLayout

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView2.text = getTranslation(placeTextView2.text.toString())
                placeTextView3.text = getTranslation(placeTextView3.text.toString())
                placeTextView4.text = getTranslation(placeTextView4.text.toString())
                placeTextView6.text = getTranslation(placeTextView6.text.toString())
                btnCancelInsert.text = getTranslation(btnCancelInsert.text.toString())
                btnConfirmInsert.text = getTranslation(btnConfirmInsert.text.toString())
            }
        }

        binding.btnCancelInsert.setOnClickListener {
//            navigateToStations()
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

        binding.btnConfirmInsert.setOnClickListener {
//            if (!machineAlreadyInStation) {

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
                    machineId,
                    binding.placeNoTextView.text.toString(),
                    args.mfgLineId,
                    DateTime(DateTimeZone.UTC).toString())
                machineViewModel.checkInMachine(machineCheckInRequest, addAction = true)
            }


//            } else {
//                Toast.makeText(activity, "This machine number is already in place", Toast.LENGTH_SHORT).show()
//            }
        }

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machineByNo ->
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machineByNo != null) {
                MachineUtil.machineNo = machineByNo.machine
                MachineUtil.machineArea = machineByNo.area
                MachineUtil.machineLocation = if (machineByNo.area.toLowerCase().contains("prod")) {
                    "${machineByNo.mfgLine} - ${machineByNo.station}"
                } else {
                    machineByNo.area
                }
                MachineUtil.machineHasOpenTickets = machineByNo.hasOpenTicket

                machine = machineByNo.machine
                binding.machineNoTextView.text = machineByNo.machine
                binding.machineCodeTextView.text = machineByNo.machine
                binding.machineIDTextView.text = machineByNo.rfid
                binding.machineSubTypeTextView.text = machineByNo.subtype

                dismissPopup()

                machineAlreadyInStation = machineByNo.station.isNotBlank()

                machineViewModel.machineDetailsByRfidComplete()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machineByNo ->
            if (machineByNo != null) {

                MachineUtil.machineNo = machineByNo.machine
                MachineUtil.machineArea = machineByNo.area
                MachineUtil.machineLocation = if (machineByNo.area.toLowerCase().contains("prod")) {
                    "${machineByNo.mfgLine} - ${machineByNo.station}"
                } else {
                    machineByNo.area
                }
                MachineUtil.machineHasOpenTickets = machineByNo.hasOpenTicket

                machineId = machineByNo.id

                if (machineByNo.rfid == null) {
                    binding.placeTextView4.visibility = View.GONE
                    binding.machineIDTextView.visibility = View.GONE
                } else {
                    binding.placeTextView4.visibility = View.VISIBLE
                    binding.machineIDTextView.visibility = View.VISIBLE
                }


                machine = machineByNo.machine
                binding.machineNoTextView.text = machineByNo.machine
                binding.machineCodeTextView.text = machineByNo.machine
                binding.machineIDTextView.text = machineByNo.rfid
                binding.machineSubTypeTextView.text = machineByNo.subtype

                dismissPopup()

                machineAlreadyInStation = machineByNo.station.isNotBlank()

            }
        })

        machineViewModel.machineCheckInStatus.observe(viewLifecycleOwner, Observer { checkInStatus ->
            if(checkInStatus != null) {
                when (checkInStatus) {
                    MachineCheckinStatus.SUCCESS -> {
//                    machinePlacesViewModel.machinePlaces.add(MachinePlace(place, args.machineNo, args.machineID, args.machineNo))

                        navigateToStations(true, machine, args.station)

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
                    else -> {
                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Check in: Something went wrong"))
                    }

                }

                machineViewModel.setMachineCheckInStatusComplete()
            }
        })

        return binding.root
    }

    private fun showPopupWindow(view: View) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .7).toInt()

        dismissPopup()
        popupWindow = getPopupWindow()
        popupWindow?.isOutsideTouchable = true

        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.let {
                    if (it.x < 0 || it.x > width) return true
                    if (it.y < 0 || it.y > height) return true
                }

                return false
            }

        })

        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, -25)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun startCameraScan() {

    //if (MainUtil.googlePlayAvailable) {
    //val intent = Intent(activity, CameraScanActivity::class.java)
    //startActivityForResult(intent, 0)
//} else {
    val integrator = IntentIntegrator.forSupportFragment(this)
    integrator.setPrompt("")
    integrator.initiateScan()
//}

}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            val barCode = result?.contents
            machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        binding.btnScanCamera.setOnClickListener {
            startCameraScan()
        }

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            dismissPopup()
        }

        binding.btnSubmitMachine.setOnClickListener {
            machineViewModel.getMachineByMachineNo(binding.machineEditText.text.toString())

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

    private fun navigateToStations(addMachineSuccess: Boolean, machine: String, station: String) {
        val action = LineStatusAddMachineNextMachineAFragmentDirections.actionLineStatusAddMachineNextMachineAFragmentToLineStatusStationsFragment(
            args.mfgLineId,
            args.mfgLine,
            addMachineSuccess,
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
            val machineCheckInRequest = MachineCheckInRequest(
                machineId,
                args.station,
                args.mfgLineId,
                DateTime(DateTimeZone.UTC).toString())
            machineViewModel.checkInMachine(machineCheckInRequest, addAction = true)
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

}
