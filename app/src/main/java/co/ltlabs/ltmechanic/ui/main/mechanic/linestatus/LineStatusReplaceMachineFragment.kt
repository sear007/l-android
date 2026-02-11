package co.ltlabs.ltmechanic.ui.main.mechanic.linestatus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineStatusReplaceMachineBinding
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusReplaceMachineViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class LineStatusReplaceMachineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private val viewModel: LineStatusReplaceMachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineStatusReplaceMachineViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLineStatusReplaceMachineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel




        return binding.root
    }

}
