package co.ltlabs.ltmechanic.util

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import org.json.JSONObject

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("translate_textview")
    fun setTranslationsTextView(tv: TextView, jsonObject: JSONObject?) {
        if (jsonObject == null) return
        tv.text = jsonObject.getTranslation(tv.text.toString())
    }

    @JvmStatic
    @BindingAdapter("translate_hint")
    fun setTranslationsHint(tv: EditText, jsonObject: JSONObject?) {
        if (jsonObject == null) return
        tv.hint = jsonObject.getTranslation(tv.hint.toString())
    }

    @JvmStatic
    @BindingAdapter("translate_button")
    fun setTranslationsButton(btn: Button, jsonObject: JSONObject?) {
        if (jsonObject == null) return
        btn.text = jsonObject.getTranslation(btn.text.toString())
    }

    @JvmStatic
    @BindingAdapter("translate_edittext")
    fun setTranslationsEditText(edt: EditText, jsonObject: JSONObject?) {
        if (jsonObject == null) return
        edt.hint = jsonObject.getTranslation(edt.hint.toString())
    }

    @JvmStatic
    @BindingAdapter("layoutMarginTop")
    fun setLayoutMarginTop(view: View, dimen: Float) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = dimen.toInt()
        view.layoutParams = layoutParams
    }

    @JvmStatic
    @BindingAdapter("visibleGone")
    fun setVisibleGoneView(view: View, visible: Boolean) {
        view.isVisible = visible
    }

}