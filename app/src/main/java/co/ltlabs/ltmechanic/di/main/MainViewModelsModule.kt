package co.ltlabs.ltmechanic.di.main

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.di.ViewModelKey
import co.ltlabs.ltmechanic.ui.changeover.COViewModel
import co.ltlabs.ltmechanic.ui.changeover.detailco.CODetailViewModel
import co.ltlabs.ltmechanic.ui.changeover.prepareco.PrepareCOViewModel
import co.ltlabs.ltmechanic.ui.changeover.readyco.ReadyViewModel
import co.ltlabs.ltmechanic.ui.dialog.movemachine.MoveMCViewModel
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.SocketViewModel
import co.ltlabs.ltmechanic.ui.main.filter.FilterViewModel
import co.ltlabs.ltmechanic.ui.main.main_helper.FireLanguageChangedViewModel
import co.ltlabs.ltmechanic.ui.main.mechanic.maintenance.MaintViewModel
import co.ltlabs.ltmechanic.viewmodels.PerAccessViewModel
import co.ltlabs.ltmechanic.viewmodels.main.HomeViewModel
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.*
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.*
import co.ltlabs.ltmechanic.viewmodels.shared.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(LineLeaderHomeViewModel::class)
    abstract fun bindLineLeaderHomeViewModel(viewModel: LineLeaderHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicHomeViewModel::class)
    abstract fun bindMechanicHomeViewModel(viewModel: MechanicHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineViewModel::class)
    abstract fun bindLineViewModel(viewModel: LineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MachineViewModel::class)
    abstract fun bindMachineViewModel(viewModel: MachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupLineViewModel::class)
    abstract fun bindSetupViewModel(viewModel: SetupLineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupLineScanMachineViewModel::class)
    abstract fun bindSetupLineScanMachineViewModel(viewModel: SetupLineScanMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupLineMachineInPlaceDetailsViewModel::class)
    abstract fun bindSetupLineMachineInPlaceDetailsViewModel(viewModel: SetupLineMachineInPlaceDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupLinePlacesViewModel::class)
    abstract fun bindSetupLinePlacesViewModel(viewModel: SetupLinePlacesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupLineSelectLineViewModel::class)
    abstract fun bindSetupLineSelectLineViewModel(viewModel: SetupLineSelectLineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupLineMachineInPlaceDetailsWithScanViewModel::class)
    abstract fun bindSetupLineMachineInPlaceDetailsWithScanViewModel(viewModel: SetupLineMachineInPlaceDetailsWithScanViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusViewModel::class)
    abstract fun bindLineStatusViewModel(viewModel: LineStatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusStationDetailsViewModel::class)
    abstract fun bindLineStatusStationDetailsViewModel(viewModel: LineStatusStationDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusStationsViewModel::class)
    abstract fun bindLineStatusStationsViewModel(viewModel: LineStatusStationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReplaceMachineViewModel::class)
    abstract fun bindReplaceMachineViewModel(viewModel: ReplaceMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReplaceMachineScanDetailsViewModel::class)
    abstract fun bindReplaceMachineScanDetailsViewModel(viewModel: ReplaceMachineScanDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReplaceMachineScanDetailsConfirmViewModel::class)
    abstract fun bindReplaceMachineScanDetailsConfirmViewModel(viewModel: ReplaceMachineScanDetailsConfirmViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusReplaceMachineViewModel::class)
    abstract fun bindLineStatusReplaceMachineViewModel(viewModel: LineStatusReplaceMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusReplaceMachineScanDetailsViewModel::class)
    abstract fun bindLineStatusReplaceMachineScanDetailsViewModel(viewModel: LineStatusReplaceMachineScanDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusReplaceMachineScanDetailsConfirmViewModel::class)
    abstract fun bindLineStatusReplaceMachineScanDetailsConfirmViewModel(viewModel: LineStatusReplaceMachineScanDetailsConfirmViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusInsertScanMachineViewModel::class)
    abstract fun bindLineStatusInsertScanMachineViewModel(viewModel: LineStatusInsertScanMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusInsertScanMachineDetailsViewModel::class)
    abstract fun bindLineStatusInsertScanMachineDetailsViewModel(viewModel: LineStatusInsertScanMachineDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusAddMachineScanMachineViewModel::class)
    abstract fun bindLineStatusAddMachineScanMachineViewModel(viewModel: LineStatusAddMachineScanMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusAddMachineScanMachineDetailsViewModel::class)
    abstract fun bindLineStatusAddMachineScanMachineDetailsViewModel(viewModel: LineStatusAddMachineScanMachineDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusAddMachineNextMachineAViewModel::class)
    abstract fun bindLineLineStatusAddMachineNextMachineAViewModel(viewModel: LineStatusAddMachineNextMachineAViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusNextMachineAScanMachineDetailsViewModel::class)
    abstract fun bindLineLineStatusNextMachineAScanMachineDetailsViewModel(viewModel: LineStatusNextMachineAScanMachineDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusNextMachineAScanMachineViewModel::class)
    abstract fun bindLineLineStatusNextMachineAScanMachineViewModel(viewModel: LineStatusNextMachineAScanMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusInsertNextMachineAScanMachineViewModel::class)
    abstract fun bindLineStatusInsertNextMachineAScanMachineViewModel(viewModel: LineStatusInsertNextMachineAScanMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineStatusInsertNextMachineScanMachineDetailsViewModel::class)
    abstract fun bindLineStatusInsertNextMachineScanMachineDetailsViewModel(viewModel: LineStatusInsertNextMachineScanMachineDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateTicketViewModel::class)
    abstract fun bindCreateTicketViewModel(viewModel: CreateTicketViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TicketViewModel::class)
    abstract fun bindTicketViewModel(viewModel: TicketViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateTicketAttachViewModel::class)
    abstract fun bindCreateTicketAttachViewModel(viewModel: CreateTicketAttachViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateTicketPreviewViewModel::class)
    abstract fun bindCreateTicketPreviewViewModel(viewModel: CreateTicketPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StationViewModel::class)
    abstract fun bindStationViewModel(viewModel: StationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineLeaderReportedTicketsViewModel::class)
    abstract fun bindLineLeaderReportedTicketsViewModel(viewModel: LineLeaderReportedTicketsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsViewModel::class)
    abstract fun bindMechanicReportedTicketsViewModel(viewModel: MechanicReportedTicketsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsPreviewViewModel::class)
    abstract fun bindMechanicReportedTicketsPreviewViewModel(viewModel: MechanicReportedTicketsPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsConfirmViewModel::class)
    abstract fun bindMechanicReportedTicketsConfirmViewModel(viewModel: MechanicReportedTicketsConfirmViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsChecklistViewModel::class)
    abstract fun bindMechanicReportedTicketsChecklistViewModel(viewModel: MechanicReportedTicketsChecklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsAlternativeMachineLocationsViewModel::class)
    abstract fun bindMechanicReportedTicketsAlternativeMachineLocationsViewModel(viewModel: MechanicReportedTicketsAlternativeMachineLocationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsAlternativeMachinesViewModel::class)
    abstract fun bindMechanicReportedTicketsAlternativeMachinesViewModel(viewModel: MechanicReportedTicketsAlternativeMachinesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicReportedTicketsMachineHistoryViewModel::class)
    abstract fun bindMechanicReportedTicketsMachineHistoryViewModel(viewModel: MechanicReportedTicketsMachineHistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AttachmentViewModel::class)
    abstract fun bindAttachmentViewModel(viewModel: AttachmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReferenceViewModel::class)
    abstract fun bindReferenceViewModel(viewModel: ReferenceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SpinnerViewModel::class)
    abstract fun bindSpinnerViewModel(viewModel: SpinnerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SolutionViewModel::class)
    abstract fun bindSolutionViewModel(viewModel: SolutionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProblemViewModel::class)
    abstract fun bindProblemViewModel(viewModel: ProblemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicRepairedTicketsViewModel::class)
    abstract fun bindMechanicRepairedTicketsViewModel(viewModel: MechanicRepairedTicketsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicInRepairTicketsViewModel::class)
    abstract fun bindMechanicInRepairTicketsViewModel(viewModel: MechanicInRepairTicketsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicInRepairTicketsPreviewViewModel::class)
    abstract fun bindMechanicInRepairTicketsPreviewViewModel(viewModel: MechanicInRepairTicketsPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicInRepairTicketsConfirmViewModel::class)
    abstract fun bindMechanicInRepairTicketsConfirmViewModel(viewModel: MechanicInRepairTicketsConfirmViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicInRepairTicketsChecklistViewModel::class)
    abstract fun bindMechanicInRepairTicketsChecklistViewModel(viewModel: MechanicInRepairTicketsChecklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicRepairedTicketsChecklistViewModel::class)
    abstract fun bindMechanicRepairedTicketsChecklistViewModel(viewModel: MechanicRepairedTicketsChecklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicCreateTicketViewModel::class)
    abstract fun bindMechanicCreateTicketViewModel(viewModel: MechanicCreateTicketViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicCreateTicketAttachViewModel::class)
    abstract fun bindMechanicCreateTicketAttachViewModel(viewModel: MechanicCreateTicketAttachViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MechanicCreateTicketPreviewViewModel::class)
    abstract fun bindMechanicCreateTicketPreviewViewModel(viewModel: MechanicCreateTicketPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineLeaderInRepairTicketsViewModel::class)
    abstract fun bindLineLeaderInRepairTicketsViewModel(viewModel: LineLeaderInRepairTicketsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineLeaderRepairedTicketsViewModel::class)
    abstract fun bindLineLeaderRepairedTicketsViewModel(viewModel: LineLeaderRepairedTicketsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineLeaderTicketPreviewViewModel::class)
    abstract fun bindLineLeaderTicketPreviewViewModel(viewModel: LineLeaderTicketPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineLeaderTicketChecklistViewModel::class)
    abstract fun bindLineLeaderTicketChecklistViewModel(viewModel: LineLeaderTicketChecklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChecklistViewModel::class)
    abstract fun bindChecklistViewModel(viewModel: ChecklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MoveMachineViewModel::class)
    abstract fun bindMoveMachineViewModel(viewModel: MoveMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenanceViewModel::class)
    abstract fun bindMaintenanceViewModel(viewModel: MaintenanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenancePreviewViewModel::class)
    abstract fun bindMaintenancePreviewViewModel(viewModel: MaintenancePreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenanceChecklistViewmodel::class)
    abstract fun bindMaintenanceChecklistViewmodel(viewModel: MaintenanceChecklistViewmodel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenanceRepairHistoryViewModel::class)
    abstract fun bindMaintenanceRepairHistoryViewModel(viewModel: MaintenanceRepairHistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenanceHistoryViewModel::class)
    abstract fun bindMaintenanceHistoryViewModel(viewModel: MaintenanceHistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenanceHistoryChecklistViewModel::class)
    abstract fun bindMaintenanceHistoryChecklistViewModel(viewModel: MaintenanceHistoryChecklistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QueryMachineViewModel::class)
    abstract fun bindQueryMachineViewModel(viewModel: QueryMachineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    abstract fun bindNotificationViewModel(viewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SendRequestViewModel::class)
    abstract fun bindSendRequestViewModel(viewModel: SendRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordViewModel::class)
    abstract fun bindChangePasswordViewModel(viewModel: ChangePasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangeFactoryViewModel::class)
    abstract fun bindChangeFactoryViewModel(viewModel: ChangeFactoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangeLanguageViewModel::class)
    abstract fun bindChangeLanguageViewModel(viewModel: ChangeLanguageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FireLanguageChangedViewModel::class)
    abstract fun bindFireLanguageChangedViewModel(viewModel: FireLanguageChangedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(COViewModel::class)
    abstract fun bindChangeOverViewModel(viewModel: COViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SocketViewModel::class)
    abstract fun bindSocketViewModel(viewModel: SocketViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PerAccessViewModel::class)
    abstract fun bindPerAccessViewModel(viewModel: PerAccessViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    abstract fun bindDashboardViewModel(viewModel: DashboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FilterViewModel::class)
    abstract fun bindFilterViewModel(viewModel: FilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CODetailViewModel::class)
    abstract fun bindCODetailViewModel(viewModel: CODetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PrepareCOViewModel::class)
    abstract fun bindPrepareCOViewModel(viewModel: PrepareCOViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReadyViewModel::class)
    abstract fun bindReadyViewModel(viewModel: ReadyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MoveMCViewModel::class)
    abstract fun bindMoveMCViewModel(viewModel: MoveMCViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintViewModel::class)
    abstract fun bindMaintViewModel(viewModel: MaintViewModel): ViewModel

}