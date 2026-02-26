package co.ltlabs.ltmechanic.di

import co.ltlabs.ltmechanic.di.auth.AuthModule
import co.ltlabs.ltmechanic.di.auth.AuthScope
import co.ltlabs.ltmechanic.di.auth.AuthViewModelsModule
import co.ltlabs.ltmechanic.di.main.MainFragmentBuildersModule
import co.ltlabs.ltmechanic.di.main.MainModule
import co.ltlabs.ltmechanic.di.main.MainScope
import co.ltlabs.ltmechanic.di.main.MainViewModelsModule
import co.ltlabs.ltmechanic.di.setup.SetupModule
import co.ltlabs.ltmechanic.di.setup.SetupScope
import co.ltlabs.ltmechanic.di.setup.SetupViewModelsModule
import co.ltlabs.ltmechanic.ui.auth.AuthActivity
import co.ltlabs.ltmechanic.ui.login.LoginActivity
import co.ltlabs.ltmechanic.ui.main.MainActivity
import co.ltlabs.ltmechanic.ui.setup.LoginCompanyActivity
import co.ltlabs.ltmechanic.ui.setup.SetupActivity
import co.ltlabs.ltmechanic.util.notification.PushReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthViewModelsModule::class, AuthModule::class])
    abstract fun contributeAuthActivity(): AuthActivity

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthViewModelsModule::class, AuthModule::class])
    abstract fun contributeLoginActivity(): LoginActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainFragmentBuildersModule::class, MainViewModelsModule::class, MainModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @SetupScope
    @ContributesAndroidInjector(
        modules = [SetupViewModelsModule::class, SetupModule::class])
    abstract fun contributeSetupActivity(): SetupActivity

    @SetupScope
    @ContributesAndroidInjector(
        modules = [SetupViewModelsModule::class, SetupModule::class])
    abstract fun contributeLoginCompanyActivity(): LoginCompanyActivity

    @ContributesAndroidInjector
    abstract fun contributesPushReceiver() : PushReceiver
}