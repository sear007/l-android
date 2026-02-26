package co.ltlabs.ltmechanic.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.databinding.FragmentHomeBinding
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.navigate
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.HomeViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

private const val TAG = "HomeFragment"

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onResume() {
        super.onResume()

//        if (AuthUtil.role != UserType.LINE_LEADER) {
//            Log.d(TAG, "onCreateView: navigate to mechanic")
//            navigateToMechanic()
//        } else {
//            Log.d(TAG, "onCreateView: navigate to lineleader")
//            navigateToLineLeader()
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentHomeBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        Log.d(TAG, "onCreateView: token: ${AuthUtil.token}")
        Log.d(TAG, "onCreateView: role: ${AuthUtil.role}")



        binding.viewModel = ViewModelProvider(this, providerFactory).get(HomeViewModel::class.java)

//        binding.btnMechanic.setOnClickListener {
//            navigateToMechanic()
//        }
//
//        binding.btnLineleader.setOnClickListener {
//            navigateToLineLeader()
//        }

        return binding.root
    }

//    private fun navigateToMechanic() {
//        val action = HomeFragmentDirections
//            .actionHomeFragmentToMechanicHomeFragment(
//                "",
//                false,
//                "",
//                ""
//            )
//        navigate(action)
//    }
//
//    private fun navigateToLineLeader() {
//        val action = HomeFragmentDirections
//            .actionHomeFragmentToLineLeaderHomeFragment()
//        navigate(action)
//    }

}
