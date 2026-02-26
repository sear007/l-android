package co.ltlabs.ltmechanic.ui.dialog.movemachine

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.DialogMoveMachineBinding
import co.ltlabs.ltmechanic.ui.dialog.BaseBSDialog
import co.ltlabs.ltmechanic.util.getTranslation
import org.json.JSONObject
import javax.inject.Inject

class MoveMCBSDialog : BaseBSDialog<DialogMoveMachineBinding>() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private var building: String? = null
    private var buildingId: Int = 0
    private var area: String? = null
    private var areaId: Int = 0
    private var buildingDestinationBSDialog: BuildingDestinationBSDialog? = null
    private var areaDestinationBSDialog: AreaDestinationBSDialog? = null

    private var onOk: ((buildId: Int, areaId: Int, buildingName: String?, areaName: String?) -> Unit)? =
        null

    override fun getLayoutId() = R.layout.dialog_move_machine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            building = args.getString(EXTRA_BUILDING)
            buildingId = args.getInt(EXTRA_BUILDING_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.jTranslate = languageJsonObject
        setDefaultData()
        setupListener()
        setTranslation()
    }

    fun setOnOkClicked(onOk: (buildId: Int, areaId: Int, buildingName: String?, areaName: String?) -> Unit) =
        apply {
            this.onOk = onOk
        }

    private fun setTranslation() {
        binding.apply {
            rootBuilding.hint = "${languageJsonObject.getTranslation("Destination Building")} *"
            rootArea.hint = "${languageJsonObject.getTranslation("Choose an area")} *"
        }
    }

    private fun setDefaultData() {
        building?.let { building ->
            binding.edtBuilding.setText(building)
        }
        area?.let {
            binding.edtArea.setText(area)
        }
    }

    private fun setupListener() {
        binding.apply {
            edtBuilding.setOnClickListener {
                showBuildingDestinationDialog()
            }

            edtArea.setOnClickListener {
                showAreaDestinationDialog()
            }

            tvCancel.setOnClickListener {
                dismiss()
            }

            tvOk.setOnClickListener {
                validateInput()
            }
        }
    }

    private fun validateInput() {
        binding.apply {
            if (edtBuilding.text.toString().isEmpty()) {
                rootBuilding.error = "${languageJsonObject.getTranslation("Destination is required")}!"
                return
            }

            if (edtArea.text.toString().isEmpty()) {
                rootArea.error = "${languageJsonObject.getTranslation("Area is required")}!"
                return
            }

            onOk?.invoke(buildingId, areaId, building, area)
            dismiss()

        }
    }

    private fun showBuildingDestinationDialog() {
        if (buildingDestinationBSDialog == null)
            buildingDestinationBSDialog =
                BuildingDestinationBSDialog.newInstance(building, buildingId)

        if (buildingDestinationBSDialog?.isAdded == false) {
            buildingDestinationBSDialog?.isCancelable = false
            buildingDestinationBSDialog?.show(
                childFragmentManager,
                buildingDestinationBSDialog?.tag
            )

            buildingDestinationBSDialog?.onDismissListener {
                buildingDestinationBSDialog = null
            }

            buildingDestinationBSDialog?.setOnOkClicked { buildingName, buildingId ->
                if (this.buildingId != buildingId) {
                    area = ""
                    areaId = 0
                    binding.edtArea.setText("")
                }
                this.building = buildingName
                this.buildingId = buildingId
                binding.edtBuilding.setText(buildingName)
                binding.rootBuilding.isErrorEnabled = (buildingName == null || buildingName.isEmpty())
            }
        }
    }

    private fun showAreaDestinationDialog() {
        if (areaDestinationBSDialog == null)
            areaDestinationBSDialog = AreaDestinationBSDialog.newInstance(area, areaId, buildingId)

        if (areaDestinationBSDialog?.isAdded == false) {
            areaDestinationBSDialog?.isCancelable = false
            areaDestinationBSDialog?.show(childFragmentManager, areaDestinationBSDialog?.tag)
            areaDestinationBSDialog?.onDismissListener {
                areaDestinationBSDialog = null
            }

            areaDestinationBSDialog?.setOnOkClicked { areaName, areaId ->
                this.area = areaName
                this.areaId = areaId
                binding.edtArea.setText(areaName)
                binding.rootArea.isErrorEnabled = (areaName == null || areaName.isEmpty())
            }
        }

    }

    companion object {
        private const val EXTRA_BUILDING = "EXTRA_BUILDING"
        private const val EXTRA_BUILDING_ID = "EXTRA_BUILDING_ID"

        fun newInstance(building: String?, buildingId: Int) = MoveMCBSDialog()
            .apply {
                arguments = bundleOf(
                    EXTRA_BUILDING to building,
                    EXTRA_BUILDING_ID to buildingId
                )
            }
    }
}