package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import co.ltlabs.ltmechanic.databinding.FragmentLineStatusNextMachineAScanMachineBinding
import co.ltlabs.ltmechanic.databinding.PopupScanOptionsBinding
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusNextMachineAScanMachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "NextMachineScanMachine";

class LineStatusNextMachineAScanMachineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val  viewModel: LineStatusNextMachineAScanMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusNextMachineAScanMachineViewModel::class.java)
    }

    private val  machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private var nfcAdapter: NfcAdapter? = null

    private val args: LineStatusNextMachineAScanMachineFragmentArgs by navArgs()

    private lateinit var progressBar: ProgressBar

    private var popupWindow: PopupWindow? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        val binding = FragmentLineStatusNextMachineAScanMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        progressBar = binding.progressBar

        coordinatorLayout = binding.coordinatorLayout

        binding.viewModel = viewModel

//        binding.toolBarTitleTextView.text = getString(R.string.line_status_place, args.station.replace("-A", ""))
        binding.toolBarTitleTextView.text = "${languageJsonObject.getTranslation("PLACE")} ${args.station.replace("-A", "")}"
        binding.placeNoTextView.text = args.station
        binding.toolBarSelectedLineTextView.text = args.mfgLine

        binding.btnScan.setOnClickListener {
            showPopupWindow(binding.root)
        }

        binding.btnCancel3.setOnClickListener {
            activity?.onBackPressed()
        }

        with(languageJsonObject) {
            with(binding) {
                placeTextView.text = getTranslation(placeTextView.text.toString())
                placeTextView8.text = getTranslation(placeTextView8.text.toString())
                btnScan.text = getTranslation(btnScan.text.toString())
                btnCancel3.text = getTranslation(btnCancel3.text.toString())
            }
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

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer { machineStatus ->
            when (machineStatus) {
                MachineStatus.FOUND -> {

                } else -> {
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

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

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

                dismissPopup()

                navigateToScanDetails(
                    args.mfgLineId,
                    args.mfgLine,
                    args.station,
                    machineByNo.id,
                    machineByNo.machine,
                    machineByNo.rfid ?: "",
                    machineByNo.subtype ?: ""
                )

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

                dismissPopup()

                navigateToScanDetails(
                    args.mfgLineId,
                    args.mfgLine,
                    args.station,
                    machineByNo.id,
                    machineByNo.machine,
                    machineByNo.rfid ?: "",
                    machineByNo.subtype ?: ""
                )

                machineViewModel.machineDetailsByMachineNoComplete()

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

        DimUtil.dimBehind(popupWindow)

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

    private fun getPopupWindow(): PopupWindow {

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

    private fun navigateToScanDetails(mfgLineId: Long, mfgLine: String, station: String, machineId: Long, machine: String, rfid: String, subType: String) {
        val action = LineStatusNextMachineAScanMachineFragmentDirections.actionLineStatusNextMachineAScanMachineFragmentToLineStatusNextMachineAScanMachineDetailsFragment(
            mfgLineId,
            mfgLine,
            station,
            machineId,
            machine,
            rfid,
            subType
        )
        navigate(action)
    }

}
