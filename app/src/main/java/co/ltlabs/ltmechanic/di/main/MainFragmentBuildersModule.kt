package co.ltlabs.ltmechanic.di.main

import co.ltlabs.ltmechanic.ui.changeover.COFragment
import co.ltlabs.ltmechanic.ui.changeover.SubCOFragment
import co.ltlabs.ltmechanic.ui.changeover.ViewAttachmentBSDialog
import co.ltlabs.ltmechanic.ui.changeover.detailco.CODetailFragment
import co.ltlabs.ltmechanic.ui.changeover.prepareco.PrepareCOFragment
import co.ltlabs.ltmechanic.ui.changeover.readyco.ReadyCOFragment
import co.ltlabs.ltmechanic.ui.dialog.FindMachineBSDialog
import co.ltlabs.ltmechanic.ui.dialog.maint.MaintChecklistBSDialog
import co.ltlabs.ltmechanic.ui.dialog.movemachine.AreaDestinationBSDialog
import co.ltlabs.ltmechanic.ui.dialog.movemachine.BuildingDestinationBSDialog
import co.ltlabs.ltmechanic.ui.dialog.movemachine.MoveMCBSDialog
import co.ltlabs.ltmechanic.ui.main.ChangeLanguageFragment
import co.ltlabs.ltmechanic.ui.main.ChangePasswordFragment
import co.ltlabs.ltmechanic.ui.main.HomeFragment
import co.ltlabs.ltmechanic.ui.main.ListPopupFragment
import co.ltlabs.ltmechanic.ui.main.filter.FilterLineAndAreaDialog
import co.ltlabs.ltmechanic.ui.main.lineleader.LineLeaderHomeFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.createticket.CreateTicketAttachFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.createticket.CreateTicketFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.createticket.CreateTicketPreviewFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.inrepairtickets.LineLeaderInRepairTicketsFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.repairedtickets.LineLeaderRepairedTicketsFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.reportedtickets.LineLeaderReportedTicketsFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.sendrequest.SendRequestFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.shared.LineLeaderTicketChecklistFragment
import co.ltlabs.ltmechanic.ui.main.lineleader.shared.LineLeaderTicketPreviewFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.MechanicHomeFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.createticket.MechanicCreateTicketAttachFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.createticket.MechanicCreateTicketFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.createticket.MechanicCreateTicketPreviewFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets.MechanicInRepairTicketsChecklistFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets.MechanicInRepairTicketsConfirmFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets.MechanicInRepairTicketsFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets.MechanicInRepairTicketsPreviewFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.linestatus.*
import co.ltlabs.ltmechanic.ui.main.mechanic.maintenance.*
import co.ltlabs.ltmechanic.ui.main.mechanic.repairedtickets.MechanicRepairedTicketsChecklistFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.repairedtickets.MechanicRepairedTicketsFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.replacemachine.ReplaceMachineFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.replacemachine.ReplaceMachineScanDetailsConfirmFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.replacemachine.ReplaceMachineScanDetailsFragment
import co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets.*
import co.ltlabs.ltmechanic.ui.main.mechanic.setupline.*
import co.ltlabs.ltmechanic.ui.main.shared.ChangeFactoryFragment
import co.ltlabs.ltmechanic.ui.main.shared.MoveMachineFragment
import co.ltlabs.ltmechanic.ui.main.shared.NotificationFragment
import co.ltlabs.ltmechanic.ui.main.shared.QueryMachineFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeLineLeaderHomeFragment(): LineLeaderHomeFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicHomeFragment(): MechanicHomeFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeListPopupFragment(): ListPopupFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupLineFragment(): SetupLineFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupLineScanMachineFragment(): SetupLineScanMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupLineMachineInPlaceDetailsFragment(): SetupLineMachineInPlaceDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupLinePlacesFragment(): SetupLinePlacesFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupLineSelectLineFragment(): SetupLineSelectLineFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupLineMachineDetailsWithScanFragment(): SetupLineMachineDetailsWithScanFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusFragment(): LineStatusFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusStationDetailsFragment(): LineStatusStationDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusStationsFragment(): LineStatusStationsFragment

    @ContributesAndroidInjector
    abstract fun contributeReplaceMachineFragment(): ReplaceMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeReplaceMachineScanDetailsFragment(): ReplaceMachineScanDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeReplaceMachineScanDetailsConfirmFragment(): ReplaceMachineScanDetailsConfirmFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusReplaceMachineFragment(): LineStatusReplaceMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusReplaceMachineScanDetailsFragment(): LineStatusReplaceMachineScanDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusReplaceMachineScanDetailsConfirmFragment(): LineStatusReplaceMachineScanDetailsConfirmFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusInsertScanMachineFragment(): LineStatusInsertScanMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusInsertScanMachineDetailsFragment(): LineStatusInsertScanMachineDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusAddMachineScanMachineDetailsFragment(): LineStatusAddMachineScanMachineDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusAddMachineScanMachineFragment(): LineStatusAddMachineScanMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusAddMachineNextMachineAFragment(): LineStatusAddMachineNextMachineAFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusNextMachineAScanMachineFragment(): LineStatusNextMachineAScanMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusNextMachineAScanMachineDetailsFragment(): LineStatusNextMachineAScanMachineDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusInsertNextMachineAScanMachineFragment(): LineStatusInsertNextMachineAScanMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeLineStatusInsertNextMachineScanMachineDetailsFragment(): LineStatusInsertNextMachineScanMachineDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateTicketFragment(): CreateTicketFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateTicketAttachFragment(): CreateTicketAttachFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateTicketPreviewFragment(): CreateTicketPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeLineLeaderReportedTicketsFragment(): LineLeaderReportedTicketsFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsFragment(): MechanicReportedTicketsFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsPreviewFragment(): MechanicReportedTicketsPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsConfirmFragment(): MechanicReportedTicketsConfirmFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsChecklistFragment(): MechanicReportedTicketsChecklistFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsMachineHistoryFragment(): MechanicReportedTicketsMachineHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsAlternativeMachinesFragment(): MechanicReportedTicketsAlternativeMachinesFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicReportedTicketsAlternativeMachineLocationsFragment(): MechanicReportedTicketsAlternativeMachineLocationsFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicInRepairTicketsFragment(): MechanicInRepairTicketsFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicRepairedTicketsFragment(): MechanicRepairedTicketsFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicInRepairTicketsPreviewFragment(): MechanicInRepairTicketsPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicInRepairTicketsConfirmFragment(): MechanicInRepairTicketsConfirmFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicInRepairTicketsChecklistFragment(): MechanicInRepairTicketsChecklistFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicRepairedTicketsChecklistFragment(): MechanicRepairedTicketsChecklistFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicCreateTicketFragment(): MechanicCreateTicketFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicCreateTicketAttachFragment(): MechanicCreateTicketAttachFragment

    @ContributesAndroidInjector
    abstract fun contributeMechanicCreateTicketPreviewFragment(): MechanicCreateTicketPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeLineLeaderInRepairTicketsFragment(): LineLeaderInRepairTicketsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineLeaderRepairedTicketsFragment(): LineLeaderRepairedTicketsFragment

    @ContributesAndroidInjector
    abstract fun contributeLineLeaderTicketPreviewFragment(): LineLeaderTicketPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeLineLineLeaderTicketChecklistFragment(): LineLeaderTicketChecklistFragment

    @ContributesAndroidInjector
    abstract fun contributeMoveMachineFragment(): MoveMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeMaintenance2Fragment(): MaintFragment

    @ContributesAndroidInjector
    abstract fun contributeSubMaintenanceFragment(): SubMaintFragment

    @ContributesAndroidInjector
    abstract fun contributeMaintenancePreviewFragment(): MaintenancePreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeMaintenanceChecklistFragment(): MaintenanceChecklistFragment

    @ContributesAndroidInjector
    abstract fun contributeRepairHistoryFragment(): RepairHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeMaintenanceHistoryFragment(): MaintenanceHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeMaintenanceHistoryChecklistFragment(): MaintenanceHistoryChecklistFragment

    @ContributesAndroidInjector
    abstract fun contributeQueryMachineFragment(): QueryMachineFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationFragment(): NotificationFragment

    @ContributesAndroidInjector
    abstract fun contributeSendRequestFragment(): SendRequestFragment

    @ContributesAndroidInjector
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeChangeFactoryFragment(): ChangeFactoryFragment

    @ContributesAndroidInjector
    abstract fun contributeChangeLanguageFragment(): ChangeLanguageFragment

    @ContributesAndroidInjector
    abstract fun contributeFindMachineBSDialog(): FindMachineBSDialog

    @ContributesAndroidInjector
    abstract fun contributeFilterLineAndAreaDialog(): FilterLineAndAreaDialog

    @ContributesAndroidInjector
    abstract fun contributeChangeOverFragment(): COFragment

    @ContributesAndroidInjector
    abstract fun contributeSubChangeOverFragment(): SubCOFragment

    @ContributesAndroidInjector
    abstract fun contributeCODetailFragment(): CODetailFragment

    @ContributesAndroidInjector
    abstract fun contributePrepareCOFragment(): PrepareCOFragment

    @ContributesAndroidInjector
    abstract fun contributeReadyCOFragment(): ReadyCOFragment

    @ContributesAndroidInjector
    abstract fun contributeViewAttachmentBSDialog(): ViewAttachmentBSDialog

    @ContributesAndroidInjector
    abstract fun contributeMoveMCBSDialog(): MoveMCBSDialog

    @ContributesAndroidInjector
    abstract fun contributeBuildingDestinationBSDialog(): BuildingDestinationBSDialog

    @ContributesAndroidInjector
    abstract fun contributeAreaDestinationBSDialog(): AreaDestinationBSDialog

    @ContributesAndroidInjector
    abstract fun contributeMaintAddCheckListFragment(): MaintAddCheckListFragment

    @ContributesAndroidInjector
    abstract fun contributeMaintChecklistBSDialog(): MaintChecklistBSDialog
}