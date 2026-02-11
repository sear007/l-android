package co.ltlabs.ltmechanic.ui.main.mechanic.setupline

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentSetupLineSelectLineBinding
import co.ltlabs.ltmechanic.ui.adapter.SetupLineSelectLineListAdapter
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineSelectLineViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

private const val TAG = "SelectLineFragment";

/**
 * A simple [Fragment] subclass.
 */
class SetupLineSelectLineFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private val viewModel: SetupLineSelectLineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SetupLineSelectLineViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentSetupLineSelectLineBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        val adapter = SetupLineSelectLineListAdapter(viewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        viewModel.mfgLines.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        viewModel.navigateToSelectedLine.observe(viewLifecycleOwner, Observer {

//            navigateToSetupLine(it.mfgLine)

//            viewModel.displaySetupLineComplete()
        })

        return binding.root
    }

//    private fun navigateToSetupLine(selectedLine: String) {
//        val action = SetupLineSelectLineFragmentDirections.actionSetupLineSelectLineFragmentToSetupLineFragment(selectedLine, 0)
//        navigate(action)
//
//    }

}
