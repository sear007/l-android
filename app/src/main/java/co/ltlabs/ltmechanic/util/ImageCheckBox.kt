package co.ltlabs.ltmechanic.util

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import co.ltlabs.ltmechanic.R

class ImageCheckBox(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageView(context, attrs) {

    var isChecked = false

    fun setToggleCheck() {
        if (isChecked) {
            setUnChecked()
        } else {
            setChecked()
        }
    }

    fun setReady() {
        isEnabled = false
        isChecked = true
        setImageResource(R.drawable.ic_baseline_check_24)
        setBackgroundResource(R.drawable.bg_ready_small_radious)
    }

    fun setChecked() {
        isChecked = true
        setImageResource(R.drawable.ic_baseline_check_24)
        setBackgroundResource(R.drawable.button)
    }

    fun setUnChecked() {
        isChecked = false
        setImageResource(0)
        setBackgroundResource(R.drawable.bg_white_small_radious)
    }

}