package co.ltlabs.ltmechanic.ui.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.constant.AppConfig.APP_NAME
import co.ltlabs.ltmechanic.constant.AppConfig.BASE_URL
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_CODE
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_NAME
import co.ltlabs.ltmechanic.constant.AppConfig.GLOBAL_BASE_URL
import co.ltlabs.ltmechanic.constant.AppConfig.SP_COMPANY_BASE_URL
import co.ltlabs.ltmechanic.constant.AppConfig.SP_COMPANY_CODE
import co.ltlabs.ltmechanic.constant.AppConfig.SP_COMPANY_NAME
import co.ltlabs.ltmechanic.constant.AppConfig.SP_COMPANY_SOCKET_IO_URL
import co.ltlabs.ltmechanic.constant.AppConfig.SP_GLOBAL_BASE_URL
import co.ltlabs.ltmechanic.domain.AppConfigRequest
import co.ltlabs.ltmechanic.domain.CompanyInfoResponse
import co.ltlabs.ltmechanic.ui.login.LoginActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.util.notification.NotificationClient
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.setup.SetupViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TranslationViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_login_company.*
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class LoginCompanyActivity : DaggerAppCompatActivity() {

    private lateinit var loginIntent: Intent

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var laoding: LoadingIndicator


    private val viewModel: SetupViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupViewModel::class.java)
    }

    private val translationViewModel: TranslationViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TranslationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_company)
        // If click from notification
        apply {
            val title = intent.getStringExtra(NotificationClient.KEY_TITLE)
            val body = intent.getStringExtra(NotificationClient.KEY_BODY)
            val category = intent.getStringExtra(NotificationClient.KEY_CATEGORY)
            val action = intent.getStringExtra(NotificationClient.KEY_ACTION)
            val reference = intent.getStringExtra(NotificationClient.KEY_REFERENCE)
            val factoryId = intent.getIntExtra(NotificationClient.KEY_FACTORY_ID, 0)
            val companyCode = intent.getIntExtra(NotificationClient.KEY_COMPANY_CODE, 0)
            loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.putExtra(AppConfig.EXTRA_TITLE, title)
            loginIntent.putExtra(AppConfig.EXTRA_BODY, body)
            loginIntent.putExtra(AppConfig.EXTRA_CATEGORY, category)
            loginIntent.putExtra(AppConfig.EXTRA_ACTION, action)
            loginIntent.putExtra(AppConfig.EXTRA_REFERENCE, reference)
            loginIntent.putExtra(AppConfig.EXTRA_FACTORY_ID, factoryId)
            loginIntent.putExtra(AppConfig.EXTRA_COMPANY_CODE, companyCode)
        }
        checkCompanyCode()
        requestUserPermissions()
        companyCodeLayout.setPadding(0, 30, 0, 0)
        subscribeToObserver()
        setListener()

        btnNext.setOnClickListener {
            val companyCode = etCompanyCode.text.toString().trim()
            val request = AppConfigRequest(APP_NAME, companyCode)
            try {
                viewModel.loginCompany(request)
            } catch (e: Exception) {
                Log.e(APP_NAME, "error: ${e.message}")
            }
            subscribeToConnectionObserver()
        }
    }

    private fun setListener() {
        etCompanyCode.addTextChangedListener {
            this.run {
                errorMessage.isVisible = false
                companyCodeLayout.hintTextColor =
                    ContextCompat.getColorStateList(this, R.color.black_54)
                companyCodeLayout.error = null
            }
        }
    }

    private fun requestUserPermissions() {
        val permissionAll = 1
        val permissions = listOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!hasPermissions(this, *permissions.toTypedArray())) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), permissionAll)
        }
    }

    private fun setDefaultLanguageAndServerObserver() {
        // SET DEFAULT VALUES
        val selectedLanguage = LanguageUtil.ENGLISH
        val selectedFactory = "ltlabs"

        viewModel.saveSettings(
            selectedLanguage
        )

        viewModel.setupStatus.observeOnce(this, Observer {
            if (it == SetupStatus.SUCCESS) {
                translationViewModel.getTranslations(this, selectedLanguage, selectedFactory)
            }
        })

        translationViewModel.translation.observeOnce(this, Observer {
            if (it != null) {
                if (it) {

                    val endpoint = if (BASE_URL.isEmpty()) {
                        "$API_SCHEME://$API_HOSTNAME"
                    } else BASE_URL

                    if (URLUtil.isValidUrl(endpoint)) {
                        viewModel.testConnection(
                            this, selectedLanguage, endpoint, hasNFC = true, hasBarcode = true
                        )
                    } else {
                        Snackbar.make(
                            constraintLayout,
                            "Cannot connect to server",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        })
    }

    private fun subscribeToConnectionObserver() {
        viewModel.connected.observeOnce(this, Observer {
            Log.d("LTM", "onCreate: connected: $it")
            if (it != null) {

                Log.d("LTM", "onCreate: connected: $it")

                if (it) {
                    /*startActivity(loginIntent)
                    finish()*/
                } else {
                    Snackbar.make(
                        constraintLayout,
                        "Cannot connect to server",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                viewModel.connectedComplete()
            }
        })
    }

    private fun subscribeToObserver() {
        viewModel.companyLoginStatus.observe(this, Observer { response ->
            when (response) {
                is ResponseUtil.Loading -> {
                    btnNext.isEnabled = false
                    laoding.show(this)
                }

                is ResponseUtil.Success -> {
                    val data = response.data as CompanyInfoResponse
                    onLoginSuccess(data)

                }

                is ResponseUtil.Error -> {
                    btnNext.isEnabled = true
                    when (val e = response.exception) {
                        is HttpException -> onLoginError(e)
                        is Exception -> e.printStackTrace()
                    }
                    laoding.dismiss()
                    Timber.e(response.exception?.localizedMessage)
                }
            }
        })
    }

    private fun checkCompanyCode() {
        val companyCodeSP = SharePrefUtil.getString(SP_COMPANY_CODE, COMPANY_CODE)
        val globalBaseURL = SharePrefUtil.getString(SP_GLOBAL_BASE_URL, GLOBAL_BASE_URL)
        val baseUrlSP = SharePrefUtil.getString(SP_COMPANY_BASE_URL, BASE_URL)
        val companyNameSP = SharePrefUtil.getString(SP_COMPANY_NAME, COMPANY_NAME)
        val socketIoUrl = SharePrefUtil.getString(SP_COMPANY_SOCKET_IO_URL, "")
        etCompanyCode.setText(companyCodeSP)
        val from = intent.getStringExtra(LoginActivity.EXTRA_BACK_PRESSED)
        if (from == LoginActivity.FROM_LOGIN) return
        if (companyCodeSP != "" && baseUrlSP != "" && globalBaseURL != "") {
            GLOBAL_BASE_URL = globalBaseURL ?: ""
            BASE_URL = baseUrlSP ?: ""
            COMPANY_CODE = companyCodeSP ?: ""
            COMPANY_NAME = companyNameSP ?: ""
            startActivity(loginIntent)
            finish()
        }
    }

    private fun onLoginSuccess(data: CompanyInfoResponse) {
        try {
            SharePrefUtil.set(SP_GLOBAL_BASE_URL, data.globalBaseURL)
            SharePrefUtil.set(SP_COMPANY_BASE_URL, data.baseURL)
            SharePrefUtil.set(SP_COMPANY_CODE, data.company)
            SharePrefUtil.set(SP_COMPANY_NAME, data.companyName)
            SharePrefUtil.set(SP_COMPANY_SOCKET_IO_URL, data.socketURL)

            GLOBAL_BASE_URL = data.globalBaseURL
            BASE_URL = data.baseURL
            COMPANY_CODE = data.company

            COMPANY_NAME = data.companyName
            setDefaultLanguageAndServerObserver()
            viewModel.createConfigDirAndFile(this)
            startActivity(loginIntent)
            laoding.dismiss()
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            Log.e(APP_NAME, e.message.toString())
        }
    }

    private fun onLoginError(httpError: HttpException?) {
        try {
            if (httpError?.response()?.errorBody() != null) {
                val responseError = httpError.response()?.errorBody()!!
                companyCodeLayout.setPadding(0, 0, 0, -25)
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = ConvertErrorBody.toErrorResponse(responseError)
                companyCodeLayout.error = "    "
            }
        } catch (e: Exception) {
            Log.e(APP_NAME, e.message.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        if (errorMessage.isVisible) {
            errorMessage.visibility = View.GONE
            companyCodeLayout.error = null
        }
        if (!btnNext.isEnabled) btnNext.isEnabled = true
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

}