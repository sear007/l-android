package co.ltlabs.ltmechanic

import co.ltlabs.ltmechanic.constant.AppConfig.BASE_APPLICATION
import co.ltlabs.ltmechanic.di.DaggerAppComponent
import co.ltlabs.ltmechanic.util.notification.NotificationClient
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import org.json.JSONObject
import timber.log.Timber
import timber.log.Timber.DebugTree


class BaseApplication : DaggerApplication() {

    companion object {
        var isOnMainActivity: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        BASE_APPLICATION = this
        NotificationClient.stopAudio()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    var languageJsonObject = JSONObject()

    // Login Screen
    var loginScreenLabelWelcomeTo = "Welcome to"
    var loginScreenLabelLTMechanic = "LT Mechanic"
    var loginScreenLabelUsername = "username"
    var loginScreenLabelPassword = "password"
    var loginScreenButtonLogin = "LOGIN"

    // Line Leader Home
    var lineLeaderHomeButtonCreateTicket = "CREATE TICKET"
    var lineLeaderHomeButtonReportedTickets = "REPORTED TICKET"
    var lineLeaderHomeButtonInRepairTickets = "IN-REPAIR TICKETS"
    var lineLeaderHomeButtonRepairedTickets = "REPAIRED TICKETS"
    var lineLeaderHomeButtonMachines = "MACHINES"
    var lineLeaderHomeButtonQueryMachine = "QUERY MACHINE"
    var lineLeaderHomeButtonSendRequest = "SEND REQUEST"

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().application(this).build()
}