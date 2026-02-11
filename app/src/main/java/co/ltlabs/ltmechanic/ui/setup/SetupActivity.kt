package co.ltlabs.ltmechanic.ui.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.domain.Language
import co.ltlabs.ltmechanic.ui.auth.AuthActivity
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.setup.SetupViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TranslationViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

private const val TAG = "SetupActivity";

class SetupActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private val viewModel: SetupViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupViewModel::class.java)
    }

    private val translationViewModel: TranslationViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TranslationViewModel::class.java)
    }

    private lateinit var startButton: Button
    private lateinit var endpointEditText: EditText
    private lateinit var nfcCheckbox: CheckBox
    private lateinit var barcodeCheckbox: CheckBox
    private lateinit var languageSpinner: Spinner
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var progressBar: ProgressBar

    private var selectedLanguage = ""
    private var selectedFactory = ""

    override fun onResume() {
        super.onResume()

        val settings = getSharedPreferences("prefs", 0)
        val firstRun = settings.getBoolean("firstRun", true)

        Log.d(TAG, "onResume: firstRun: $firstRun")
        if (!firstRun) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

            val permissionAll = 1
            val permissions = listOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (!hasPermissions(this, *permissions.toTypedArray())) {
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), permissionAll)
            }

//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.ACCESS_WIFI_STATE).toTypedArray(), 100);
//            }
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.CHANGE_WIFI_STATE).toTypedArray(), 101);
//            }

//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(), 102);
//            }
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.ACCESS_COARSE_LOCATION).toTypedArray(), 103);
//            }
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.CAMERA).toTypedArray(), 104);
//            }
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(), 105);
//            }
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray(), 106);
//            }


        }
        else
        {
            // if version is below m then write code here,
        }

        startButton = findViewById(R.id.btnStart)
        endpointEditText = findViewById(R.id.editTextEndpoint)
        nfcCheckbox = findViewById(R.id.checkBoxNFC)
        barcodeCheckbox = findViewById(R.id.checkBoxBarcode)
        progressBar = findViewById(R.id.progress_bar)
        languageSpinner = findViewById(R.id.spinnerLanguage)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)

        translationViewModel.getLanguages()

        endpointEditText.hint = "$API_SCHEME://$API_HOSTNAME"

        val settings = getSharedPreferences("prefs", 0)
        val editor = settings.edit()

        if (!settings.contains("firstRun")) {
            viewModel.createConfigDirAndFile(this)
            editor.putBoolean("firstRun", true)
            editor.apply()
        }

        translationViewModel.languages.observe(this, Observer {languages ->
            Log.d(TAG, "onCreate: languages: $languages")
            if (languages != null) {

                val list = mutableListOf<Language>()
//                    list.add(SolutionType(0, "", ""))
                languages.forEach {language ->
                    list.add(language)
                }
                val dataAdapter =  ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                languageSpinner.adapter = dataAdapter

                languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        val language = parent?.selectedItem as Language
                        selectedLanguage = language.code
                        selectedFactory = language.factory

                        println("=== LCODE $selectedLanguage, LF $selectedFactory ")

                    }

                }

                translationViewModel.languagesComplete()
            }

        })

        startButton.setOnClickListener {
            viewModel.saveSettings(
                selectedLanguage
            )
//            startActivity(Intent(this, AuthActivity::class.java))
//            finish()
        }

        viewModel.status.observe(this, Observer {
            when(it) {
                ApiStatus.LOADING -> {
                    progressBar.showProgressBar(true)
                }

                else -> {
                    progressBar.showProgressBar(false)
                }
            }
        })

        viewModel.setupStatus.observe(this, Observer {
            when(it) {
                SetupStatus.SUCCESS -> {
//                    viewModel.getLanguageFile(this)
                    translationViewModel.getTranslations(this, selectedLanguage, selectedFactory)

                }
            }
        })

        translationViewModel.translation.observe(this, Observer {
            if (it != null) {
                if (it) {

                    val endpoint = if (endpointEditText.text.toString().isEmpty()) {
                        "$API_SCHEME://$API_HOSTNAME"
                    } else {
                        endpointEditText.text.toString()
                    }
                    if (URLUtil.isValidUrl(endpoint)) {

                        viewModel.testConnection(this, selectedLanguage, endpoint,
                            nfcCheckbox.isChecked,
                            barcodeCheckbox.isChecked)
                    } else {
                        coordinatorLayout.showSnackbar("Cannot connect to server")
                    }

                }
            }
        })

        viewModel.connected.observe(this, Observer {
            Log.d(TAG, "onCreate: connected: $it")
            if (it != null) {

                Log.d(TAG, "onCreate: connected: $it")

                if (it) {
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                } else {
                    coordinatorLayout.showSnackbar("Cannot connect to server")
                }

                viewModel.connectedComplete()
            }
        })

        viewModel.languageStatus.observe(this, Observer {
//            if (it != null) {
//                when(it) {
//                    LanguageStatus.SUCCESS -> {
//                        startActivity(Intent(this, AuthActivity::class.java))
//                        finish()
//                    }
//                }
//
//                viewModel.languageStatusComplete()
//            }
        })


    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
