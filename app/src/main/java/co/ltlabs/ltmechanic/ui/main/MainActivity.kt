package co.ltlabs.ltmechanic.ui.main

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.*
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import co.ltlabs.ltmechanic.BaseApplication
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.TopActivity
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.constant.AppConfig.SP_FACTORY_ID
import co.ltlabs.ltmechanic.constant.AppConfig.SP_PASSWORD
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USERNAME
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USER_ROLE
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USER_TOKEN
import co.ltlabs.ltmechanic.constant.TicketType
import co.ltlabs.ltmechanic.constant.TicketTypeCode
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.databinding.*
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.domain.*
import co.ltlabs.ltmechanic.service.DeviceTokenService
import co.ltlabs.ltmechanic.ui.auth.AuthActivity
import co.ltlabs.ltmechanic.ui.dialog.FindMachineBSDialog
import co.ltlabs.ltmechanic.ui.main.lineleader.LineLeaderHomeFragment
import co.ltlabs.ltmechanic.ui.main.main_helper.*
import co.ltlabs.ltmechanic.ui.main.mechanic.MechanicHomeFragment
import co.ltlabs.ltmechanic.ui.setup.LoginCompanyActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.util.nfc.NFCUtil
import co.ltlabs.ltmechanic.util.notification.NotificationClient
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.ReferenceViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.auth0.android.jwt.JWT
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import me.pushy.sdk.Pushy
import okhttp3.HttpUrl
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MainActivity : TopActivity() {

    @Inject
    lateinit var loading: LoadingIndicator

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val referenceViewModel: ReferenceViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReferenceViewModel::class.java)
    }

    private val onAuthenticatedMessage = Emitter.Listener { args ->
        Timber.tag(TAG).d("onAuthenticatedMessage: ${args[0]}")
    }

    private val socketViewModel: SocketViewModel by viewModels { providerFactory }

    val changedViewModel: FireLanguageChangedViewModel by viewModels()
    private val nfcViewModel: NFCViewModel by viewModels()

    private var currentFragmentId = 0
    private var nfcAdapter: NfcAdapter? = null
    private var popupWindow: PopupWindow? = null
    private var action = CREATE_TICKET
    private var actionMachine = ""
    private var sendRequestMessage = ""
    private var sendRequestDate = ""
    private var mfgLinesTemp = mutableListOf<MfgLine>()
    private var nfcEnabled = false
    private val notifications = mutableListOf<Notification>()
    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""
    private var selectedLinesStr = mutableListOf<String>()
    private var selectedAreaStr = mutableListOf<String>()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navGraph: NavGraph
    private lateinit var progressBar: ProgressBar
    private lateinit var socket: Socket
    private lateinit var socketLines: Socket

    lateinit var binding: ActivityMainBinding
    lateinit var navigationView: NavigationView
    lateinit var navController: NavController
    lateinit var telephonyManager: TelephonyManager
    private lateinit var userType: UserType

    var count = 0
    var loggedOut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApplication.isOnMainActivity = true
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        checkAppStore()
        logoutObserve()
        setupReceiver()
        initSocket()
        listenNFCScanning()
        makeStatusBarTransparent()
        setupNavigationDrawer()
        handleJobInBGThread()
        registerDeviceToken()

        MainUtil.googlePlayAvailableInt =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
        MainUtil.googlePlayAvailable = MainUtil.googlePlayAvailableInt == ConnectionResult.SUCCESS

        telephonyManager =
            application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        referenceViewModel.statusId.observe(this) { statusId ->

            if (statusId != null) {
                when (statusId.type) {

                    TicketsStatus.IN_REPAIR -> {
                        StatusIdUtil.RT_IN_REPAIR = statusId.statusId
                    }

                    TicketsStatus.REPAIRED -> {
                        StatusIdUtil.RT_REPAIRED = statusId.statusId
                    }

                    TicketsStatus.CANCELLED -> {

                        if (statusId.module == TicketModule.MAINTENANCE) {
                            StatusIdUtil.MT_CANCELLED = statusId.statusId
                        } else {
                            StatusIdUtil.RT_CANCELLED = statusId.statusId
                        }

                    }

                    TicketsStatus.CLOSED -> {
                        StatusIdUtil.RT_CLOSED = statusId.statusId
                    }

                    TicketsStatus.IN_PROGRESS -> {
                        StatusIdUtil.MT_IN_PROGRESS = statusId.statusId
                    }

                    TicketsStatus.COMPLETED -> {
                        StatusIdUtil.MT_COMPLETED = statusId.statusId
                    }

                }

                referenceViewModel.statusIdComplete()
            }

        }

        machineViewModel.machineDetailsByMachineNo.observe(this) { machine ->
            if (machine != null) {

                var date = ""
                var time = ""
                var millis = ""

                if (sendRequestDate.isNotBlank()) {
                    date = DateUtil.getDate(DateUtil.getDateTimeWithTimeZone(sendRequestDate))
                    time = DateUtil.getTime(DateUtil.getDateTimeWithTimeZone(sendRequestDate))
                    millis =
                        DateUtil.getDateTimeWithTimeZone(sendRequestDate).millisOfDay.toString()
                }

                viewModel.insertToNotificationDatabase(
                    listOf(
                        Notification(
                            id = 0,
                            username = AuthUtil.username,
                            dateTime = Date(),
                            message = "${languageJsonObject.getTranslation(sendRequestMessage)} ${
                                languageJsonObject.getTranslation(
                                    "in"
                                )
                            } ${machine.machine}",
                            type = "send_request",
                            ticketId = "",
                            generatedDate = "$date $time",
                            location = if (machine.station.isNotBlank()) {
                                "${machine.mfgLine} - ${machine.station}"
                            } else {
                                machine.area
                            },
                            machineNo = machine.machine,
                            rfid = machine.rfid ?: "",
                            subType = machine.subtype ?: "",
                            millis = millis
                        )
                    ).asNotificationDatabaseModel().toTypedArray()
                )

                machineViewModel.machineDetailsByMachineNoComplete()
            }
        }

        viewModel.notificationsFromDatebase.observe(this, Observer {
            if (it != null) {
                notifications.clear()
                notifications.addAll(it)
            }
        })

        ticketViewModel.ticket.observe(this) {
            if (it != null) {

                when (action) {

                    CREATE_TICKET -> {
                        val date = DateUtil.formatToDate(it.createdDt)
                        val time = DateUtil.formatToTime(it.createdDt)

                        val message = StrUtil.replaceStr(
                            languageJsonObject.getTranslation("A new repair ticket [] has been submitted.")
                        ).format(it.ticketNo)

                        viewModel.insertToNotificationDatabase(
                            listOf(
                                Notification(
                                    id = 0,
                                    username = AuthUtil.username,
                                    dateTime = Date(),
                                    message = message,
                                    type = CREATE_TICKET,
                                    ticketId = it.id.toString(),
                                    generatedDate = "$date $time",
                                    location = "",
                                    machineNo = "",
                                    rfid = "",
                                    subType = "",
                                    millis = ""
                                )
                            ).asNotificationDatabaseModel().toTypedArray()
                        )
                    }

                    UPDATE_TICKET -> {

                        val date = DateUtil.formatToDate(it.updatedDt)
                        val time = DateUtil.formatToTime(it.updatedDt)

                        val message = StrUtil.replaceStr(
                            languageJsonObject.getTranslation("A repair ticket [] has been repaired.")
                        ).format(it.ticketNo)

                        viewModel.insertToNotificationDatabase(
                            listOf(
                                Notification(
                                    id = 0,
                                    username = AuthUtil.username,
                                    dateTime = Date(),
                                    message = message,
                                    type = UPDATE_TICKET,
                                    ticketId = it.id.toString(),
                                    generatedDate = "$date $time",
                                    location = "",
                                    machineNo = "",
                                    rfid = "",
                                    subType = "",
                                    millis = ""
                                )
                            ).asNotificationDatabaseModel().toTypedArray()
                        )
                    }
                }
            }
        }

        try {
            val jwt = JWT(AuthUtil.token)
            AuthUtil.userId = jwt.getClaim("userId").asInt() ?: 0
            AuthUtil.factoryId = jwt.getClaim("factoryId").asLong() ?: 0L
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }

        progressBar = findViewById(R.id.progress_bar)

        viewModel.insertToNfcDeviceDatabase(false)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        NFCUtil.rfid.observe(this) { rfid ->
            if (rfid != null) {
                if (!nfcViewModel.isObserveOutsideMainActivity) {
                    this.rfid = rfid
                    viewModel.insertToNfcDatabase(rfid, true)
                } else {
                    nfcViewModel.setScanRfid(rfid)
                }
                NFCUtil.clearRfid()
            }
        }

        viewModel.nfcFromDatabase.observe(this) { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {
                machineViewModel.getMachineByRfid(nfc.rfid)
                viewModel.insertToNfcDatabase("", false)
            }
        }

        viewModel.nfcDeviceFromDatabase.observe(this, Observer { nfcDevice ->
            if (nfcDevice != null) {
                nfcEnabled = nfcDevice.enabled
            }
        })

        viewModel.mfgLinesFromDatabase.observe(this) { mfgLines ->
            if (mfgLines.isNotEmpty()) {
                mfgLinesTemp.addAll(mfgLines)
                mfgLines.filter { it.checked ?: false }.forEach {
                    selectedLinesStr.add(it.mfgLine)
                }
            }
        }

        viewModel.mfgAreaFromDatabase.observe(this) { mfgArea ->
            if (mfgArea.isNotEmpty()) {
                selectedAreaStr.clear()
                mfgArea.filter { it.isSelected }.forEach {
                    selectedAreaStr.add(it.id.toString())
                }
            }
        }

        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.title = null
        }

        showNotificationFromSocket()
        onHandleNotification(intent)
    }

    private var ticketFromNotification: Ticket? = null
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        onHandleNotification(intent)

        intent?.let {
            if (nfcEnabled) {
                NFCUtil.resolveIntent(it)
                findMachineBsDialog?.dismiss()
            }

            /**
             * End process with return if not will execute below as both process get data from intent
             * If get intent data of NFC don't need to execute get intent data from notification click
             * Solved tickets LT-1127, LT-1126 and hotfix LT-1463
             */
            return
        }

        /**
         * Check notification click and will direct to screen depend on
         * @param type
         */
        val type = intent?.getStringExtra(NotificationHelper.TICKET_TYPE_EXTRA)
        if (type != null) {
            Timber.tag(TAG).d(type.toString())
            notificationHelper.navigateTo(
                navController, TicketType.fromCodeToType(type), ticketFromNotification
            )
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
            return
        }
        if (currentFragmentId == R.id.mechanicHomeFragment || currentFragmentId == R.id.lineLeaderHomeFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            lifecycleScope.launchWhenCreated {
                delay(2000)
                doubleBackToExitPressedOnce = false
            }
            return
        }

        navigateToOtherFragmentsByBackButton {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        // Init broadcast
        val filter = IntentFilter(NotificationClient.SEND_BROADCAST)
        registerReceiver(broadcastReceiver, filter)
        socketViewModel.refreshDashboard()

        if (ENV_TYPE == "STG_PROD" || ENV_TYPE == "DEV") {
            socketLines.connect().on(Socket.EVENT_RECONNECT_ATTEMPT) {
                socketLines.emit("authentication", JSONObject().put("token", AuthUtil.token))

            }
        } else {
            socketLines.connect().on(Socket.EVENT_CONNECT) {
                socketLines.emit("authentication", JSONObject().put("token", AuthUtil.token))

            }
        }

        if (ENV_TYPE == "STG_PROD" || ENV_TYPE == "DEV") {
            socket.connect().on(Socket.EVENT_RECONNECT_ATTEMPT) {
                socket.emit("authentication", JSONObject().put("token", AuthUtil.token))

            }
        } else {
            socket.connect().on(Socket.EVENT_CONNECT) {
                socket.emit("authentication", JSONObject().put("token", AuthUtil.token))

            }
        }

        socket.on(
            "authenticated", onAuthenticatedMessage
        )

        socketLines.on(
            "authenticated", onAuthenticatedMessage
        )

        socket.on(Socket.EVENT_DISCONNECT) {
            socket.connect()
        }

        socketLines.on(Socket.EVENT_DISCONNECT) {
            socketLines.connect()
        }

        socketLines.on("request", onSendRequestMessage)

        socket.on("new_ticket", onNewTicketMessage)
        socket.on("updated_ticket", onUpdateTicketMessage)
        socket.on("logout", onLogoutMessage)

        nfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }

    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        nfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onNoConnection() {
        dismissPopup()
        //showPopupWindow(findViewById(R.id.relativeLayout), showSignalNotificationWindow())
    }

    override fun onWifiStateDisabled(isMobileDataEnabled: Boolean) {
        progressBar.showProgressBar(false)
        if (!isMobileDataEnabled) {
            showPopupWindow(findViewById(R.id.relativeLayout), showSignalNotificationWindow())
        }
    }

    override fun onWifiStateEnabling() {
        progressBar.showProgressBar(true)
    }

    override fun onWifiStateEnabled(isDataEnabled: Boolean) {
        dismissPopup()
        progressBar.showProgressBar(false)
        if (!isDataEnabled) {
            showPopupWindow(findViewById(R.id.relativeLayout), showServerCommunicationProblem())
        }
    }

    override fun onWifiStateDisabling() {
        progressBar.showProgressBar(true)
    }

    override fun onConnectionPoor() {
        dismissPopup()
        //showPopupWindow(findViewById(R.id.relativeLayout), showPoorSignalNotificationWindow())
    }

    override fun onConnectionStrong() {
        dismissPopup()
    }

    override fun onNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.off("new_ticket", onNewTicketMessage)
        socket.off("updated_ticket", onUpdateTicketMessage)
        socket.off("logout", onLogoutMessage)
        socketLines.off("request", onSendRequestMessage)
        socket.disconnect()
        socket.close()

        socketViewModel.socketTicket.removeObserver(ticketObserveForever)
    }

    private fun registerDeviceToken() {
        val intent = Intent(this, DeviceTokenService::class.java)
        startService(intent)
    }

    private fun handleJobInBGThread() {
        lifecycleScope.launch(Dispatchers.IO) {
            userType = UserType.convertToType(AuthUtil.role)
            Pushy.toggleWifiPolicyCompliance(true, this@MainActivity)

            if (AuthUtil.username.isNotEmpty() && AuthUtil.password.isNotEmpty()) {
                SharePrefUtil.set(SP_USERNAME, AuthUtil.username)
                SharePrefUtil.set(SP_PASSWORD, AuthUtil.password)
                SharePrefUtil.set(SP_USER_ROLE, AuthUtil.role)
                SharePrefUtil.set(SP_USER_TOKEN, AuthUtil.token)
            }
        }
    }

    private var isNavigateOfFCM: Boolean = false
    private fun onHandleNotification(data: Intent?) {
        NotificationClient.stopAudio()
        data ?: return
        val action = data.getStringExtra(AppConfig.EXTRA_ACTION)
        val ticketNo = data.getStringExtra(AppConfig.EXTRA_REFERENCE)
        if (TicketType.fromCodeToType(action) is TicketType.Cancelled) return

        /**
         * Disabled for now by Ticket GALTM-707
         */
        // Check if Notification is CO Type
//        if (action?.contains("-CO") == true) {
//            isNavigateOfFCM = true
//            val bundle = bundleOf(
//                "isOpenedByNotify" to true, "coNo" to ticketNo
//            )
//            if (currentFragmentId == R.id.readyCOFragment || currentFragmentId == R.id.prepareCOFragment) {
//                Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show()
//
//            } else {
//                findNavController(R.id.nav_host_fragment).navigate(
//                    R.id.action_global_to_ChangeOverFragment, bundle
//                )
//            }
//            return
//        }

        lifecycleScope.launchWhenCreated {
            if (ticketNo != null && ticketNo.isNotEmpty()) {
                socketViewModel.getTicketDetailsByTicketNo(ticketNo, true)
            }
        }

        lifecycleScope.launchWhenCreated {
            socketViewModel.fcmTicket.collectLatest { ticket ->
                isNavigateOfFCM = true
                NotificationClient.navigateTo(
                    findNavController(R.id.nav_host_fragment),
                    TicketType.fromCodeToType(action),
                    ticket
                )
            }
        }
    }

    private fun listenNFCScanning() {
        lifecycleScope.launchWhenCreated {
            nfcViewModel.nfcAction.collectLatest {
                if (it != NFCAction.NONE) showFindMachineDialog()
            }
        }
    }

    private fun setupNavigationDrawer() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val list = if (AuthUtil.role == UserType.LINE_LEADER) listOfLineLeaderFragmentId()
        else listOfMechanicFragmentId()
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(
            list, drawerLayout
        )

        val navHostFragment = findNavController(R.id.nav_host_fragment)
        val graphInflater = navHostFragment.navInflater
        navGraph = graphInflater.inflate(R.navigation.nav_graph)
        navController = findNavController(R.id.nav_host_fragment)
        if (AuthUtil.role == UserType.LINE_LEADER) {
            navGraph.startDestination = R.id.lineLeaderHomeFragment
        } else {
            navGraph.startDestination = R.id.mechanicHomeFragment
        }
        navController.graph = navGraph

        setupActionBarWithNavController(
            navController, appBarConfiguration
        )
        navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Timber.tag(TAG_FRAGMENT_NAME).d(destination.label.toString())
            currentFragmentId = destination.id
            if (isOnMainPages()) {
                toolbar.setNavigationIcon(R.drawable.ic_menu_white)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                toolbar.setNavigationIcon(R.drawable.ic_back_white)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        toolbar.setNavigationOnClickListener {
            if (isOnMainPages()) drawerLayout.openDrawer(GravityCompat.START)
            else navigateToOtherFragmentsByBackButton {
                findNavController(R.id.nav_host_fragment).popBackStack()
            }
        }

        var navItemClick: MenuItem? = null
        navigationView.setNavigationItemSelectedListener {
            if (currentFragmentId != it.itemId) {
                navItemClick = it
                drawerLayout.closeDrawers()
            }
            true
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerClosed(drawerView: View) {
                when (navItemClick?.itemId) {
                    R.id.query_machine -> {
                        actionMachine = QUERY_MACHINE
                        nfcViewModel.setNFCAction(NFCAction.QUERY_MACHINE)
                    }
                    R.id.replace_machine -> {
                        actionMachine = REPLACE_MACHINE
                        nfcViewModel.setNFCAction(NFCAction.REPLACE_MACHINE)
                    }
                    R.id.create_ticket -> {
                        actionMachine = CREATE_TICKET
                        nfcViewModel.setNFCAction(NFCAction.CREATE_TICKET)
                    }
                    R.id.move_machine -> {
                        actionMachine = MOVE_MACHINE
                        nfcViewModel.setNFCAction(NFCAction.MOVE_MACHINE)
                    }
                    R.id.send_request -> {
                        actionMachine = SEND_REQUEST
                        nfcViewModel.setNFCAction(NFCAction.SEND_REQUEST)
                    }
                    else -> {
                        navItemClick?.let { item ->
                            navController.navDestinationSelected(item)
                        }
                    }
                }
                navItemClick = null
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }

        })
        addItemToDrawer()
        handleMachineObserve()
        setupListener()
    }

    private fun setupListener() {
        binding.logout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showErrorPopupWindow(): PopupWindow {

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupLineLeaderErrorPopupNotOnLineBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                labelMachineNotAssigned.text =
                    getTranslation(labelMachineNotAssigned.text.toString())
                labelWhatDoYouWant.text = getTranslation(labelWhatDoYouWant.text.toString())
                btnScanAgain.text = getTranslation(btnScanAgain.text.toString())
                btnCancelLineSetup3.text = getTranslation(btnCancelLineSetup3.text.toString())
            }
        }
        // End translation

        binding.btnScanAgain.setOnClickListener {
            dismissPopup()
            findMachineBsDialog?.dismiss()
            showFindMachineDialog()
        }

        binding.btnCancelLineSetup3.setOnClickListener {
            findMachineBsDialog?.dismiss()
            dismissPopup()
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private var findMachineBsDialog: FindMachineBSDialog? = null
    private var rfid = ""
    private fun showFindMachineDialog() {
        if (findMachineBsDialog == null) {
            findMachineBsDialog = FindMachineBSDialog()
            viewModel.insertToNfcDeviceDatabase(true)
        }

        if (findMachineBsDialog?.isAdded == false) findMachineBsDialog?.show(
            supportFragmentManager,
            findMachineBsDialog?.tag
        )

        findMachineBsDialog?.onDismissListener {
            findMachineBsDialog = null
        }

        findMachineBsDialog?.onDoneListener {
            machineViewModel.getMachineByMachineNo(it)
        }
    }

    private fun isOnMainPages() =
        listOfLineLeaderFragmentId().contains(currentFragmentId) || listOfMechanicFragmentId().contains(
            currentFragmentId
        )

    private fun handleMachineObserve() {
        machineViewModel.machineStatus.observe(this) {
            if (it == MachineStatus.NOT_FOUND) {
                dismissPopup()
                if (MachineUtil.message.isNotBlank()) {
                    val msg = languageJsonObject.getTranslation(
                        MachineUtil.message.replace(".", "")
                    )
                    showSnackBar(binding.root, msg)
                } else {
                    val msg = languageJsonObject.getTranslation(
                        "Machine number not found"
                    )
                    showSnackBar(binding.root, msg)
                }
            }
        }

        machineViewModel.status.observe(this) { status ->
            when (status) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    dismissPopup()
                    binding.progressBar.showProgressBar(false)
                }
            }
        }

        machineViewModel.machineDetailsByMachineNo.observe(this) { machine ->
            if (machine != null) {
                navigateToByMachine(machine)
            }
        }

        machineViewModel.machineDetailsByRfid.observe(this) { machine ->
            if (machine != null) {
                navigateToByMachine(machine)
            }
        }
        createTicket()
    }

    private fun navigateToByMachine(machine: Machine) {
        val mfgLineId = machine.mfgLineId ?: 0
        viewModel.insertToNfcDeviceDatabase(false)
        when (nfcViewModel.getNCFAction()) {
            NFCAction.CREATE_TICKET -> {
                loadedMachine = machine.machine
                loadedMachineId = machine.id
                loadedMachineMfgLine = machine.mfgLine ?: ""
                loadedMachineStation = machine.station
                if (MachineUtil.machineFound) {
                    if (selectedLinesStr.any { it == machine.mfgLine } || selectedAreaStr.any { it == machine.areaId.toString() }) {
                        if (machine.hasOpenTicket) {
                            dismissPopup()
                            showSnackBar(
                                binding.root, languageJsonObject.getTranslation(
                                    "This machine has an active ticket"
                                )
                            )
                        } else {
                            ticketViewModel.getMachineProblems(machine.id)
                        }

                    } else {

                        val dm = DisplayMetrics()
                        windowManager?.defaultDisplay?.getMetrics(dm)

                        val width = (dm.widthPixels * .9).toInt()
                        val height = (dm.heightPixels * .5).toInt()

                        dismissPopup()
                        popupWindow = showErrorPopupWindow()
                        popupWindow?.isOutsideTouchable = true
                        popupWindow?.isFocusable = true
                        popupWindow?.update(0, 0, width, height)
                        popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))


                    }
                } else {
                    dismissPopup()
                }
            }
            NFCAction.SEND_REQUEST -> {
                navigateToSendRequest(
                    machine.id,
                    machine.machine,
                    machine.rfid ?: "",
                    machine.subtype ?: "",
                    (machine.mfgLineId ?: 0).toLong()
                )
            }
            NFCAction.MOVE_MACHINE -> {
                navigateToMoveMachine(
                    machine.id,
                    machine.machine,
                    machine.rfid ?: "",
                    machine.subtype ?: "",
                    machine.area,
                    machine.station,
                    machine.mfgLine ?: "",
                    machine.building,
                    machine.buildingId
                )
            }
            NFCAction.REPLACE_MACHINE -> {
                MachineUtil.machineNo = machine.machine
                MachineUtil.machineArea = machine.area
                MachineUtil.machineLocation = if (machine.area.toLowerCase().contains("prod")) {
                    "${machine.mfgLine} - ${machine.station}"
                } else {
                    machine.area
                }
                MachineUtil.machineHasOpenTickets = machine.hasOpenTicket

                if (machine.station.isNotBlank()) {
                    navigateToReplace(
                        mfgLineId,
                        machine.mfgLine ?: "",
                        machine.station,
                        machine.machine,
                        machine.id
                    )
                } else {
                    val msg = languageJsonObject.getTranslation("Machine is not in Production Line")
                    showSnackBar(binding.root, msg)
                }
            }
            NFCAction.REPLACE_MACHINE_CONFIRM -> {
                with(nfcViewModel) {
                    navigateReplaceConfirmMachine(
                        this.mfgLineId,
                        mfgLine,
                        machineId,
                        this.machine,
                        station,
                        machine.id,
                        machine.machine,
                        machine.station
                    )
                }

            }
            NFCAction.QUERY_MACHINE -> {
                navigateToQueryMachine(machine.id, machine.machine, machine.rfid)
            }
            NFCAction.ADD_MACHINE -> {
                navigateToAddMachine(
                    nfcViewModel.needMfgLineId,
                    nfcViewModel.needMfgLine,
                    nfcViewModel.needStation,
                    rfid,
                    machine.machine
                )
                rfid = ""
            }
        }
        nfcViewModel.setNFCAction(NFCAction.NONE)
        machineViewModel.machineDetailsByRfidComplete()
    }

    private fun createTicket() {
        ticketViewModel.commonProblems.observe(this, Observer {
            if (it != null) {
                dismissPopup()
                if (it.isNotEmpty()) {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine,
                        it.size.toLong(),
                        "home"
                    )
                } else {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine,
                        0,
                        "home"
                    )
                }

                ticketViewModel.commonProblemsComplete()
            }
        })
    }

    private fun navigateToOtherFragmentsByBackButton(onElse: () -> Unit) {
        if (isNavigateOfFCM) {
            if (userType is UserType.LineLeader) {
                navController.popBackStack(R.id.lineLeaderHomeFragment, false)
                return
            }
            navController.popBackStack(R.id.mechanicHomeFragment, false)
            isNavigateOfFCM = false
            return
        }

        if (currentFragmentId == R.id.createTicketPreviewFragment) {
            if (userType is UserType.LineLeader) {
                navController.navigate(R.id.lineLeaderHomeFragment)
                return
            }
            navController.navigate(R.id.mechanicHomeFragment)
            return
        }

        if (currentFragmentId == R.id.mechanicInRepairTicketsChecklistFragment2) {
            navController.popBackStack(R.id.mechanicInRepairTicketsFragment, false)
            return
        }

        if (currentFragmentId == R.id.mechanicReportedTicketsChecklistFragment) {
            navController.popBackStack(R.id.mechanicReportedTicketsFragment, false)
            return
        }

        onElse.invoke()
    }

    private val onNewTicketMessage = Emitter.Listener { args ->
        // To refresh dashboard [MechanicHomeFragment] or [LineLeaderHomeFragment] once created new ticket
        if (currentFragmentId == R.id.mechanicHomeFragment || currentFragmentId == R.id.lineLeaderHomeFragment) {
            socketViewModel.refreshDashboard()
            // vibration after get Maintenance notification
            doVibration()
        }
        val jsonObject = JsonParser.parseString(args[0].toString()).asJsonObject
        try {
            val mfgLineId = jsonObject["mfgLineId"].toString().replace("\"", "").toLong()

            val ticketNo = jsonObject["ticketNo"].toString().replace("\"", "")
            if (mfgLinesTemp.any { it.mfgLineId == mfgLineId }) {

                if (notifications.size >= 20) {
                    val lastNotification =
                        notifications.filter { it.username == AuthUtil.username }[notifications.size - 1]
                    viewModel.deleteFromNotificationDatabase(lastNotification.asNotificationObjDatabaseModel())
                }

                action = CREATE_TICKET

                ticketViewModel.getTicketDetailsByTicketNo(ticketNo)

                // hit this to get ticket detail and bind into notification content
                socketViewModel.getTicketDetailsByTicketNo(ticketNo)

            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
    }

    private fun doVibration() {
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(500)
        }
    }

    private var ticketStatusFromSocket = ""
    private var reopenTicketBy = ""
    private val onUpdateTicketMessage = Emitter.Listener { args ->
        try {
            val jsonObject = JsonParser.parseString(args[0].toString()).asJsonObject

            val status = jsonObject["status"].toString().replace("\"", "")
            ticketStatusFromSocket = status
            reopenTicketBy = jsonObject["user"].toString().replace("\"", "")
            val ticketNo = jsonObject["ticketNo"].toString().replace("\"", "")

            if (status == TicketTypeCode.REPAIRED) {
                if (notifications.size >= 20) {
                    val lastNotification =
                        notifications.filter { it.username == AuthUtil.username }[notifications.size - 1]
                    viewModel.deleteFromNotificationDatabase(lastNotification.asNotificationObjDatabaseModel())
                }
                ticketViewModel.getTicketDetailsByTicketNo(ticketNo)
            }

            action = UPDATE_TICKET
            // hit this to get ticket detail and bind into notification content
            if (status == TicketTypeCode.REPAIRED || status == TicketTypeCode.IN_REPAIR || status == TicketTypeCode.RE_OPEN) {
                socketViewModel.getTicketDetailsByTicketNo(ticketNo)
            }

            /**
             * To refresh dashboard on [MechanicHomeFragment] or [LineLeaderHomeFragment] once get update of ticket
             */
            if (currentFragmentId == R.id.mechanicHomeFragment || currentFragmentId == R.id.lineLeaderHomeFragment) {
                socketViewModel.refreshDashboard()
                doVibration()
            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
    }

    private val onSendRequestMessage = Emitter.Listener { args ->

        try {
            val jsonObject = JsonParser.parseString(args[0].toString()).asJsonObject

            val machineId = jsonObject["machineId"].toString().replace("\"", "")
            val requestMsg = jsonObject["requestMsg"].toString().replace("\"", "")
            val date = jsonObject["date"].toString().replace("\"", "")

            val mfgLineId = jsonObject["mfgLineId"].toString().replace("\"", "").toInt()

            sendRequestMessage = requestMsg
            sendRequestDate = date


            // TODO uncomment when ready
            if (AuthUtil.role.lowercase(Locale.getDefault()).contains(UserType.MECHANIC)) {
                if (notifications.size >= 20) {
                    val lastNotification =
                        notifications.filter { it.username == AuthUtil.username }[notifications.size - 1]
                    viewModel.deleteFromNotificationDatabase(lastNotification.asNotificationObjDatabaseModel())
                }

                machineViewModel.getMachineById(machineId.toInt())

            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }

    }

    private val onLogoutMessage = Emitter.Listener { _ ->
        loggedOut = true

        //Ticket : 16492
        // Remove session + disconnect socket + unregister receiver
        SharePrefUtil.removeValue(SP_PASSWORD)
        SharePrefUtil.removeValue(SP_USER_TOKEN)
        if (socket.connected()) socket.disconnect()
        if (socketLines.connected()) socketLines.disconnect()

        try {
            lifecycleScope.launch {
                dismissPopup()

                viewModel.insertToAuthDetailsDatabase(
                    arrayOf(
                        DatabaseAuthDetails(
                            username = "", role = "", token = "", loggedIn = false, tokenP = ""
                        )
                    )
                )

                val dm = DisplayMetrics()
                this@MainActivity.windowManager.defaultDisplay.getMetrics(dm)
                val width = dm.widthPixels
                val height = dm.heightPixels


                dismissPopup()
                popupWindow = showPopupWindow()
                popupWindow?.isOutsideTouchable = false
                popupWindow?.isFocusable = false

                popupWindow?.update(0, 0, width, height)
                findViewById<RelativeLayout>(R.id.relativeLayout).post {
                    popupWindow?.showAtLocation(
                        findViewById(R.id.relativeLayout), Gravity.CENTER, 0, -25
                    )
                    //popupLogoutWindow?.showAtLocation(findViewById(R.id.relativeLayout), Gravity.CENTER, 0, -25)
                }

                DimUtil.dimBehind(popupWindow)


//                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                //DimUtil.dimBehind(popupLogoutWindow)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
    }

    private val ticketObserveForever = Observer<Ticket> {
        if (it != null) {
            ticketFromNotification = it
            when (action) {
                CREATE_TICKET -> {
                    // send notification when get new ticket
                    val ticketTitle = "${languageJsonObject.getTranslation("Ticket No")}:"
                    val machineTitle = "${languageJsonObject.getTranslation("Machine")}:"
                    val problemTitle = "${languageJsonObject.getTranslation("Problem")}:"
                    val content = StringBuffer()
                    content.append("$ticketTitle ${it.ticketNo}\n")
                    content.append("$machineTitle ${it.machineNo}\n")
                    content.append("$problemTitle ${it.problem}")
                    notificationHelper.sendNotification(
                        applicationContext,
                        content.toString(),
                        TicketType.fromCodeToType(TicketType.REPORTED)
                    )
                }

                UPDATE_TICKET -> {
                    // Notify from socket when ticket is updated
                    if (ticketStatusFromSocket.isNotEmpty()) {
                        val ticketTitle = "${languageJsonObject.getTranslation("Ticket No")}:"
                        val machineTitle = "${languageJsonObject.getTranslation("Machine")}:"
                        val problemTitle = "${languageJsonObject.getTranslation("Problem")}:"
                        val content = StringBuffer()
                        content.append("$ticketTitle ${it.ticketNo}\n")
                        content.append("$machineTitle ${it.machineNo}\n")
                        content.append("$problemTitle ${it.problem}\n")

                        when (ticketStatusFromSocket) {
                            TicketTypeCode.IN_REPAIR -> {
                                val ticketType = TicketType.fromCodeToType(TicketType.IN_REPAIR)
                                val mechanicTitle =
                                    "${languageJsonObject.getTranslation("Mechanic")}:"
                                content.append("$mechanicTitle ${it.grabbedBy}")
                                notificationHelper.sendNotification(
                                    applicationContext, content.toString(), ticketType
                                )
                            }
                            TicketTypeCode.REPAIRED -> {
                                val ticketType = TicketType.fromCodeToType(TicketType.REPAIRED)
                                val solutionTitle =
                                    "${languageJsonObject.getTranslation("Solution")}:"
                                val durationTitle =
                                    "${languageJsonObject.getTranslation("Duration Time")}:"
                                val mechanicTitle =
                                    "${languageJsonObject.getTranslation("Mechanic")}:"
                                content.append("$solutionTitle ${it.solution}\n")
                                content.append("$durationTitle ${it.inrepairDuration}\n")
                                content.append("$mechanicTitle ${it.repairedBy}")
                                notificationHelper.sendNotification(
                                    applicationContext, content.toString(), ticketType
                                )
                            }

                            TicketTypeCode.RE_OPEN -> {
                                val ticketType = TicketType.fromCodeToType(TicketType.REOPEN)
                                val mechanicTitle =
                                    "${languageJsonObject.getTranslation("Re-opened by")}:"
                                content.append("$mechanicTitle $reopenTicketBy")
                                notificationHelper.sendNotification(
                                    applicationContext, content.toString(), ticketType
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showNotificationFromSocket() {
        socketViewModel.socketTicket.observeForever(ticketObserveForever)
    }

    private fun showPopupWindow(): PopupWindow {

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupLogoutMessageBinding.inflate(inflater)

        binding.textView3.text =
            languageJsonObject.getTranslation(binding.textView3.text.toString())

        binding.btnOk.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun showPopupWindow(view: View, popupWindowType: PopupWindow) {

        val dm = DisplayMetrics()
        this.windowManager?.defaultDisplay?.getMetrics(dm)

        dismissPopup()
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = true

        popupWindow?.setBackgroundDrawable(BitmapDrawable(null, ""))

        popupWindow?.isFocusable = false
        view.post {
            popupWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
        }

        DimUtil.dimBehind(popupWindow)
    }

    private fun showSignalNotificationWindow(): PopupWindow {

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupSignalNotificationBinding.inflate(inflater)

        binding.btnMachineHistory.text =
            languageJsonObject.getTranslation(binding.btnMachineHistory.text.toString())

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun showServerCommunicationProblem(): PopupWindow {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ServerCommunicationNotificationBinding.inflate(inflater)
        binding.btnMachineHistory.text =
            languageJsonObject.getTranslation(binding.btnMachineHistory.text.toString())
        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun showPoorSignalNotificationWindow(): PopupWindow {

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupPoorSignalNotificationBinding.inflate(inflater)

        binding.btnMachineHistory.text =
            languageJsonObject.getTranslation(binding.btnMachineHistory.text.toString())

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun initSocket() {
        AppModule.host = HttpUrl.parse(SettingsUtil.hostname)?.host().toString()
        AppModule.scheme = SettingsUtil.hostname

        try {
            val opts = IO.Options()
            opts.reconnection = true
            opts.transports = mutableListOf("websocket").toTypedArray()
            opts.secure = true
            opts.query = "token=${AuthUtil.token}"
            socket = IO.socket("${AppModule.scheme}://${AppModule.host}", opts)
            socketLines = IO.socket("${AppModule.scheme}://${AppModule.host}", opts)

        } catch (e: Exception) {
            Timber.tag(TAG).d(e)
        }
    }

    private fun logout(logoutType: Int) {
        loading.show(this)
        viewModel.insertToAuthDetailsDatabase(
            arrayOf(
                DatabaseAuthDetails(
                    username = "", role = "", token = "", loggedIn = false, tokenP = ""
                )
            )
        )
        SharePrefUtil.removeValue(SP_PASSWORD)
        SharePrefUtil.removeValue(SP_USER_TOKEN)
        SharePrefUtil.removeValue(SP_FACTORY_ID)
        notificationHelper.removeNotification(this)
        NotificationClient.removeNotification(this)
        BaseApplication.isOnMainActivity = false
        if (logoutType == LOGOUT_COMPANY) {
            AppConfig.COMPANY_CODE = ""
            SharePrefUtil.removeValue(SP_USERNAME)
            SharePrefUtil.removeValue(AppConfig.SP_REMEMBER_PWD)
            SharePrefUtil.removeValue(AppConfig.SP_COMPANY_CODE)
        }
        val intent = Intent().setClass(this, LoginCompanyActivity::class.java)
        startActivity(intent)
        finish()
        loading.dismiss()
    }

    private var logoutType: Int = LOGOUT
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        val binding = DialogLogoutBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        binding.apply {
            dialogTitle.text = languageJsonObject.getTranslation("Logout the System")
            radioLogout.text = languageJsonObject.getTranslation("Logout")
            radioLogoutCompany.text = languageJsonObject.getTranslation("Logout Company")
            btnCancel.text = languageJsonObject.getTranslation("Cancel")
            btnOk.text = languageJsonObject.getTranslation("OK")
        }

        val dialog = builder.create()
        dialog.show()
        binding.btnCancel.setOnClickListener { dialog.dismiss() }
        binding.btnOk.setOnClickListener {
            logoutType = if (binding.radioLogout.isChecked) LOGOUT
            else LOGOUT_COMPANY
            viewModel.logout()
            dialog.dismiss()
        }
    }

    private fun logoutObserve() {
        lifecycleScope.launchWhenCreated {
            viewModel.logout.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> loading.show(this@MainActivity)
                    Resource.Status.ERROR -> loading.dismiss()
                    else -> {
                        loading.dismiss()
                        withContext(Dispatchers.IO) {
                            Pushy.unregister(applicationContext)
                        }
                        logout(logoutType)
                    }
                }
            }
        }
    }

    private fun checkAppStore() {
        viewModel.getAppInfo()
        viewModel.appStoreStatus.observe(this, Observer { appStore ->
            if (appStore != null) {
                AppConfig.APK_LINK = appStore.downloadLink
                AppConfig.NEW_VERSION = appStore.latestVersion

                //notify to fragment to visible rocket icon
                try {
                    val navHostFragment = nav_host_fragment as NavHostFragment
                    val frag = navHostFragment.childFragmentManager.fragments[0]
                    if (frag is MechanicHomeFragment) frag.showRocketIcon()
                    if (frag is LineLeaderHomeFragment) frag.showRocketIcon()
                } catch (e: java.lang.Exception) {
                }
            }
        })
    }

    /**
     * Listening to refresh dashboard while getting notification from FCM
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val isGettingMsg = intent.getBooleanExtra(AppConfig.EXTRA_GET_NOTIFY, false)
            if (action == NotificationClient.SEND_BROADCAST && isGettingMsg) {
                socketViewModel.refreshDashboard()
            }
        }
    }

    companion object {

        private const val TAG = "MainActivity"
        private const val TAG_FRAGMENT_NAME = "FragmentName"

        private const val LOGOUT = 1
        private const val LOGOUT_COMPANY = 2

    }
}
