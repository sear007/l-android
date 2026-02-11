package co.ltlabs.ltmechanic.util

import android.widget.ProgressBar

fun ProgressBar.showProgressBar(visible: Boolean) {
    with(this) {
        visibility = if (visible) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }
}