package co.ltlabs.ltmechanic.constant

import co.ltlabs.ltmechanic.BaseApplication

object AppConfig {
    var BASE_APPLICATION = BaseApplication()

    var BASE_URL = ""
    var GLOBAL_BASE_URL = ""
    var COMPANY_CODE = ""
    var COMPANY_NAME = ""
    var SELECTED_FACTORY = ""
    private const val GLOBAL_API_PATH = "msv/global-admin/api/v1/"
    const val USER_LOGIN = GLOBAL_API_PATH + "company/userlogin"
    const val TRANSLATION = GLOBAL_API_PATH + "languages/translation"
    const val PRODUCT_ACCESS = GLOBAL_API_PATH + "useraccess"
    const val LANGUAGES = GLOBAL_API_PATH + "languages"
    const val REFRESH_TOKEN = GLOBAL_API_PATH + "auth/refreshtoken"
    const val SWITCH_FACTORY = GLOBAL_API_PATH + "auth/switch-factory"
    const val DEVICE_TOKEN = GLOBAL_API_PATH + "user/device/tokens"
    const val LOGOUT = GLOBAL_API_PATH + "logout"
    var BASE_SOCKET_IO_URL = ""

    var APK_LINK = ""
    var NEW_VERSION = ""

    const val APP_NAME = "LTm"
    const val APP_ALIAS_NAME = "LTmechanic"
    const val GLOBAL_URL = "https://globaladmin-sg.ltlabs.co/msv/global-admin/api/v1/"
    const val PING_URL = "https://globaladmin-sg.ltlabs.co/msv/global-admin/"
    const val COMPANY_LOGIN = "https://dev.ltlabs.co/msv/global-admin/api/v1/"
    const val PREFS_NAME = "ltmechanic"
    const val SP_GLOBAL_BASE_URL = "globalBaseURL"
    const val SP_COMPANY_CODE = "COMPANY_CODE"
    const val SP_COMPANY_BASE_URL = "COMPANY_BASE_URL"
    const val SP_COMPANY_NAME = "COMPANY_NAME"
    const val SP_COMPANY_SOCKET_IO_URL = "SP_COMPANY_SOCKET_IO_URL"
    const val SP_USERNAME = "USERNAME"
    const val SP_PASSWORD = "PASSWORD"
    const val SP_USER_ROLE = "USER_ROLE"
    const val SP_USER_TOKEN = "USER_TOKEN"
    const val SP_FACTORY_ID = "SP_FACTORY_ID"
    const val SP_USER_REFRESH_TOKEN="USER_REFRESH_TOKEN"
    const val SP_SELECTED_LANG = "SELECTED_LANG"
    const val SP_REMEMBER_PWD = "REMEMBER_PWD"
    const val SP_DEVICE_TOKEN = "DEVICE_TOKEN"
    const val EXTRA_TITLE = "EXTRA_TITLE"
    const val EXTRA_BODY = "EXTRA_BODY"
    const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    const val EXTRA_ACTION = "EXTRA_ACTION"
    const val EXTRA_REFERENCE = "EXTRA_REFERENCE"
    const val EXTRA_FACTORY_ID = "EXTRA_FACTORY_ID"
    const val EXTRA_COMPANY_CODE = "EXTRA_COMPANY_CODE"
    const val EXTRA_GET_NOTIFY = "EXTRA_GET_NOTIFY"
    var IS_REFRESHING_TOKEN = 0
}
