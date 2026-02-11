package co.ltlabs.ltmechanic.ui.login

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.TopActivity
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_CODE
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_NAME
import co.ltlabs.ltmechanic.constant.AppConfig.SP_PASSWORD
import co.ltlabs.ltmechanic.constant.AppConfig.SP_REMEMBER_PWD
import co.ltlabs.ltmechanic.constant.AppConfig.SP_USERNAME
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.databinding.PopupPoorSignalNotificationBinding
import co.ltlabs.ltmechanic.databinding.PopupSignalNotificationBinding
import co.ltlabs.ltmechanic.databinding.ServerCommunicationNotificationBinding
import co.ltlabs.ltmechanic.domain.Employee
import co.ltlabs.ltmechanic.domain.RfidRequest
import co.ltlabs.ltmechanic.service.DeviceTokenService
import co.ltlabs.ltmechanic.ui.main.MainActivity
import co.ltlabs.ltmechanic.ui.setup.LoginCompanyActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCUtil
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.auth.AuthViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class LoginActivity : TopActivity() {

    private lateinit var viewModel: AuthViewModel

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    lateinit var telephonyManager: TelephonyManager
    private var popupWindow: PopupWindow? = null
    private var isCheckedRemember = false

    lateinit var progressBar: ProgressBar

    lateinit var loginTitle: TextView
    lateinit var companyCode: TextView
    lateinit var errorMessage: TextView

    private lateinit var usernameEditText: EditText
    lateinit var passwordEditText: EditText
    private lateinit var btnLogin: Button
    lateinit var appVersion: TextView

    private var nfcAdapter: NfcAdapter? = null
    private var title: String? = null
    private var body: String? = null
    private var category: String? = null
    private var action: String? = null
    private var reference: String? = null

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
        showPopupWindow(findViewById(R.id.constraintLayout), showPoorSignalNotificationWindow())
    }

    override fun onConnectionStrong() {
        dismissPopup()
    }

    override fun onNoConnection() {
        dismissPopup()
        showPopupWindow(findViewById(R.id.constraintLayout), showPoorSignalNotificationWindow())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // If click from notification
        apply {
            title = intent.getStringExtra(AppConfig.EXTRA_TITLE)
            body = intent.getStringExtra(AppConfig.EXTRA_BODY)
            category = intent.getStringExtra(AppConfig.EXTRA_CATEGORY)
            action = intent.getStringExtra(AppConfig.EXTRA_ACTION)
            reference = intent.getStringExtra(AppConfig.EXTRA_REFERENCE)
        }
        initUI()
        initNFC()
        setListeners()
        telephonyManager =
            application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        viewModel.loadConfigFile(this)

        viewModel.languageFromDatabase.observe(this, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    LanguageUtil.selectedLanguage = LanguageUtil.ENGLISH
                    viewModel.loadTranslationFile(this)
                }
            }
        })

        //viewModel.getAppInfo()

        subscribeToObserver()
        listenRFIDObserve()
        btnLogin.setOnClickListener { doLogin() }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            NFCUtil.resolveIntent(it)
        }
    }


    override fun onResume() {
        super.onResume()
        clearError()
        keepLogin()
        if (this::progressBar.isInitialized) progressBar.showProgressBar(false)
        checkRememberMe()

        nfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        if (isCheckedRemember) {
            SharePrefUtil.set(SP_USERNAME, usernameEditText.text.toString())
            SharePrefUtil.set(
                SP_REMEMBER_PWD,
                AESEncryption.encrypt(passwordEditText.text.toString()).toString()
            )
        }
        super.onPause()
    }

    override fun onBackPressed() {
        val intent = Intent(this, LoginCompanyActivity::class.java)
        intent.putExtra(EXTRA_BACK_PRESSED, FROM_LOGIN)
        startActivity(intent)
        finish()
    }

    private fun listenRFIDObserve() {
        NFCUtil.rfid.observe(this) { rfid ->
            if (rfid != null) {
                val loginRequest = RfidRequest(COMPANY_CODE, rfid)
                viewModel.loginEmployeeWithRfidAsync(loginRequest)
                NFCUtil.clearRfid()
            }
        }
    }

    private fun doLogin() {
        val userName = usernameEditText.text.toString().trim()
        val userPassword = passwordEditText.text.toString().trim()
        val loginRequest = Employee(COMPANY_CODE, userName, userPassword)
        AuthUtil.username = userName
        AuthUtil.password = AESEncryption.encrypt(userPassword).toString()
        viewModel.loginEmployee(loginRequest)
    }

    private fun subscribeToObserver() {
        viewModel.employeeLoginStatus.observe(this) { response ->
            when (response) {
                is ResponseUtil.Loading -> {
                    progressBar.showProgressBar(true)
                    btnLogin.isEnabled = false
                }
                is ResponseUtil.Error -> {
                    progressBar.showProgressBar(false)
                    btnLogin.isEnabled = true
                    when (val e = response.exception) {
                        is HttpException -> onLoginError(e)
                        is Exception -> e.printStackTrace()
                    }
                }
                is ResponseUtil.Success -> {
                    progressBar.showProgressBar(false)
                    btnLogin.isEnabled = true
                    val data = response.data

                    AuthUtil.userId = data?.userId!!
                    AuthUtil.role = data.role
                    AuthUtil.token = data.accessToken
                    AuthUtil.refresh_token = data.refreshToken
                    AuthUtil.username = data.username

                    SharePrefUtil.set(AppConfig.SP_USER_REFRESH_TOKEN, data.refreshToken)
                    SharePrefUtil.set(AppConfig.SP_USER_ROLE, AuthUtil.role)
                    SharePrefUtil.set(AppConfig.SP_USER_TOKEN, AuthUtil.token)
                    SharePrefUtil.set(AppConfig.SP_FACTORY_ID, data.factoryId ?: 0)

                    val intent = Intent(this, DeviceTokenService::class.java)
                    startService(intent)

                    navigateToDashboard(
                        AuthUtil.username,
                        AuthUtil.password,
                        AuthUtil.role,
                        AuthUtil.token
                    )

                    if (rememberMe.isChecked) {
                        SharePrefUtil.set(SP_REMEMBER_PWD, AuthUtil.password)
                    } else {
                        SharePrefUtil.set(SP_REMEMBER_PWD, "")
                    }

                }
            }
        }
    }

    private fun keepLogin() {
        val username = SharePrefUtil.getString(SP_USERNAME, "")
        val password = SharePrefUtil.getString(SP_PASSWORD, "")
        val userRole = SharePrefUtil.getString(AppConfig.SP_USER_ROLE, "")
        val userToken = SharePrefUtil.getString(AppConfig.SP_USER_TOKEN, "")
        AuthUtil.refresh_token = SharePrefUtil.getString(AppConfig.SP_USER_REFRESH_TOKEN, "") ?: ""

        if (!userToken.isNullOrEmpty()) {
            AuthUtil.password = password ?: ""
            navigateToDashboard(
                username ?: "",
                AuthUtil.password,
                userRole.toString(),
                userToken.toString()
            )
        } else {
            this.setupReceiver()
        }
    }

    private fun onLoginError(httpError: HttpException?) {
        try {
            usernameLayout.setPadding(0, 0, 0, 0)
            val param = usernameLayout.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(param.leftMargin, param.topMargin, param.rightMargin, -10)
            usernameLayout.layoutParams = param

            passwordLayout.setPadding(0, 0, 0, 0)
            val passParam = passwordLayout.layoutParams as ViewGroup.MarginLayoutParams
            passParam.setMargins(param.leftMargin, param.topMargin, param.rightMargin, -30)
            passwordLayout.layoutParams = passParam

            if (httpError?.response()?.errorBody() != null) {
                val responseError = httpError.response()?.errorBody()!!
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = ConvertErrorBody.toErrorResponse(responseError)
                usernameLayout.error = "    "
                passwordLayout.error = "    "
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setListeners() {
        usernameEditText.addTextChangedListener {
            this.run {
                clearError()
            }
        }

        passwordEditText.addTextChangedListener {
            this.run {
                clearError()
            }
        }
    }

    private fun clearError() {
        errorMessage.isVisible = false
        usernameLayout.hintTextColor = ContextCompat.getColorStateList(this, R.color.black_54)
        usernameLayout.error = null
        passwordLayout.hintTextColor = ContextCompat.getColorStateList(this, R.color.black_54)
        passwordLayout.error = null
    }

    private fun initUI() {
        progressBar = findViewById(R.id.progress_bar)

        loginTitle = findViewById(R.id.loginTitle)
        companyCode = findViewById(R.id.CompanyCode)

        errorMessage = findViewById(R.id.errorMessage)
        usernameEditText = findViewById(R.id.userId)
        passwordEditText = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)
        appVersion = findViewById(R.id.version)

        loginTitle.text = "Sign in to $COMPANY_NAME"
        companyCode.text = "Company Code: $COMPANY_CODE"
        appVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        setupListener()
    }

    private fun initNFC() {
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        } catch (e: Exception) {

        }
    }

    private fun setupListener() {
        rememberMe.setOnCheckedChangeListener { _, checked ->
            isCheckedRemember = checked
            if (checked) {
                SharePrefUtil.set(SP_USERNAME, usernameEditText.text.toString())
                SharePrefUtil.set(
                    SP_REMEMBER_PWD,
                    AESEncryption.encrypt(passwordEditText.text.toString()).toString()
                )
            } else {
                SharePrefUtil.set(SP_PASSWORD, "")
                SharePrefUtil.set(SP_USERNAME, "")
            }
        }
    }

    private fun checkRememberMe() {
        val username = SharePrefUtil.getString(SP_USERNAME, "")
        val password = SharePrefUtil.getString(SP_REMEMBER_PWD, "")

        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            usernameEditText.setText(username.toString())
            passwordEditText.setText(AESEncryption.decrypt(password.toString()))
            rememberMe.isChecked = true
        } else {
            if (usernameEditText.text.toString().isNotEmpty()) {
                usernameEditText.text.clear()
                passwordEditText.text.clear()
                passwordEditText.clearFocus()
            }
        }
        isCheckedRemember = rememberMe.isChecked
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

        popupWindow?.setTouchInterceptor { v, event ->
            event?.let {}
            false
        }
        popupWindow?.isFocusable = false
        view.post {
            popupWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
        }
        DimUtil.dimBehind(popupWindow)
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

    private fun showServerCommunicationProblem(): PopupWindow {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ServerCommunicationNotificationBinding.inflate(inflater)
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

    private fun navigateToDashboard(
        username: String, password: String, userRole: String, userToken: String
    ) {

        AuthUtil.token = userToken
        AuthUtil.role = userRole.trim()
        AuthUtil.username = username
        AuthUtil.password = password

        viewModel.insertToAuthDetailsDatabase(
            arrayOf(
                DatabaseAuthDetails(
                    username = username, role = userRole.trim(),
                    token = userToken, loggedIn = true, tokenP = password
                )
            )
        )
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(AppConfig.EXTRA_TITLE, title)
        intent.putExtra(AppConfig.EXTRA_BODY, body)
        intent.putExtra(AppConfig.EXTRA_CATEGORY, category)
        intent.putExtra(AppConfig.EXTRA_ACTION, action)
        intent.putExtra(AppConfig.EXTRA_REFERENCE, reference)
        startActivity(intent)
        finishAffinity()
    }

    companion object {
        const val EXTRA_BACK_PRESSED = "EXTRA_BACK_PRESSED"
        const val FROM_LOGIN = "FROM_LOGIN"
    }

}