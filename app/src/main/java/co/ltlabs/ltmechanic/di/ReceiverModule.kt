package co.ltlabs.ltmechanic.di

import co.ltlabs.ltmechanic.service.WifiConnectivityReceiver
import co.ltlabs.ltmechanic.service.WifiScannerReceiver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ReceiverModule {

    @Provides
    fun provideWifiConnectivityReceiver()= WifiConnectivityReceiver()

    @Provides
    fun provideWifiScannerReceiver() = WifiScannerReceiver()
 }