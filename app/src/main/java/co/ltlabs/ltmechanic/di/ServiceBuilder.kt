package co.ltlabs.ltmechanic.di

import co.ltlabs.ltmechanic.service.DeviceTokenService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ServiceBuilder {

    @ContributesAndroidInjector
    fun bindDeviceTokenService(): DeviceTokenService

}