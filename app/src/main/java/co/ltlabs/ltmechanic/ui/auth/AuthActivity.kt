package co.ltlabs.ltmechanic.ui.auth

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.TopActivity
import co.ltlabs.ltmechanic.constant.AppConfig.SP_PASSWORD
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USERNAME
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USER_ROLE
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USER_TOKEN
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.databinding.PopupPoorSignalNotificationBinding
import co.ltlabs.ltmechanic.databinding.PopupSignalNotificationBinding
import co.ltlabs.ltmechanic.ui.main.MainActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import java.lang.reflect.Method
import javax.inject.Inject

private const val TAG = "AuthActivity";

class AuthActivity : TopActivity() {

    private lateinit var viewModel: AuthViewModel

    private val viewModelJob = SupervisorJob()
    private val IOScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private val mainScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var progressBar: ProgressBar
    lateinit var usernameEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var btnLogin: Button
    lateinit var appVersion: TextView

    lateinit var titleWelcomeText: TextView
    lateinit var titleLtMechanic: TextView
    lateinit var labelUsername: TextView
    lateinit var labelPassword: TextView

    private var wifiDisabled = false

    private var popupWindow: PopupWindow? = null

    lateinit var telephonyManager: TelephonyManager

    var switching = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //println("==== BASE_URL : ${AppConfig.BASE_URL}")

        telephonyManager =
            application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager


        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.title = null
        }

        titleWelcomeText = findViewById(R.id.titleTextViewLL3)
        titleLtMechanic = findViewById(R.id.titleTextViewLL2)
        labelUsername = findViewById(R.id.usernameLabel2)
        labelPassword = findViewById(R.id.passwordLabel)

        titleWelcomeText.text = languageJsonObject.getTranslation(
            titleWelcomeText.text.toString()
        )

        titleLtMechanic.text = languageJsonObject.getTranslation(
            titleLtMechanic.text.toString()
        )

        labelUsername.text = languageJsonObject.getTranslation(
            labelUsername.text.toString()
        )

        labelPassword.text = languageJsonObject.getTranslation(
            labelPassword.text.toString()
        )



        Log.d(TAG, "onCreate: isOnline(): ${isOnline()}")

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        val username = SharePrefUtil.getString(SP_USERNAME, "")
        val password = SharePrefUtil.getString(SP_PASSWORD, "")
        val userRole = SharePrefUtil.getString(SP_USER_ROLE, "")
        val userToken = SharePrefUtil.getString(SP_USER_TOKEN, "")

        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            navigateToDashboard(username, password, userRole.toString(), userToken.toString())
        }

        viewModel.loadConfigFile(this)

        viewModel.languageFromDatabase.observe(this, Observer {
            Log.d(TAG, "onCreate: LanguageUtil.selectedLanguage: $it")
            if (it != null) {
                if (it.isNotEmpty()) {
//                    LanguageUtil.selectedLanguage = it[0].language
                    LanguageUtil.selectedLanguage =
                        "en" //this will force not update : it related to AuthViewModel : loadTranslationFile
                    viewModel.loadTranslationFile(this)
                }
            }
        })

        //viewModel.getAppInfo()

        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        progressBar = findViewById(R.id.progress_bar)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        btnLogin = findViewById(R.id.btnLogin)
        appVersion = findViewById(R.id.version)

        val versionName = BuildConfig.VERSION_NAME
        appVersion.text = "v$versionName"

        progressBar.showProgressBar(false)

//        usernameEditText.setText("lineleader1")
//        passwordEditText.setText("lineleader")

//        usernameEditText.setText("mechanic1")
//        passwordEditText.setText("mechanic")

        btnLogin.text = languageJsonObject.getTranslation(
            btnLogin.text.toString()
        )

        viewModel.transLationFileLoaded.observe(this, Observer {

            if (it != null && it) {
                titleWelcomeText.text = languageJsonObject.getTranslation(
                    titleWelcomeText.text.toString()
                )

                titleLtMechanic.text = languageJsonObject.getTranslation(
                    titleLtMechanic.text.toString()
                )

                labelUsername.text = languageJsonObject.getTranslation(
                    labelUsername.text.toString()
                )

                labelPassword.text = languageJsonObject.getTranslation(
                    labelPassword.text.toString()
                )
                btnLogin.text = languageJsonObject.getTranslation(
                    btnLogin.text.toString()
                )
            }
        })


        btnLogin.setOnClickListener {
            this.hideKeyboard()

            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {

                // TODO Comment when api fixed
//                AuthUtil.role = if (username.contains("lineleader")) {
//                    "lineleader"
//                } else {
//                    "mechanic"
//                }

                progressBar.showProgressBar(true)
                viewModel.login(username, password)
            } else {
                coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("You have entered Incorrect username and password"))
            }
        }

        viewModel.loginDetailsFromDatabase.observe(this, Observer { loginDetails ->
            if (loginDetails != null) {
                if (loginDetails.isNotEmpty()) {

                    val loginDetail = loginDetails[0]

                    Log.d(TAG, "onCreate: loginDetail.loggedIn: ${loginDetail.loggedIn}")

                    if (loginDetail.loggedIn) {
                        progressBar.showProgressBar(true)

                        AuthUtil.username = loginDetail.username
                        AuthUtil.role = loginDetail.role
                        AuthUtil.token = loginDetail.token

//                        val encoded = Crypto.encryptAndEncode(loginDetail.tokenP)
                        AuthUtil.password = loginDetail.tokenP

                        //viewModel.checkTokenValidity(AuthUtil.token)

                    }
                }
            }
        })

        // DISABLE AUTO LOGIN
        /*viewModel.callStatus.observe(this, Observer {
            Log.d(TAG, "onCreate: it: $it")
            if (it != null ){
                when(it) {
                    ApiCallStatus.SUCESS -> {

                        Log.d(TAG, "onCreate: pasok")
//                        progressBar.showProgressBar(false)

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

                    ApiCallStatus.TOKEN_EXPIRED -> {
//                        progressBar.showProgressBar(false)
                    }
                }
                viewModel.callStatusComplete()
            }
        })*/

        viewModel.authDetails.observe(this, Observer {
            if (it != null) {

                if (it.loginSucces) {

                    /*AuthUtil.token = it.token
                    AuthUtil.role = it.role
                    AuthUtil.username = it.username
                    viewModel.insertToAuthDetailsDatabase(
                        arrayOf(
                            DatabaseAuthDetails(
                                username = it.username,
                                role = it.role,
                                token = it.token,
                                loggedIn = true,
                                tokenP = AuthUtil.password
                            )
                        )
                    )*/
//                    startActivity(Intent(this, MainActivity::class.java))

                    navigateToDashboard(it.username, AuthUtil.password, it.role, it.token)
                    //finish()
                    progressBar.showProgressBar(false)

                }

                viewModel.authDetailsComplete()
            }
        })

//        viewModel.status.observe(this, Observer {
//            when (it) {
//                ApiStatus.LOADING -> {
//                    progressBar.showProgressBar(true)
//                }
//                else -> {
//                    progressBar.showProgressBar(false)
//                }
//            }
//        })

        viewModel.loginStatus.observe(this, Observer {
            when (it) {
                LoginStatus.SUCCESS -> {

                }

                LoginStatus.USERNAME_INVALID -> {
                    usernameEditText.text.clear()
                    passwordEditText.text.clear()
                    progressBar.showProgressBar(false)
                    coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("You have entered Incorrect username and password"))
                }

                LoginStatus.PASSWORD_INVALID -> {
                    usernameEditText.text.clear()
                    passwordEditText.text.clear()
                    progressBar.showProgressBar(false)
                    coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("You have entered Incorrect username and password"))
                }

                else -> {
                    usernameEditText.text.clear()
                    passwordEditText.text.clear()
                    progressBar.showProgressBar(false)
                    coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Login failed"))
                }
            }
        })

        //checkAppStore()
    }

    private fun navigateToDashboard(
        username: String, password: String, userRole: String, userToken: String
    ) {

        AuthUtil.token = userToken
        AuthUtil.role = userRole.trim()
        AuthUtil.username = username

        viewModel.insertToAuthDetailsDatabase(
            arrayOf(
                DatabaseAuthDetails(
                    username = username, role = userRole.trim(),
                    token = userToken, loggedIn = true, tokenP = password
                )
            )
        )

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        this.setupReceiver()
        if (this::progressBar.isInitialized) progressBar.showProgressBar(false)
        if (usernameEditText.text.toString().isNotEmpty()) {
            usernameEditText.text.clear()
            passwordEditText.text.clear()
            passwordEditText.clearFocus()
        }
    }

//    private fun checkAppStore() {
//        viewModel.appStoreStatus.observe(this, Observer { appStore ->
//            if (appStore != null) {
//                APK_LINK = appStore.downloadLink
//                NEW_VERSION = appStore.latestVersion
//            }
//        })
//    }

//    private fun initConnectivityEventUtil(): BroadcastReceiver {
//
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//
////                Log.d(TAG, "onReceive: ping isConnected(): ${isConnected()}")
//
//
//                IOScope.launch {
//                    try {
//                        val connected = isConnected()
//                        mainScope.launch {
//                            Log.d(TAG, "onCreate: connected: $connected")
//                            if (!connected) {
////                                showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
//                                if (telephonyManager.isDataEnabled) {
//                                    dismissPopup()
//                                } else {
//                                    try {
//                                        showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
//                                    } catch (e: Exception) {}
//                                }
//                            } else {
//                                dismissPopup()
//                            }
//                        }
//                    } catch (e: java.lang.Exception) {
//                        Log.e(TAG, "onCreate: ", e)
//                    }
//                }
//
////                Log.d(TAG, "onReceive: initConnectivityEventUtil")
////                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
////                    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
////                    val networkInfo = manager.activeNetworkInfo
////
////                    Log.d(TAG, "onReceive: networkInfo: ${networkInfo.isAvailable}")
////                    Log.d(TAG, "onReceive: networkInfo isconnected: ${networkInfo.isConnected}")
////
////                    if (networkInfo != null && networkInfo.state == NetworkInfo.State.CONNECTED) {
////                        Log.d(TAG, "onReceive: connected")
////                        dismissPopup()
////                    } else if(intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == false) {
////                        Log.d(TAG, "onReceive: not connected")
////                        showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
////                    }
////                }
//
//
//            }
//
//        }
//
//        return receiver
//
//    }

//    fun registerWifiListenerReceiver(receiver: BroadcastReceiver) {
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
//
//
//        applicationContext.registerReceiver(receiver, intentFilter)
//    }
//
//    fun unRegisterWifiListenerReceiver(receiver: BroadcastReceiver) {
//        applicationContext.unregisterReceiver(receiver)
//    }

    private fun isConnected(): Boolean {
        val command = "ping -c 1 www.google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }

//    private fun initWifiEventUtil() {
//        val wifiEventReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//                Log.d(TAG, "onReceive: wifi.isWifiEnabled: ${wifi.isWifiEnabled}")
////                dismissPopup()
//
//                Log.d(TAG, "onReceive: wifi state: ${wifi.wifiState}")
//
//                when (wifi.wifiState) {
//
//                    WifiManager.WIFI_STATE_DISABLING -> {
//                        progressBar.showProgressBar(true)
//                    }
//
//                    WifiManager.WIFI_STATE_DISABLED -> {
//                        progressBar.showProgressBar(false)
////                        showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
//                        if (telephonyManager.isDataEnabled) {
//                            dismissPopup()
//                        } else {
//                            try {
//                                showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
//                            } catch (e: Exception) {}
//                        }
//                    }
//
//                    WifiManager.WIFI_STATE_ENABLING -> {
//                        progressBar.showProgressBar(true)
//                    }
//
//                    WifiManager.WIFI_STATE_ENABLED -> {
//                        progressBar.showProgressBar(false)
//                        IOScope.launch {
//                            try {
//                                val connected = isConnected()
//                                mainScope.launch {
//                                    Log.d(TAG, "onCreate: connected: $connected")
//                                    if (!connected) {
//                                        Log.d(TAG, "onReceive: not connected")
////                                        showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
//                                        if (telephonyManager.isDataEnabled) {
//                                            dismissPopup()
//                                        } else {
//                                            try {
//                                                showPopupWindow(findViewById(R.id.constraintLayout), showSignalNotificationWindow())
//                                            } catch (e: Exception) {}
//                                        }
//                                    } else {
//                                        Log.d(TAG, "onReceive: connected enabled")
//                                        dismissPopup()
//                                    }
//                                }
//                            } catch (e: java.lang.Exception) {
//                                Log.e(TAG, "onCreate: ", e)
//                            }
//                        }
//
//                    }
//
//                }
//
////                if(!wifi.isWifiEnabled) {
////                    count++
////                    if (count == 1) {
////                        showPopupWindow(findViewById(R.id.relativeLayout), showSignalNotificationWindow())
////                    }
////                }
//
//            }
//
//        }
//
//        val intentFilter = IntentFilter()
////        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
//
////        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
////        wifiManager.startScan()
//
//        applicationContext.registerReceiver(wifiEventReceiver, intentFilter)
//    }

//    private fun initMobileDataUtil() {
//
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
//
//        applicationContext.registerReceiver(mobileDataReceiver, intentFilter)
//
//    }
//
//    private fun initAccessPointUtil() {
//
//        var startTime = System.currentTimeMillis()
//
//
//        val intentFilter = IntentFilter()
////        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
//
//        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        wifiManager.startScan()
//
//        applicationContext.registerReceiver(wifiScanReceiver, intentFilter)
//
//
////        connetToWifi(wifiManager)
//
//    }
//
//    private fun scanWifi(wifiManager: WifiManager) {
//        val wifiNetworks = mutableListOf<WifiNetwork>()
//
//        Log.d(TAG, "scanWifi: scanning")
//
//        // scan networks
//        val wifiList: List<ScanResult> = wifiManager.scanResults
//        Log.d(TAG, "ScanResults size is ${wifiManager.scanResults.size}")
//        for (scanResult in wifiList) {
//            val level = WifiManager.calculateSignalLevel(scanResult.level, 5)
////            Log.d(TAG, "Scanned name is ${scanResult.SSID} Level is: $level")
//
//
//            if (wifiManager.configuredNetworks.any {
//                    it.SSID.replace("\"", "") == scanResult.SSID }) {
//                wifiNetworks.add(WifiNetwork(level, scanResult.SSID))
//            }
//        }
//
//        wifiNetworks.sortByDescending { it.level }
//        loop@ for (wifiNetwork in wifiNetworks) {
//
//            if (wifiNetwork.level > 1) {
//                val connected = connetToWifi(wifiManager, wifiNetwork.ssid)
//                Log.d(TAG, "scanWifi: connected: $connected")
////                Toast.makeText(applicationContext, "connected to ${wifiNetwork.ssid}: $connected", Toast.LENGTH_SHORT).show()
//                if (connected) {
//                    break@loop
//                }
//            }
//
//
//        }
//        // level of current connection
////        val rssi = wifiManager.connectionInfo.rssi
////        val level = WifiManager.calculateSignalLevel(rssi, 5)
////        Log.d(TAG, "Current connection level is $level")
//
////        connetToWifi(wifiManager)
//
//    }
//
//    private fun connetToWifi(wifiManager: WifiManager, ssid: String): Boolean {
//
//        Log.d(TAG, "connetToWifi: ssid: $ssid")
////        Toast.makeText(applicationContext, "connect wifi $ssid", Toast.LENGTH_SHORT).show()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Log.d(TAG, "connetToWifi: wifi suggestion")
//            val builder = WifiNetworkSuggestion.Builder()
//                .setSsid(ssid)
//            val suggestion = builder.build()
//            val list = arrayListOf(suggestion)
//
//            val status = wifiManager.addNetworkSuggestions(list)
//
//            return if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
//                Log.d(TAG, "connetToWifi: success")
//                true
//            } else {
////                scanWifi(wifiManager)
//                false
//            }
//
//        } else {
//            if (!wifiManager.isWifiEnabled) {
//                wifiManager.isWifiEnabled = true
//            }
//
//            val conf = WifiConfiguration()
//            conf.SSID = String.format("\"%s\"", ssid)
////        conf.preSharedKey = String.format("\"%s\"", "e9FwxN2z")
//
//            val netId = wifiManager.addNetwork(conf)
//            wifiManager.disconnect()
//            wifiManager.enableNetwork(netId, true)
//            wifiManager.reconnect()
//
////            val cm = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
////            val networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
////
////            val connected = networkInfo?.isConnected
//
////            Log.d(TAG, "connetToWifi: networkInfo?.isConnected: $connected")
//
//
//            return wifiManager.connectionInfo.ssid == ssid
//
//
//        }
//
//
//    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
            Log.d(TAG, "dismissPopup: dismissed")
        }
    }

    private fun showPopupWindow(view: View, popupWindowType: PopupWindow) {
        val dm = DisplayMetrics()
        this.windowManager?.defaultDisplay?.getMetrics(dm)

//        val width = (dm.widthPixels * 1).toInt()
//        val height = (dm.heightPixels * .1).toInt()

        dismissPopup()
//        popupWindow = null
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = true

        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.let {
//                    if (it.x < 0 || it.x > width) return true
//                    if (it.y < 0 || it.y > height) return true
                }

                return false
            }

        })

        popupWindow?.isFocusable = false
//        popupWindow?.update(0, 0, width, height)
        view.post {
            popupWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
        }

        DimUtil.dimBehind(popupWindow)

//        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun showSignalNotificationWindow(): PopupWindow {

        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupSignalNotificationBinding.inflate(inflater)

        binding.btnMachineHistory.text =
            languageJsonObject.getTranslation(binding.btnMachineHistory.text.toString())

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun showPoorSignalNotificationWindow(): PopupWindow {

        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupPoorSignalNotificationBinding.inflate(inflater)

        binding.btnMachineHistory.text =
            languageJsonObject.getTranslation(binding.btnMachineHistory.text.toString())

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun isOnline(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    fun isMobileDataEnabled(): Boolean? {
        val connectivityService =
            getSystemService(Context.CONNECTIVITY_SERVICE)
        val cm = connectivityService as ConnectivityManager
        return try {
            val c = Class.forName(cm.javaClass.name)
            val m: Method = c.getDeclaredMethod("getMobileDataEnabled")
            m.setAccessible(true)
            m.invoke(cm) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkNetworkStatus(context: Context): String? {
        var networkStatus = ""
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkStatus = if (manager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
            "wifi"
        } else if (manager.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
            "mobileData"
        } else {
            "noNetwork"
        }
        return networkStatus
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


    override fun onNoConnection() {
        dismissPopup()
        showPopupWindow(findViewById(R.id.coordinatorLayout), showSignalNotificationWindow())
    }

    override fun onWifiStateDisabled(isMobileDataEnabled: Boolean) {
        progressBar.showProgressBar(false)
        if (!isMobileDataEnabled) {
            showPopupWindow(findViewById(R.id.coordinatorLayout), showSignalNotificationWindow())
        }
    }

    override fun onWifiStateEnabling() {
        progressBar.showProgressBar(true)
    }

    override fun onWifiStateEnabled(isDataEnabled: Boolean) {
        dismissPopup()
        progressBar.showProgressBar(false)
        if (!isDataEnabled) {
            showPopupWindow(findViewById(R.id.coordinatorLayout), showSignalNotificationWindow())
        }
    }

    override fun onWifiStateDisabling() {
        progressBar.showProgressBar(true)
    }

    override fun onConnectionPoor() {
        dismissPopup()
        showPopupWindow(findViewById(R.id.coordinatorLayout), showPoorSignalNotificationWindow())
    }

    override fun onConnectionStrong() {
        dismissPopup()
    }

}

