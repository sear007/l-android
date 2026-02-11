package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusInsertScanMachineBinding
import co.ltlabs.ltmechanic.databinding.PopupKeepEmptyConfirmationMessageBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusInsertScanMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "InsertScanMachine"

class LineStatusInsertScanMachineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineStatusInsertScanMachineViewModel by lazy {
        ViewModelProvider(
            this,
            providerFactory
        ).get(LineStatusInsertScanMachineViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private val nfcViewModel: NFCViewModel by activityViewModels()

    private var nfcAdapter: NfcAdapter? = null

    private val args: LineStatusInsertScanMachineFragmentArgs by navArgs()

    lateinit var progressBar: ProgressBar

    private var stationNumber:String?=null

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        val binding = FragmentLineStatusInsertScanMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        progressBar = binding.progressBar

        args.station.let {
            val lengthStation=it.length
            stationNumber = if(lengthStation==1){
                "0$it"
            }else it
        }
        binding.placeNoTextView.text = stationNumber
//        binding.toolBarTitleTextView.text = getString(R.string.line_status_place, args.station)
        binding.toolBarTitleTextView.text =
            "${languageJsonObject.getTranslation("PLACE")} $stationNumber"
        binding.toolBarSelectedLineTextView.text = args.mfgLine

        with(languageJsonObject) {
            with(binding) {
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView8.text = getTranslation(placeTextView8.text.toString())
                btnScan.text = getTranslation(btnScan.text.toString())
                btnKeepEmpty.text = getTranslation(btnKeepEmpty.text.toString())
            }
        }

        coordinatorLayout = binding.coordinatorLayout

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
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
                navigateToScanDetails(
                    args.mfgLineId,
                    args.mfgLine,
                    stationNumber.toString(),
                    machine.id,
                    machine.machine,
                    machine.rfid ?: "",
                    machine.subtype ?: "",
                    args.actionTitle,
                    args.actionName
                )

                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        lineViewModel.machineInsertStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    MachineInsertStatus.SUCCESS -> {
                        navigateToStations()
                    }

                    else -> {
                        binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Something went wrong"))
                    }
                }
                lineViewModel.insertBetweenMachinesComplete()
            }
        })

        lineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {

                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }
                else -> {
                    progressBar.showProgressBar(false)
                }

            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }
                else -> {
                    dismissPopup()
                    progressBar.showProgressBar(false)
                }
            }
        })

        binding.btnKeepEmpty.visibility = if (args.endStation) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        binding.btnKeepEmpty.setOnClickListener {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .4).toInt()

            dismissPopup()
            popupWindow = showConfirmationPopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            DimUtil.dimBehind(popupWindow)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
            when (machineStatus) {
                MachineStatus.FOUND -> {

                }
                else -> {
                    dismissPopup()


                    if (MachineUtil.message.isNotBlank()) {
                        binding.coordinatorLayout.showSnackbar(
                            languageJsonObject.getTranslation(
                                "You do not have access to the machine's current location"
                            )
                        )
                    } else {

                        coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Machine number not found"))
                    }
                }
            }
        })

        binding.btnScan.setOnClickListener {
            nfcViewModel.needMfgLineId = args.mfgLineId
            nfcViewModel.needMfgLine = args.mfgLine
            nfcViewModel.needStation = stationNumber.toString()
            nfcViewModel.setNFCAction(NFCAction.ADD_MACHINE)
        }

        return binding.root
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
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        val barCode = result?.contents
        machineViewModel.getMachineByMachineNo(barCode.toString())

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showPopupWindow(): PopupWindow {

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
                labelBarcodeDescription.text =
                    getTranslation(labelBarcodeDescription.text.toString())
            }
        }

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

    private fun showConfirmationPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupKeepEmptyConfirmationMessageBinding.inflate(inflater)

//        binding.textView3.text = getString(R.string.keep_empty_confirmation_message, args.mfgLine, args.station)
        binding.textView3.text =
            "${languageJsonObject.getTranslation("Are you sure you want to empty Line")} ${args.mfgLine} ${stationNumber}?"

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        binding.btnConfirmLineSetup2.setOnClickListener {
            lineViewModel.insertBetweenMachines(null, stationNumber.toString(), args.mfgLineId, true)
//            machineViewModel.checkOutMachine(args.machineId)
            dismissPopup()
//            navigateToStations()
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

    private fun navigateToScanDetails(
        mfgLineId: Long,
        mfgLine: String,
        station: String,
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        actionTitle: String,
        actionName: String
    ) {
        val action =
            LineStatusInsertScanMachineFragmentDirections.actionLineStatusInsertScanMachineFragmentToLineStatusInsertScanMachineDetailsFragment(
                mfgLineId,
                mfgLine,
                station,
                machineId,
                machine,
                rfid,
                subType,
                actionTitle,
                actionName
            )
        navigate(action)
    }

    private fun navigateToStations() {
        val action =
            LineStatusInsertScanMachineFragmentDirections.actionLineStatusInsertScanMachineFragmentToLineStatusStationsFragment(
                args.mfgLineId,
                args.mfgLine,
                false,
                "",
                ""
            )
        navigate(action)
    }

}
