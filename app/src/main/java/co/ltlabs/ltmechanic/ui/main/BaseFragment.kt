package co.ltlabs.ltmechanic.ui.main

import android.content.Intent
import android.os.Handler
import androidx.lifecycle.ViewModelProvider
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.ui.auth.AuthActivity
import co.ltlabs.ltmechanic.ui.dialog.FindMachineBSDialog
import co.ltlabs.ltmechanic.util.SharePrefUtil
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

open class BaseFragment : DaggerFragment() {

    @Inject
    lateinit var loading: LoadingIndicator

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    var findMachineBsDialog: FindMachineBSDialog? = null
    fun showFindMachineDialog(callback: (machineNo: String) -> Unit) {
        if (findMachineBsDialog == null)
            findMachineBsDialog = FindMachineBSDialog()

        if (findMachineBsDialog?.isAdded == false) {
            mainViewModel.insertToNfcDeviceDatabase(true)
            findMachineBsDialog?.show(childFragmentManager, findMachineBsDialog?.tag)
        }

        findMachineBsDialog?.onDismissListener {
            mainViewModel.insertToNfcDeviceDatabase(false)
            findMachineBsDialog = null
        }

        findMachineBsDialog?.onDoneListener {
            callback.invoke(it)
        }
    }

    fun logout() {
        loading.show(requireContext())
        Handler().postDelayed({
            mainViewModel.insertToAuthDetailsDatabase(
                arrayOf(
                    DatabaseAuthDetails(
                        username = "",
                        role = "",
                        token = "",
                        loggedIn = false,
                        tokenP = ""
                    )
                )
            )

            //startActivity(Intent(activity, AuthActivity::class.java))
            SharePrefUtil.removeValue(AppConfig.SP_PASSWORD)
            //activity?.finish()
            val intent = Intent().setClass(requireContext(), AuthActivity::class.java)
            startActivity(intent)
            activity?.finish()
            loading.dismiss()
        }, 1000)
    }

}