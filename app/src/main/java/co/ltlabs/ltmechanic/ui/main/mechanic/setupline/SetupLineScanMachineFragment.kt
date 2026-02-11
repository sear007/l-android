package co.ltlabs.ltmechanic.ui.main.mechanic.setupline

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentSetupLineScanMachineBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineScanMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "ScanMachineFragment";

/**
 * A simple [Fragment] subclass.
 */
class SetupLineScanMachineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    lateinit var progressBar: ProgressBar

    private val viewModel: SetupLineScanMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupLineScanMachineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private var nfcAdapter: NfcAdapter? = null

    private var nextMachine = MachineInStation(0, "", "", "", "")
    private var nextMachineA = MachineInStation(0, "", "", "", "")

    private val args: SetupLineScanMachineFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        machineViewModel.getMachinesInStation(args.selectedLineId)

        Log.d(TAG, "onCreateView: args.origin: ${args.origin}")

        val binding = FragmentSetupLineScanMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.placeNoTextView.text = args.selectedPlace
        binding.toolBarTitleTextView.text = args.selectedLine
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView8.text = getTranslation(placeTextView8.text.toString())
                //btnScan.text = getTranslation(btnScan.text.toString())
                btnKeepEmpty.text = getTranslation(btnKeepEmpty.text.toString())
                btnFinishLineSetup.text = getTranslation(btnFinishLineSetup.text.toString())
            }
        }

        // EditText DrawableRight
        val searchMachineEt = binding.machineEditText
        searchMachineEt.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (event?.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= (v.right - searchMachineEt.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    startCameraScan()
                    true
                }
            }
            false
        }

        binding.btnSubmitMachine.setOnClickListener {
            machineViewModel.getMachineByMachineNo(binding.machineEditText.text.toString())
        }

        binding.btnFinishLineSetup.setOnClickListener {
//            LineUtil.lastSelectedStation = ""
//            LineUtil.finishedSetupLine = true

//            if (LineUtil.fromLinePlaces) {
//                activity?.onBackPressed()
//
//                LineUtil.fromLinePlaces = false
//            } else {
                navigateToSetupLinePlaces()
//            }


        }

        coordinatorLayout = binding.coordinatorLayout

        progressBar = binding.progressBar

        LineUtil.lastSelectedStation = args.selectedPlace

        val placeInt = args.selectedPlace.replace("-A", "").toInt()
        Log.d(TAG, "onCreateView: placeInt: $placeInt")
        val place = if (placeInt > 9) placeInt.toString() else "0$placeInt"
        val machineSamePlace = if (placeInt > 9) "$placeInt-A" else "0$placeInt-A"
        Log.d(TAG, "onCreateView: args.selectedPlace: ${args.selectedPlace}")
        val nextPlace = if ((placeInt + 1) > 9) "${placeInt + 1}" else "0${placeInt + 1}"
        Log.d(TAG, "onCreateView: nextPlace: $nextPlace")
        val nextMachineSamePlace = "0$nextPlace-A"

        Log.d(TAG, "onCreateView: place: $place")

        machineViewModel.getNextMachineByStation(nextPlace, args.selectedLineId)
        machineViewModel.getNextMachineAByStation(machineSamePlace, args.selectedLineId)

        machineViewModel.nextMachine.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                nextMachine = it
            }

        })

        machineViewModel.nextMachineA.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                nextMachineA = it
            }

        })

        binding.btnKeepEmpty.setOnClickListener {

            if (!place.contains("-A")) {
                if (nextMachine.machine.isNotBlank()) {
                    navigateToSetupLineMachineDetails(
                        args.selectedLine,
                        nextPlace,
                        nextMachine.machine,
                        nextMachine.machine,
                        nextMachine.id.toString(),
                        nextMachine.rfid, nextMachine.subType
                    )
                } else {

//                    if (args.origin == "linePlaces") {
//                        navigateToSetupLinePlaces()
//                    } else {
                        navigateToSelf(nextPlace)
//                    }
                }
            } else {
                if (nextMachineA.machine.isNotBlank()) {
                    navigateToSetupLineMachineDetails(
                        args.selectedLine,
                        nextPlace,
                        nextMachineA.machine,
                        nextMachineA.machine,
                        nextMachineA.id.toString(),
                        nextMachineA.rfid, nextMachine.subType
                    )
                } else {
                    Log.d(TAG, "onCreateView: args.origin:  ${args.origin == "linePlaces"} ")
                    if (args.origin == "linePlaces") {
                        navigateToSetupLinePlaces()
                    } else {
                        navigateToSelf(nextPlace)
                    }
                }
            }
        }

        /*binding.btnScan.setOnClickListener {

            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .7).toInt()

            dismissPopup()
            popupWindow = showPopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            DimUtil.dimBehind(popupWindow)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }*/

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        machineViewModel.machineDetailsByRfid.observe(viewLifecycleOwner, Observer { machine ->
            mainViewModel.insertToNfcDeviceDatabase(false)
            if (machine != null) {
                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket
                navigateToSetupLineMachineDetails(args.selectedLine,
                    binding.placeNoTextView.text.toString(),
                    machine.machine, machine.machine,
                    machine.id.toString(),
                    machine.rfid ?: "",
                    machine.subtype ?: "")
                machineViewModel.machineDetailsByRfidComplete()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            if (machine != null) {
                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

                //TODO machine ticket no
//                MachineUtil.machineOpenTicketNo =

                //hide keyboard
                //activity?.hideKeyboard()
                navigateToSetupLineMachineDetails(args.selectedLine,
                    binding.placeNoTextView.text.toString(),
                    machine.machine, machine.machine,
                    machine.id.toString(),
                    machine.rfid ?: "",
                    machine.subtype ?: "")
                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
            when (machineStatus) {
                MachineStatus.FOUND -> {
                    activity?.hideKeyboard()
                } else -> {
                dismissPopup()
                if (MachineUtil.message.isNotBlank()) {
                    binding.coordinatorLayout.showSnackbar(
                        languageJsonObject.getTranslation(
                            "You do not have access to the machine's current location"
                        )
                    )
                } else {

                    binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine number not found"))
                }
                }
            }
        })

        machineViewModel.machinesInStation.observe(viewLifecycleOwner, Observer {
//            if (args.origin == "linePlaces") {
//                binding.btnFinishLineSetup.visibility = View.INVISIBLE
//            } else {
                if (it != null) {
                    binding.btnFinishLineSetup.visibility = View.VISIBLE

                } else {
                    binding.btnFinishLineSetup.visibility = View.INVISIBLE
                }
//            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    showProgressBar(true)
                }
                else -> {
                    dismissPopup()
                    showProgressBar(false)
                }
            }
        })

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToSetupLine()
            }

        })

        return binding.root
    }

    private fun startCameraScan() {

        Log.d(TAG, "startCameraScan: MainUtil.googlePlayAvailable: ${MainUtil.googlePlayAvailable}")

        //if (MainUtil.googlePlayAvailable) {
    //val intent = Intent(activity, CameraScanActivity::class.java)
    //startActivityForResult(intent, 0)
//} else {
    val integrator = IntentIntegrator.forSupportFragment(this)
    integrator.setPrompt("")
    integrator.initiateScan()
//}

    }

    override fun onResume() {
        super.onResume()
        mainViewModel.insertToNfcDeviceDatabase(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //        if (MainUtil.googlePlayAvailable) {
//            if (requestCode == 0) {
//                if (resultCode == CommonStatusCodes.SUCCESS) {
//                    dismissPopup()
//                    if (data != null) {
//
//                        var barcode: Barcode? = data.getParcelableExtra("barcode")
//                        machineViewModel.getMachineByMachineNo(barcode?.displayValue.toString())
//
//                    } else {
//                        coordinatorLayout.showSnackbar(
//                            languageJsonObject.getTranslation(
//                                "No QR code found"
//                            )
////                            "No QR code found"
//                        )
//                    }
//                }
//            } else {
//
//            }
//        } else {
            val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            val barCode = result?.contents
            machineViewModel.getMachineByMachineNo(barCode.toString())

//        }

        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun navigateToSetupLineMachineDetails(selectedLine: String, place: String, machineNo: String, machineCode: String, machineID: String, rfid: String, subType: String) {

        val action = SetupLineScanMachineFragmentDirections
            .actionSetupLineScanMachineFragmentToSetupLineMachineInPlaceDetailsFragment(
                selectedLine,
                place,
                machineNo,
                machineCode,
                machineID,
                rfid,
                args.selectedLineId,
                false, args.origin,
                subType)

        navigate(action)
    }

    private fun navigateToSetupLinePlaces() {
        val action = SetupLineScanMachineFragmentDirections
            .actionSetupLineScanMachineFragmentToSetupLinePlacesFragment(args.selectedLineId, args.selectedLine)

        val bundle = Bundle()
        bundle.putLong("selectedLineId", args.selectedLineId)
        bundle.putString("selectedLine", args.selectedLine)

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.action_setupLineScanMachineFragment_to_setupLinePlacesFragment, true)
            .setPopUpTo(R.id.action_setupLineScanMachineFragment_self, true)
            .setPopUpTo(R.id.action_setupLineMachineInPlaceDetailsFragment_to_setupLineScanMachineFragment, true)
            .build()

        navigate(action)
    }

    private fun navigateToSelf(nextPlace: String) {
        val action = SetupLineScanMachineFragmentDirections.actionSetupLineScanMachineFragmentSelf(args.selectedLine, nextPlace, args.selectedLineId, "")
        navigate(action)
    }

    /*private fun showPopupWindow(): PopupWindow {

        mainViewModel.insertToNfcDeviceDatabase(true)

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupScanOptionsBinding.inflate(inflater)
        val closeButton = binding.closePopup

        with(languageJsonObject) {
            with(binding) {
                labelReadyToScan.text = getTranslation(labelReadyToScan.text.toString())
                machineEditText.hint = getTranslation(machineEditText.hint.toString())
                btnScanCamera.text = getTranslation(btnScanCamera.text.toString())
                labelTitleScanNFC.text = getTranslation(labelTitleScanNFC.text.toString())
                labelNFCDescription.text = getTranslation(labelNFCDescription.text.toString())
                labelTitleScanBarcode.text = getTranslation(labelTitleScanBarcode.text.toString())
                labelBarcodeDescription.text = getTranslation(labelBarcodeDescription.text.toString())
            }
        }

        binding.btnScanCamera.setOnClickListener {
            startCameraScan()
        }

        closeButton.setOnClickListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            dismissPopup()
        }

        *//*binding.btnSubmitMachine.setOnClickListener {
            machineViewModel.getMachineByMachineNo(binding.machineEditText.text.toString())

        }*//*

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }*/

    private fun showProgressBar(visible: Boolean) {
        with(progressBar) {
            visibility = if (visible) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.insertToNfcDeviceDatabase(false)
    }

    private fun navigateToSetupLine() {
        val action = SetupLineScanMachineFragmentDirections
            .actionSetupLineScanMachineFragmentToSetupLineFragment(args.selectedLine, args.selectedLineId)
        navigate(action)
    }

}
