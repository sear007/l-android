package co.ltlabs.ltmechanic.util

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

object SnackBarManager {
    /**
     * Show default shorts with string message
     */
    fun showShortSnackBar(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        val sView = snackbar.view
        val textView = sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    /**
     * Show short snackbar with string message and color
     */
    fun showShortSnackBar(view: View, message: String, color: Int) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        val sView = snackbar.view
        sView.setBackgroundColor(color)
        val textView = sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    fun showInFiniteSnackBar(view : View, message : String){
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        val sView = snackbar.view
        sView.setBackgroundColor(Color.RED)
        val textView = sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    /**
     * Show short snackbar with resource string & color
     */
    fun showShortSnackBar(view : View, @StringRes message: Int, color: Int) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        val sView = snackbar.view
        sView.setBackgroundColor(color)
        val textView = sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    fun showIndefiniteSnackBar(view : View, message: String,color: Int) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        val sView = snackbar.view
        sView.setBackgroundColor(color)
        val textView = sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }
}
