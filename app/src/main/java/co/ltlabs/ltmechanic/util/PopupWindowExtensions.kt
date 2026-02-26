package co.ltlabs.ltmechanic.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

fun PopupWindow?.showPopupWindow(view: View, activity: Activity?) {
    val dm = DisplayMetrics()
    activity?.windowManager?.defaultDisplay?.getMetrics(dm)

    val width = (dm.widthPixels * .9).toInt()
    val height = (dm.heightPixels * .93).toInt()

    this.dismissPopup()
    this?.isOutsideTouchable = false

    this?.setTouchInterceptor(object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            event?.let {
                if (it.x < 0 || it.x > width) return true
                if (it.y < 0 || it.y > height) return true
            }

            return false
        }

    })

    this?.isFocusable = true
    this?.update(0, 0, width, height)
    this?.showAtLocation(view, Gravity.CENTER, 0, -25)

    this?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
}

fun PopupWindow?.dismissPopup() {
    this?.let {
        if (it.isShowing) {
            it.dismiss()
        }
//        it = null
    }
}

//fun PopupWindow.dimBehind() {
//
//    var container = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        this.contentView.parent as View
//    } else {
//        this.contentView
//    }
//
//    if (this.background != null) {
//        container = container.parent as View
//    }
//
//    val context = this.contentView.context
//    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//    val p = container.layoutParams as WindowManager.LayoutParams
//    p.flags |= Wind
//}