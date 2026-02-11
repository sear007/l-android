package co.ltlabs.ltmechanic.util

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T) {
            removeObserver(this)
            observer.onChanged(t)
        }
    })
}

fun Any?.toYesNo(): String {
    val data = this
    return if (data is Int) {
        if (data == 1) "YES" else "NO"
    } else if (data is Boolean) {
        if (data) "YES" else "NO"
    } else {
        "NO"
    }
}

fun Any?.toBoolean() = when (this) {
    is Int -> this == 1
    is Boolean -> this
    else -> false
}

fun AppCompatActivity.showToast(msg: String?) {
    if (msg != null) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}