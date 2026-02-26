package co.ltlabs.ltmechanic.ui.main.lineleader.shared

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentLineLeaderTicketChecklistBinding
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderTicketChecklistListAdapter
import co.ltlabs.ltmechanic.util.ConnectionUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderTicketChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "LLTicketChecklist";

class LineLeaderTicketChecklistFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineLeaderTicketChecklistViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineLeaderTicketChecklistViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val args: LineLeaderTicketChecklistFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLineLeaderTicketChecklistBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

//                    ticketViewModel.getTicketDetailsById(args.ticketId)
                    ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

//        ticketViewModel.getTicketDetailsById(args.ticketId)
        ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

        val adapter = LineLeaderTicketChecklistListAdapter()

        binding.toolBarTitleTextView.text = args.ticketNo

        with(languageJsonObject) {
            with(binding) {

            }
        }

        ticketViewModel.checkList.observe(viewLifecycleOwner, Observer {checkList ->
            if (checkList != null) {

//                binding.stepsTextView.text = getString(
//                    R.string.checklist_steps,
//                    checkList.filter { it.checked }.size.toString(),
//                    checkList.size.toString()
//                )

                binding.stepsTextView.text = "${checkList.filter { it.checked }.size}/${checkList.size} ${languageJsonObject.getTranslation("STEPS")}"

                adapter.data = checkList

                ticketViewModel.checklistComplete()
            }
        })

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                labelTitleChecklists.text = getTranslation(labelTitleChecklists.text.toString())
            }
        }
        // End translation

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

}
