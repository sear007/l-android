package co.ltlabs.ltmechanic.ui.main.filter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.DialogFilterLinesAreasBinding
import co.ltlabs.ltmechanic.domain.Areas
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.ui.dialog.BaseBSDialog
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class FilterLineAndAreaDialog : BaseBSDialog<DialogFilterLinesAreasBinding>() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var linesAdapter: LinesAdapter

    @Inject
    lateinit var areasAdapter: AreasAdapter

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private val viewModel: FilterViewModel by viewModels { providerFactory }

    private var linesSelectedCount = 0
    private var areasSelectedCount = 0
    private lateinit var linesText: String
    private lateinit var areasText: String

    private var selectedCallback: ((areas: List<Areas>?, lines: List<MfgLine>?, checkAllFilter: Boolean) -> Unit)? =
        null

    override fun getLayoutId(): Int {
        return R.layout.dialog_filter_lines_areas
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTranslation()
        setupLinesList()
        setupAreasList()
        setupListener()
        onTabLinesActive()
    }

    fun onSelect(item: (areas: List<Areas>?, lines: List<MfgLine>?, checkAllFilter: Boolean) -> Unit) {
        this.selectedCallback = item
    }

    private fun setupLinesList() {
        viewModel.getLinesInAreas()
        lifecycleScope.launchWhenCreated {
            viewModel.lines.collectLatest {
                if (it.status == Resource.Status.SUCCESS) {
                    val list = it.data ?: return@collectLatest
                    linesAdapter.modifyList(list)
                    binding.rvLines.adapter = linesAdapter
                    updateTabLinesCount(list)
                    showNoRecordMessage()
                }
            }
        }

        linesAdapter.setOnItemClick {
            updateTabLinesCount()
        }
    }

    private fun setupAreasList() {
        viewModel.getAreasNoLines()
        lifecycleScope.launchWhenCreated {
            viewModel.areasNoLines.collectLatest {
                if (it.status == Resource.Status.SUCCESS) {
                    val list = it.data ?: return@collectLatest
                    areasAdapter.modifyList(list)
                    binding.rvAreas.adapter = areasAdapter
                    updateTabAreasCount()
                    showNoRecordMessage()
                }
            }
        }

        areasAdapter.setOnItemClick {
            updateTabAreasCount()
        }
    }

    private fun setTranslation() {
        linesText = languageJsonObject.getTranslation("LINES")
        areasText = languageJsonObject.getTranslation("AREAS")
        binding.apply {
            jTranslate = languageJsonObject
            lines = linesText
            areas = areasText
        }
    }

    private fun setupListener() {
        binding.tvLinesSelectAll.setOnClickListener { view ->
            view.isSelected = !view.isSelected
            val list = linesAdapter.currentList
            list.map {
                it.checked = view.isSelected
            }
            updateTabLinesCount(list)
            linesAdapter.submitList(list)
            linesAdapter.notifyDataSetChanged()
        }

        binding.tvAreasSelectAll.setOnClickListener { view ->
            view.isSelected = !view.isSelected
            val list = areasAdapter.currentList
            list.map {
                it.isSelected = view.isSelected
            }
            updateTabAreasCount(list)
            areasAdapter.submitList(list)
            areasAdapter.notifyDataSetChanged()
        }

        binding.tvTabLines.setOnClickListener {
            onTabLinesActive()
        }

        binding.tvTabAreas.setOnClickListener {
            onTabAreasActive()
        }

        binding.closePopupMC.setOnClickListener {
            dismiss()
        }

        binding.btnMCSelectLine.setOnClickListener {
            val l = linesAdapter.currentList
            val a = areasAdapter.currentList

            val checkAllLine = l.filter { it.checked == false }
            val checkAllArea = a.filter { !it.isSelected }
            val checkAllFilter: Boolean = checkAllLine.isEmpty() && checkAllArea.isEmpty()
            // pass data only checked line and area
            selectedCallback?.invoke(
                a.filter { it.isSelected },
                l.filter { it.checked == true }, checkAllFilter
            )
            dismiss()
        }

        binding.linesearchEditTextMC.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                areasAdapter.filter(p0) {
                    binding.tvAresNoRecord.isVisible =
                        binding.tvTabAreas.isSelected && it.isEmpty()
                    updateTabAreasCount(it)

                }

                linesAdapter.filter(p0) {
                    binding.tvLinesNoRecord.isVisible =
                        binding.tvTabLines.isSelected && it.isEmpty()
                    updateTabLinesCount(it)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun onTabLinesActive() {
        binding.apply {
            // Lines
            tvTabLines.isSelected = true
            tvLinesSelectAll.isVisible = true
            rvLines.visibility = if (tvTabLines.isSelected) View.VISIBLE else View.INVISIBLE

            // Areas
            tvTabAreas.isSelected = false
            tvAreasSelectAll.isVisible = false
            rvAreas.visibility = if (tvTabAreas.isSelected) View.VISIBLE else View.INVISIBLE

            showNoRecordMessage()
        }
    }

    private fun onTabAreasActive() {
        binding.apply {
            // Lines
            tvTabLines.isSelected = false
            tvLinesSelectAll.isVisible = false
            rvLines.visibility = if (tvTabLines.isSelected) View.VISIBLE else View.INVISIBLE

            // Areas
            tvTabAreas.isSelected = true
            tvAreasSelectAll.isVisible = true
            rvAreas.visibility = if (tvTabAreas.isSelected) View.VISIBLE else View.INVISIBLE

            showNoRecordMessage()
        }
    }

    private fun updateTabLinesCount(list: List<MfgLine> = linesAdapter.currentList) {
        lifecycleScope.launch(Dispatchers.IO) {
            linesSelectedCount = 0
            list.map {
                if (it.checked == true) linesSelectedCount++
            }
            withContext(Dispatchers.Main) {
                binding.tvTabLines.text = "$linesText ($linesSelectedCount)"
                binding.tvLinesSelectAll.isSelected =
                    linesSelectedCount == list.size && linesSelectedCount > 0
            }
        }
    }

    private fun updateTabAreasCount(list: List<Areas> = areasAdapter.currentList) {
        lifecycleScope.launch(Dispatchers.IO) {
            areasSelectedCount = 0
            list.map {
                if (it.isSelected) areasSelectedCount++
            }
            withContext(Dispatchers.Main) {
                binding.tvTabAreas.text = "$areasText ($areasSelectedCount)"
                binding.tvAreasSelectAll.isSelected =
                    areasSelectedCount == list.size && areasSelectedCount > 0
            }
        }
    }

    private fun showNoRecordMessage() {
        binding.apply {
            tvLinesNoRecord.isVisible =
                tvTabLines.isSelected && linesAdapter.currentList.isEmpty()

            tvAresNoRecord.isVisible =
                tvTabAreas.isSelected && areasAdapter.currentList.isEmpty()
        }
    }
}