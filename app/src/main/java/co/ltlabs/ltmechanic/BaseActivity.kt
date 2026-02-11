package co.ltlabs.ltmechanic

import android.content.Intent
import android.os.Bundle
import co.ltlabs.ltmechanic.ui.auth.AuthActivity
import co.ltlabs.ltmechanic.util.makeStatusBarTransparent
import dagger.android.support.DaggerAppCompatActivity

private const val TAG = "BaseActivity";

abstract class BaseActivity : TopActivity() {

//    @Inject
//    lateinit var socketInstance: SocketInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.title = null
        }

        makeStatusBarTransparent()
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        window.statusBarColor = Color.TRANSPARENT

        // connect to socket io server
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }


}