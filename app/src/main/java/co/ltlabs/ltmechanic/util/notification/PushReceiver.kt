package co.ltlabs.ltmechanic.util.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.ltlabs.ltmechanic.BaseApplication
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.ui.main.MainActivity
import co.ltlabs.ltmechanic.ui.setup.LoginCompanyActivity
import co.ltlabs.ltmechanic.util.SharePrefUtil
import co.ltlabs.ltmechanic.util.getTranslation
import dagger.android.AndroidInjection
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class PushReceiver : BroadcastReceiver() {

    @Inject
    lateinit var jsonObject: JSONObject

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        val factoryId = intent.getIntExtra(NotificationClient.KEY_FACTORY_ID, -2)
        val companyCode = intent.getStringExtra(NotificationClient.KEY_COMPANY_CODE)

        /**
         * Allow to show notification in cases:
         *  - notification's companyCode is equal to app's companyCode
         *  - notification's factoryId is equal to app's factoryId
         */
        if (companyCode != SharePrefUtil.getString(
                AppConfig.SP_COMPANY_CODE, "1"
            ) || factoryId != SharePrefUtil.getInt(AppConfig.SP_FACTORY_ID, -1)
        ) return

        val t = intent.getStringExtra(NotificationClient.KEY_TITLE) ?: ""
        var body = intent.getStringExtra(NotificationClient.KEY_BODY) ?: ""
        val action = intent.getStringExtra(NotificationClient.KEY_ACTION) ?: ""
        val reference = intent.getStringExtra(NotificationClient.KEY_REFERENCE) ?: ""

        val title = jsonObject.getTranslation(t)
        val ticketNo = "Ticket No"
        val machine = "Machine"
        val problem = "Problem"
        val solution = "Solution"
        val duration = "Duration"
        val mechanic = "Mechanic"

        if (body.contains(ticketNo)) body =
            body.replace(ticketNo, jsonObject.getTranslation(ticketNo))
        if (body.contains(machine)) body = body.replace(machine, jsonObject.getTranslation(machine))
        if (body.contains(problem)) body = body.replace(problem, jsonObject.getTranslation(problem))
        if (body.contains(solution)) body =
            body.replace(solution, jsonObject.getTranslation(solution))
        if (body.contains(duration)) body =
            body.replace(duration, jsonObject.getTranslation(duration))
        if (body.contains(mechanic)) body =
            body.replace(mechanic, jsonObject.getTranslation(mechanic))

        val pushIntent = if (BaseApplication.isOnMainActivity) {
            // for app is using
            Intent(context, MainActivity::class.java).apply {
                putExtra(AppConfig.EXTRA_REFERENCE, reference)
                putExtra(AppConfig.EXTRA_ACTION, action)
                putExtra(AppConfig.EXTRA_FACTORY_ID, factoryId)
                putExtra(AppConfig.EXTRA_COMPANY_CODE, companyCode)
            }
        } else {
            // for app is not using
            Intent(context, LoginCompanyActivity::class.java).apply {
                putExtra(NotificationClient.KEY_TITLE, title)
                putExtra(NotificationClient.KEY_BODY, body)
                putExtra(NotificationClient.KEY_REFERENCE, reference)
                putExtra(NotificationClient.KEY_ACTION, action)
                putExtra(NotificationClient.KEY_FACTORY_ID, factoryId)
                putExtra(NotificationClient.KEY_COMPANY_CODE, companyCode)
            }
        }

        NotificationClient.sendNotification(
            context, title.ifEmpty { "New Cancelled Ticket" }, body, action, pushIntent
        )
    }
}