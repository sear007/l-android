package co.ltlabs.ltmechanic.ui.main.main_helper

import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.ui.main.MainActivity
import co.ltlabs.ltmechanic.util.AuthUtil

const val CREATE_TICKET = "create_ticket"
const val UPDATE_TICKET = "update_ticket"
const val REPLACE_MACHINE = "replace"
const val MOVE_MACHINE = "move_machine"
const val QUERY_MACHINE = "query_machine"
const val SEND_REQUEST = "send_request"

fun listOfMechanicFragmentId(): Set<Int> {
    return setOf(
        R.id.mechanicHomeFragment,
        R.id.mechanicReportedTicketsFragment,
        R.id.mechanicInRepairTicketsFragment,
        R.id.mechanicRepairedTicketsFragment,
        R.id.changeOverFragment,
        R.id.lineStatusFragment,
        R.id.setupLineFragment,
        R.id.maintFragment,
        R.id.changeLanguageFragment,
        R.id.notificationFragment,
        R.id.changePasswordFragment,
        R.id.changeFactoryFragment
    )
}

fun listOfLineLeaderFragmentId(): Set<Int> {
    return setOf(
        R.id.lineLeaderHomeFragment,
        R.id.lineLeaderReportedTicketsFragment,
        R.id.lineLeaderInRepairTicketsFragment,
        R.id.lineLeaderRepairedTicketsFragment,
        R.id.changeOverFragment,
        R.id.changeLanguageFragment,
        R.id.notificationFragment,
        R.id.changePasswordFragment,
        R.id.changeFactoryFragment
    )
}

fun MainActivity.navigateToReplace(
    mfgLineId: Long,
    mfgLine: String,
    station: String,
    machine: String,
    machineId: Long
) {
    val bundle = bundleOf(
        "mfgLineId" to mfgLineId,
        "mfgLine" to mfgLine,
        "station" to station,
        "machine" to machine,
        "machineId" to machineId,
    )
    navController.navigate(R.id.action_global_to_replaceMachineScanDetailsFragment, bundle)
}

fun MainActivity.navigateToMoveMachine(
    machineId: Long,
    machine: String,
    rfid: String,
    subType: String,
    location: String,
    station: String,
    mfgLine: String,
    building: String?,
    buildingId: Int
) {

    val bundle = bundleOf(
        "machineId" to machineId,
        "machine" to machine,
        "rfid" to rfid,
        "subType" to subType,
        "location" to location,
        "station" to station,
        "mfgLine" to mfgLine,
        "buildingName" to building,
        "buildingId" to buildingId
    )
    navController.navigate(R.id.action_global_to_moveMachineFragment, bundle)
}

fun MainActivity.navigateToQueryMachine(machineId: Long, machine: String, rfid: String? = "") {
    val bundle = bundleOf(
        "machineId" to machineId,
        "machine" to machine,
        "rfid" to rfid
    )
    navController.navigate(R.id.action_global_to_queryMachineFragment, bundle)

}

fun MainActivity.navigateReplaceConfirmMachine(
    mfgLineId: Long,
    mfgLine: String,
    machineId: Long,
    machine: String,
    station: String,
    machineIdToCheckIn: Long,
    machineToCheckIn: String,
    scannedMachineStation: String
) {
    val bundle = bundleOf(
        "mfgLineId" to mfgLineId,
        "mfgLine" to mfgLine,
        "machineId" to machineId,
        "machine" to machine,
        "station" to station,
        "machineIdToCheckIn" to machineIdToCheckIn,
        "machineToCheckIn" to machineToCheckIn,
        "scannedMachineStation" to scannedMachineStation
    )
    navController.navigate(R.id.action_global_to_replaceMachineScanDetailsConfirmFragment, bundle)

}

fun MainActivity.navigateToSendRequest(
    machineId: Long,
    machine: String,
    rfid: String,
    subType: String,
    mfgLineId: Long
) {
    val bundle = bundleOf(
        "machineId" to machineId,
        "machine" to machine,
        "rfid" to rfid,
        "subType" to subType,
        "mfgLineId" to mfgLineId,
    )
    navController.navigate(R.id.action_global_to_sendRequestFragment, bundle)
}

fun MainActivity.navigateToCreateTicket(
    machineId: Long,
    machine: String,
    station: String,
    mfgLine: String,
    commonProblems: Long,
    origin: String
) {
    val bundle = bundleOf(
        "machineId" to machineId,
        "machine" to machine,
        "station" to station,
        "mfgLine" to mfgLine,
        "commonProblems" to commonProblems,
        "origin" to origin
    )
    navController.navigate(R.id.action_global_to_createTicketFragment, bundle)
}

fun MainActivity.navigateToAddMachine(
    mfgLineId: Long,
    mfgLine: String,
    station: String,
    rfId: String,
    machine: String
) {
    val bundle = bundleOf(
        "mfgLineId" to mfgLineId,
        "mfgLine" to mfgLine,
        "station" to station,
        "rfId" to rfId,
        "machine" to machine
    )
    navController.navigate(R.id.action_global_to_lineStatusAddMachineScanMachineDetailsFragment, bundle)
}

fun NavController.navDestinationSelected(item: MenuItem): Boolean {
    val navController = this
    val builder = NavOptions.Builder()
        .setLaunchSingleTop(true)
    if (item.order and Menu.CATEGORY_SECONDARY == 0) {
        val id = if (AuthUtil.role == UserType.LINE_LEADER)
            R.id.lineLeaderHomeFragment
        else R.id.mechanicHomeFragment
        builder.setPopUpTo(id, false)
    }
    val options = builder.build()
    return try {
        navController.navigate(item.itemId, null, options)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}
