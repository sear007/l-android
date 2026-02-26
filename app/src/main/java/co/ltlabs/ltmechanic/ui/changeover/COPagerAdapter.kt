package co.ltlabs.ltmechanic.ui.changeover

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class COPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycleOwner: Lifecycle,
    private val fragments: List<Fragment>,
    private val titles: List<String>
) : FragmentStateAdapter(fragmentManager, lifecycleOwner) {

    override fun getItemCount() = titles.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getTitle(position: Int) = titles[position]
}