package co.ltlabs.ltmechanic.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.R
import java.util.*
import kotlin.math.roundToInt

class DividerItemDecoration(context: Context?, orientation: Int = VERTICAL) :
    RecyclerView.ItemDecoration() {

    private var divider: Drawable? = null
    private var orientation: Int = 0
    var isShownLastItem: Boolean = true

    init {
        divider = context?.let { ContextCompat.getDrawable(it, R.drawable.divider) }
        setOrientation(orientation)
    }

    private fun setOrientation(orientation: Int) {
        require(!(orientation != HORIZONTAL && orientation != VERTICAL)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL" }
        this.orientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null || divider == null) return
        if (orientation == VERTICAL) drawVertical(c, parent)
        else drawHorizontal(c, parent)
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val childCount: Int = if (isShownLastItem) parent.childCount else parent.childCount - 1
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val decoratedBottom =
                Objects.requireNonNull(parent.layoutManager!!).getDecoratedBottom(child)
            val bottom = decoratedBottom + child.translationY.roundToInt()
            val top = bottom - divider!!.intrinsicHeight
            divider!!.setBounds(left, top, right, bottom)
            divider!!.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        } else {
            top = 0
            bottom = parent.height
        }

        val childCount: Int = if (isShownLastItem) parent.childCount else parent.childCount - 1
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val decoratedRight =
                Objects.requireNonNull(parent.layoutManager!!).getDecoratedRight(child)
            val right = decoratedRight + child.translationX.roundToInt()
            val left = right - divider!!.intrinsicWidth
            divider!!.setBounds(left, top, right, bottom)
            divider!!.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (divider == null) {
            outRect.setEmpty()
            return
        }

        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        val itemCount = state.itemCount

        if (isShownLastItem) {
            if (orientation == VERTICAL) outRect.set(0, 0, 0, divider!!.intrinsicHeight)
            else outRect.set(0, 0, divider!!.intrinsicWidth, 0)
        } else if (itemPosition == itemCount - 1) {
            // We didn't set the last item when isShowInLastItem's value is false.
            outRect.setEmpty()
        } else {
            if (orientation == VERTICAL) outRect.set(0, 0, 0, divider!!.intrinsicHeight)
            else outRect.set(0, 0, divider!!.intrinsicWidth, 0)
        }
    }

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL
    }
}
