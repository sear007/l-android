package co.ltlabs.ltmechanic.util

import android.content.Context
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import co.ltlabs.ltmechanic.R
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

private const val TAG = "SnackBarExtensions";

enum class SnackBarActions { ADD_MACHINE, REPLACE_MACHINE, KEEP_EMPTY, NONE }

const val SNACK_BAR_ACTION_ADD_MACHINE = "add_machine"
const val SNACK_BAR_ACTION_REPLACE_MACHINE = "replace_machine"
const val SNACK_BAR_ACTION_REMOVE_MACHINE = "remove_machine"
const val SNACK_BAR_ACTION_KEEP_EMPTY = "keep_empty"
const val SNACK_BAR_ACTION_KEEP_EMPTY_INSERT = "keep_empty_insert"
const val SNACK_BAR_ACTION_INSERT = "insert"
const val SNACK_BAR_ACTION_NONE = "none"
const val SNACK_BAR_ACTION_CANCEL_TICKET = "cancel_ticket"
const val SNACK_BAR_ACTION_REOPEN_TICKET = "reopen_ticket"
const val SNACK_BAR_ACTION_CREATE_TICKET = "create_ticket"
const val SNACK_BAR_ACTION_CLOSE_TICKET = "close_ticket"
const val SNACK_BAR_ACTION_MOVE_MACHINE = "move_machine"
const val SNACK_BAR_ACTION_COMPLETED_TICKET = "completed_ticket"
const val SNACK_BAR_ACTION_IN_PROGRESS_TICKET = "inprogress_ticket"

fun CoordinatorLayout.showSnackbar(message: String, action: String = "OKAY") {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)

    snackbar.setAction(LanguageUtil.languageJsonObject.getTranslation(action)) {
        snackbar.dismiss()
    }

    Log.d(TAG, "showSnackbar: action: $action")

    snackbar.show()
}

fun showSnackBar(view: View, message: String, action: String = "OKAY") {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)

    snackBar.setAction(LanguageUtil.languageJsonObject.getTranslation(action)) {
        snackBar.dismiss()
    }
    snackBar.show()
}