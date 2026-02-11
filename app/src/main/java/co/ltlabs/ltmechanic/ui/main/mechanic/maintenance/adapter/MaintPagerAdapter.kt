package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.ltlabs.ltmechanic.R

class MaintPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycleOwner: Lifecycle,
    private val context: Context,
    private val fragments: List<Fragment>,
    private val titles: List<String>
) : FragmentStateAdapter(fragmentManager, lifecycleOwner) {

    fun getTabView(position: Int): View {
        val view = View.inflate(context, R.layout.item_text_only, null)
        view.findViewById<TextView>(R.id.tv_value).text = titles[position]
        return view
    }

    override fun getItemCount() = titles.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getTitle(position: Int) = titles[position]
}