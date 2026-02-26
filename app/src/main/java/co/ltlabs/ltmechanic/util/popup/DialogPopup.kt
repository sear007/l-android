package co.ltlabs.ltmechanic.util.popup

import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.util.dismissPopup
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.shared.SpinnerViewModel
import kotlinx.android.synthetic.main.popup_spinner_with_search.view.*
import org.json.JSONObject
import javax.inject.Inject

object DialogPopup {

    fun show(viewRoot: View, viewModel: SpinnerViewModel, label: String = "", languageJsonObject: JSONObject) {
        val activity = viewRoot.context as Activity
        val dm = DisplayMetrics()
        activity.windowManager?.defaultDisplay?.getMetrics(dm)
        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .93).toInt()

        PopupWindow(viewRoot.width, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply {
                isOutsideTouchable = true
                isFocusable = true

                val viewLayout = activity.layoutInflater.inflate(
                    R.layout.popup_spinner_with_search,null,false)

                val adapter = SpinnerListAdapter(viewModel)

                viewLayout.closePopup.setOnClickListener {
                    dismissPopup()
                    viewLayout.selectButton.isEnabled = false
                }

                if (label !== "") {
                    viewLayout.labelSelectItem.text = label
                }

                adapter.setOnItemClickListener(object : SpinnerListAdapter.ItemListener{
                    override fun onItemClick(item: SpinnerItem) {
                        viewLayout.selectButton.apply {
                            isEnabled = true
                            background = resources.getDrawable(R.drawable.button, null)
                        }
                    }
                })

                adapter.setOnFilterListener(object : SpinnerListAdapter.ItemFilteringListener{
                    override fun onEventSearchResultNotFound(isNotFound: Boolean) {
                        if (isNotFound) {
                            viewLayout.noResultsTextView.visibility = View.VISIBLE
                            viewLayout.recyclerView.visibility = View.INVISIBLE
                        } else {
                            viewLayout.noResultsTextView.visibility = View.INVISIBLE
                            viewLayout.recyclerView.visibility = View.VISIBLE
                        }
                    }
                })

                viewLayout.recyclerView.apply {
                    this.adapter = adapter
                    this.layoutManager = LinearLayoutManager(viewLayout.context)
                }

                viewLayout.itemSearchEditText.addTextChangedListener (object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) { }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.filter.filter(text)
                    }
                })

                viewLayout.selectButton.setOnClickListener {
                    dismissPopup()
                    viewModel.submitData.postValue(viewModel.selectedItem)
                }

                viewLayout.selectButton.apply {
                    if (viewModel.selectedItem.name != "") {
                        background = resources.getDrawable(R.drawable.button, null)
                    } else {
                        setBackgroundColor(Color.GRAY)
                    }

                    isEnabled = viewModel.selectedItem.name != ""
                }

                with(languageJsonObject) {
                    viewLayout.labelSelectItem.text = getTranslation(viewLayout.labelSelectItem.text.toString())
                    viewLayout.itemSearchEditText.hint = getTranslation(viewLayout.itemSearchEditText.hint.toString())
                    viewLayout.selectButton.text = getTranslation(viewLayout.selectButton.text.toString())
                    viewLayout.noResultsTextView.text = getTranslation(viewLayout.noResultsTextView.text.toString())
                }

                contentView = viewLayout
                update(0, 0, width, height)
                showAtLocation(viewRoot, Gravity.CENTER, 0, -25)
            }
    }
}