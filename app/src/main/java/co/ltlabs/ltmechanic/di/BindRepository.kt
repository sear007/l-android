package co.ltlabs.ltmechanic.di

import co.ltlabs.ltmechanic.repository.areas.AreasRepo
import co.ltlabs.ltmechanic.repository.areas.AreasRepoImpl
import co.ltlabs.ltmechanic.repository.co.CORepository
import co.ltlabs.ltmechanic.repository.co.CORepositoryImpl
import co.ltlabs.ltmechanic.repository.lines.LinesRepository
import co.ltlabs.ltmechanic.repository.lines.LinesRepositoryImpl
import co.ltlabs.ltmechanic.repository.maintenance.MaintRepository
import co.ltlabs.ltmechanic.repository.maintenance.MaintRepositoryImpl
import co.ltlabs.ltmechanic.repository.peraccess.PerAccessRepo
import co.ltlabs.ltmechanic.repository.peraccess.PerAccessRepoImpl
import co.ltlabs.ltmechanic.repository.tickets.TicketsRepository
import co.ltlabs.ltmechanic.repository.tickets.TicketsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BindRepository {

    @Binds
    abstract fun bindPerAccessRepository(repository: PerAccessRepoImpl): PerAccessRepo

    @Binds
    abstract fun bindCORepository(repository: CORepositoryImpl): CORepository

    @Binds
    abstract fun bindAreasRepository(repo: AreasRepoImpl): AreasRepo

    @Binds
    abstract fun bindLinesRepository(repo: LinesRepositoryImpl): LinesRepository

    @Binds
    abstract fun bindTicketRepository(repo: TicketsRepositoryImpl): TicketsRepository

    @Binds
    abstract fun bindMaintRepository(repo: MaintRepositoryImpl): MaintRepository

}
