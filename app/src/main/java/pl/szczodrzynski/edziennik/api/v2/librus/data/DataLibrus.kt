/*
 * Copyright (c) Kuba Szczodrzyński 2019-9-21.
 */

package pl.szczodrzynski.edziennik.api.v2.librus.data

import pl.szczodrzynski.edziennik.App
import pl.szczodrzynski.edziennik.api.interfaces.ProgressCallback
import pl.szczodrzynski.edziennik.api.v2.models.Data
import pl.szczodrzynski.edziennik.datamodels.LoginStore
import pl.szczodrzynski.edziennik.datamodels.Profile

class DataLibrus(app: App, profile: Profile?, loginStore: LoginStore) : Data(app, profile, loginStore) {

    /*    _____           _        _
         |  __ \         | |      | |
         | |__) |__  _ __| |_ __ _| |
         |  ___/ _ \| '__| __/ _` | |
         | |  | (_) | |  | || (_| | |
         |_|   \___/|_|   \__\__,_|*/
    private var mPortalEmail: String? = null
    var portalEmail: String?
        get() { mPortalEmail = mPortalEmail ?: loginStore.getLoginData("email", null); return mPortalEmail }
        set(value) { loginStore.putLoginData("email", value); mPortalEmail = value }
    private var mPortalPassword: String? = null
    var portalPassword: String?
        get() { mPortalPassword = mPortalPassword ?: loginStore.getLoginData("password", null); return mPortalPassword }
        set(value) { loginStore.putLoginData("password", value); mPortalPassword = value }

    private var mPortalAccessToken: String? = null
    var portalAccessToken: String?
        get() { mPortalAccessToken = mPortalAccessToken ?: loginStore.getLoginData("accessToken", null); return mPortalAccessToken }
        set(value) { loginStore.putLoginData("accessToken", value); mPortalAccessToken = value }
    private var mPortalRefreshToken: String? = null
    var portalRefreshToken: String?
        get() { mPortalRefreshToken = mPortalRefreshToken ?: loginStore.getLoginData("refreshToken", null); return mPortalRefreshToken }
        set(value) { loginStore.putLoginData("refreshToken", value); mPortalRefreshToken = value }
    private var mPortalTokenExpiryTime: Long? = null
    var portalTokenExpiryTime: Long
        get() { mPortalTokenExpiryTime = mPortalTokenExpiryTime ?: loginStore.getLoginData("tokenExpiryTime", 0L); return mPortalTokenExpiryTime ?: 0L }
        set(value) { loginStore.putLoginData("tokenExpiryTime", value); mPortalTokenExpiryTime = value }

    /*             _____ _____
             /\   |  __ \_   _|
            /  \  | |__) || |
           / /\ \ |  ___/ | |
          / ____ \| |    _| |_
         /_/    \_\_|   |____*/
    /**
     * A Synergia login, like 1234567u.
     * Used: for login (API Login Method) in Synergia mode.
     * And also in various places in [pl.szczodrzynski.edziennik.api.v2.models.Endpoint]s
     */
    private var mApiLogin: String? = null
    var apiLogin: String?
        get() { mApiLogin = mApiLogin ?: profile?.getStudentData("accountLogin", null); return mApiLogin }
        set(value) { profile?.putStudentData("accountLogin", value) ?: return; mApiLogin = value }
    /**
     * A Synergia password.
     * Used: for login (API Login Method) in Synergia mode.
     */
    private var mApiPassword: String? = null
    var apiPassword: String?
        get() { mApiPassword = mApiPassword ?: profile?.getStudentData("accountPassword", null); return mApiPassword }
        set(value) { profile?.putStudentData("accountPassword", value) ?: return; mApiPassword = value }

    /**
     * A JST login Code.
     * Used only during first login in JST mode.
     */
    private var mApiCode: String? = null
    var apiCode: String?
        get() { mApiCode = mApiCode ?: profile?.getStudentData("accountCode", null); return mApiCode }
        set(value) { profile?.putStudentData("accountCode", value) ?: return; mApiCode = value }
    /**
     * A JST login PIN.
     * Used only during first login in JST mode.
     */
    private var mApiPin: String? = null
    var apiPin: String?
        get() { mApiPin = mApiPin ?: profile?.getStudentData("accountPin", null); return mApiPin }
        set(value) { profile?.putStudentData("accountPin", value) ?: return; mApiPin = value }

    /**
     * A Synergia API access token.
     * Used in all Api Endpoints.
     * Created in Login Method Api.
     * Applicable for all login modes.
     */
    private var mApiAccessToken: String? = null
    var apiAccessToken: String?
        get() { mApiAccessToken = mApiAccessToken ?: profile?.getStudentData("accountToken", null); return mApiAccessToken }
        set(value) { profile?.putStudentData("accountToken", value) ?: return; mApiAccessToken = value }
    /**
     * A Synergia API refresh token.
     * Used when refreshing the [apiAccessToken] in JST, Synergia modes.
     */
    private var mApiRefreshToken: String? = null
    var apiRefreshToken: String?
        get() { mApiRefreshToken = mApiRefreshToken ?: profile?.getStudentData("accountRefreshToken", null); return mApiRefreshToken }
        set(value) { profile?.putStudentData("accountRefreshToken", value) ?: return; mApiRefreshToken = value }
    /**
     * The expiry time for [apiAccessToken], as a UNIX timestamp.
     * Used when refreshing the [apiAccessToken] in JST, Synergia modes.
     * Used when refreshing the [apiAccessToken] in Portal mode ([pl.szczodrzynski.edziennik.api.v2.librus.login.SynergiaTokenExtractor])
     */
    private var mApiTokenExpiryTime: Long? = null
    var apiTokenExpiryTime: Long
        get() { mApiTokenExpiryTime = mApiTokenExpiryTime ?: profile?.getStudentData("accountTokenTime", 0L); return mApiTokenExpiryTime ?: 0L }
        set(value) { profile?.putStudentData("accountTokenTime", value) ?: return; mApiTokenExpiryTime = value }

    /*     ____  _   _
          / __ \| | | |
         | |  | | |_| |__   ___ _ __
         | |  | | __| '_ \ / _ \ '__|
         | |__| | |_| | | |  __/ |
          \____/ \__|_| |_|\___|*/
    var isPremium
        get() = profile?.getStudentData("isPremium", false) ?: false
        set(value) { profile?.putStudentData("isPremium", value) }

}
