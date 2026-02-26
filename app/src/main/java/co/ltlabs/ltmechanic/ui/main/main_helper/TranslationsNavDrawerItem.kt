package co.ltlabs.ltmechanic.ui.main.main_helper

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.domain.DynamicMenu
import co.ltlabs.ltmechanic.ui.main.MainActivity
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.getTranslation

fun MainActivity.addItemToDrawer() {
    val menu = navigationView.menu
    val list = if (AuthUtil.role == UserType.LINE_LEADER)
        menuItemLineLeader()
    else
        menuItemMechanic()
    binding.logout.text = languageJsonObject.getTranslation(getString(R.string.log_out))
    list.forEachIndexed { index, i ->
        val title = languageJsonObject.getTranslation(getString(i.titleRes))
        menu.add(i.groupId, i.itemId, i.order, title)
            .apply {
                isCheckable = i.isCheckable
                isChecked = index == 0
            }
    }

    changedViewModel.changeLanguage.observe(this) {
        onTranslateChanged()
    }
}

fun MainActivity.onTranslateChanged() {
    val list = if (AuthUtil.role == UserType.LINE_LEADER)
        menuItemLineLeader()
    else
        menuItemMechanic()
    binding.logout.text = languageJsonObject.getTranslation(getString(R.string.log_out))
    list.forEach {
        navigationView.menu.findItem(it.itemId).title =
            languageJsonObject.getTranslation(getString(it.titleRes))
    }
}

fun menuItemLineLeader(): List<DynamicMenu> {
    return mutableListOf<DynamicMenu>()
        .apply {
            add(DynamicMenu(1, R.id.lineLeaderHomeFragment, 1, R.string.dashboard, true))
            add(DynamicMenu(1, R.id.create_ticket, 2, R.string.create_ticket, false))
            add(
                DynamicMenu(
                    1,
                    R.id.lineLeaderReportedTicketsFragment,
                    3,
                    R.string.reported_tickets,
                    true
                )
            )
            add(
                DynamicMenu(
                    1,
                    R.id.lineLeaderInRepairTicketsFragment,
                    4,
                    R.string.in_repair_tickets,
                    true
                )
            )
            add(
                DynamicMenu(
                    1,
                    R.id.lineLeaderRepairedTicketsFragment,
                    5,
                    R.string.repaired_tickets,
                    true
                )
            )
            add(DynamicMenu(1, R.id.nav_change_over, 6, R.string.change_over, true))
            add(DynamicMenu(1, R.id.query_machine, 7, R.string.query_machine, false))
            add(DynamicMenu(1, R.id.send_request, 8, R.string.send_request, false))
            add(DynamicMenu(1, R.id.changeLanguageFragment, 9, R.string.change_language, true))
            add(DynamicMenu(1, R.id.notificationFragment, 10, R.string.notifications, true))
            add(DynamicMenu(1, R.id.changePasswordFragment, 11, R.string.change_password, true))
            add(DynamicMenu(1, R.id.changeFactoryFragment, 12, R.string.change_factory, true))
            add(DynamicMenu(1, 0, 12, R.string.fake_string, false))
            add(DynamicMenu(1, 0, 13, R.string.fake_string, false))
        }

}

fun menuItemMechanic(): List<DynamicMenu> {
    return mutableListOf<DynamicMenu>().apply {
        add(DynamicMenu(2, R.id.mechanicHomeFragment, 1, R.string.dashboard, true))
        add(
            DynamicMenu(
                2,
                R.id.mechanicReportedTicketsFragment,
                2,
                R.string.reported_tickets,
                true
            )
        )
        add(
            DynamicMenu(
                2,
                R.id.mechanicInRepairTicketsFragment,
                3,
                R.string.in_repair_tickets,
                true
            )
        )
        add(
            DynamicMenu(
                2,
                R.id.mechanicRepairedTicketsFragment,
                4,
                R.string.repaired_tickets,
                true
            )
        )
        add(DynamicMenu(2, R.id.nav_change_over, 5, R.string.change_over, true))
        add(DynamicMenu(2, R.id.query_machine, 6, R.string.query_machine, false))
        add(DynamicMenu(2, R.id.lineStatusFragment, 7, R.string.line_overview, true))
        add(DynamicMenu(2, R.id.replace_machine, 8, R.string.replace_machine, false))
        add(DynamicMenu(2, R.id.setupLineFragment, 9, R.string.setup_line, true))
        add(DynamicMenu(2, R.id.nav_maint, 10, R.string.maintenance, true))
        add(DynamicMenu(2, R.id.move_machine, 11, R.string.move_machine, false))
        add(DynamicMenu(2, R.id.changeLanguageFragment, 12, R.string.change_language, true))
        add(DynamicMenu(2, R.id.notificationFragment, 13, R.string.notifications, true))
        add(DynamicMenu(2, R.id.changePasswordFragment, 14, R.string.change_password, true))
        add(DynamicMenu(2, R.id.changeFactoryFragment, 15, R.string.change_factory, true))
        add(DynamicMenu(2, 0, 15, R.string.fake_string, false))
        add(DynamicMenu(2, 0, 16, R.string.fake_string, false))
    }
}