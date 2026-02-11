package co.ltlabs.ltmechanic.di.main

import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.network.main.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

@Module
class MainModule @Inject constructor() {

    companion object {

        @MainScope
        @Provides
        fun provideMachineApi(@Named(AppModule.MACHINE) retrofit: Retrofit): MachineApi =
            retrofit.create(MachineApi::class.java)

        @MainScope
        @Provides
        fun provideLineApi(@Named(AppModule.LINE) retrofit: Retrofit): LineApi =
            retrofit.create(LineApi::class.java)

        @MainScope
        @Provides
        fun provideReferenceApi(@Named(AppModule.REFERENCE) retrofit: Retrofit): ReferenceApi =
            retrofit.create(ReferenceApi::class.java)

        @MainScope
        @Provides
        fun provideTicketApi(@Named(AppModule.TICKET) retrofit: Retrofit): TicketApi =
            retrofit.create(TicketApi::class.java)

        @MainScope
        @Provides
        fun provideAuth2Api(@Named(AppModule.AUTH2) retrofit: Retrofit): Auth2Api =
            retrofit.create(Auth2Api::class.java)
    }
}