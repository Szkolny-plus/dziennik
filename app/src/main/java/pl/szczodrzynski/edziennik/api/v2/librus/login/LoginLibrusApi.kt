/*
 * Copyright (c) Kuba Szczodrzyński 2019-9-20.
 */

package pl.szczodrzynski.edziennik.api.v2.librus.login

import com.google.gson.JsonObject
import im.wangchao.mhttp.Request
import im.wangchao.mhttp.Response
import im.wangchao.mhttp.body.MediaTypeUtils
import im.wangchao.mhttp.callback.JsonCallbackHandler
import pl.szczodrzynski.edziennik.api.AppError
import pl.szczodrzynski.edziennik.api.AppError.*
import pl.szczodrzynski.edziennik.api.v2.*
import pl.szczodrzynski.edziennik.api.v2.librus.data.DataLibrus
import pl.szczodrzynski.edziennik.currentTimeUnix
import pl.szczodrzynski.edziennik.getInt
import pl.szczodrzynski.edziennik.getString
import pl.szczodrzynski.edziennik.isNotNullNorEmpty
import java.net.HttpURLConnection
import java.net.HttpURLConnection.*

class LoginLibrusApi {
    companion object {
        private const val TAG = "LoginLibrusApi"
    }

    private lateinit var data: DataLibrus
    private lateinit var onSuccess: () -> Unit

    constructor(data: DataLibrus, onSuccess: () -> Unit) {
        this.data = data
        this.onSuccess = onSuccess

        if (data.profile == null) {
            data.callback.onError(null, AppError(TAG, 19, CODE_LIBRUS_PROFILE_NULL))
            return
        }

        if (data.apiTokenExpiryTime-30 > currentTimeUnix() && data.apiAccessToken.isNotNullNorEmpty()) {
            onSuccess()
        }
        else {
            when (data.loginStore.mode) {
                LOGIN_MODE_LIBRUS_EMAIL -> loginWithPortal()
                LOGIN_MODE_LIBRUS_SYNERGIA -> loginWithSynergia()
                LOGIN_MODE_LIBRUS_JST -> loginWithJst()
                else -> {
                    data.callback.onError(null, AppError(TAG, 25, CODE_INVALID_LOGIN_MODE))
                }
            }
        }
    }

    private fun loginWithPortal() {
        if (!data.loginMethods.contains(LOGIN_METHOD_LIBRUS_PORTAL)) {
            data.callback.onError(null, AppError(TAG, 26, CODE_LOGIN_METHOD_NOT_SATISFIED))
            return
        }
        SynergiaTokenExtractor(data) {
            onSuccess()
        }
    }

    private fun copyFromLoginStore() {
        data.loginStore.data?.apply {
            if (has("accountLogin")) {
                data.apiLogin = getString("accountLogin")
                remove("accountLogin")
            }
            if (has("accountPassword")) {
                data.apiPassword = getString("accountPassword")
                remove("accountPassword")
            }
            if (has("accountCode")) {
                data.apiCode = getString("accountCode")
                remove("accountCode")
            }
            if (has("accountPin")) {
                data.apiPin = getString("accountPin")
                remove("accountPin")
            }
        }
    }

    private fun loginWithSynergia() {
        copyFromLoginStore()
        if (data.apiRefreshToken != null) {
            // refresh a Synergia token
            synergiaRefreshToken()
        }
        else if (data.apiLogin != null && data.apiPassword != null) {
            synergiaGetToken()
        }
        else {
            // cannot log in: token expired, no login data present
            data.callback.onError(null, AppError(TAG, 91, CODE_INVALID_LOGIN))
        }
    }

    private fun loginWithJst() {
        copyFromLoginStore()

        if (data.apiRefreshToken != null) {
            // refresh a JST token
            jstRefreshToken()
        }
        else if (data.apiCode != null && data.apiPin != null) {
            // get a JST token from Code and PIN
            jstGetToken()
        }
        else {
            // cannot log in: token expired, no login data present
            data.callback.onError(null, AppError(TAG, 110, CODE_INVALID_LOGIN))
        }
    }

    private val tokenCallback = object : JsonCallbackHandler() {
        override fun onSuccess(json: JsonObject?, response: Response?) {
            if (json == null) {
                data.callback.onError(null, AppError(TAG, 117, CODE_MAINTENANCE, response))
                return
            }
            json.getString("error")?.let { error ->
                when (error) {
                    "librus_captcha_needed" -> {

                    }
                    "connection_problems" -> {

                    }
                    "invalid_client" -> {

                    }
                    "librus_reg_accept_needed" -> {

                    }
                    "librus_change_password_error" -> {

                    }
                    "librus_password_change_required" -> {

                    }
                    "invalid_grant" -> {

                    }
                    else -> {

                    }
                }
                return
            }

            try {
                data.apiAccessToken = json.getString("access_token")
                data.apiRefreshToken = json.getString("refresh_token")
                data.apiTokenExpiryTime = currentTimeUnix() + json.getInt("expires_in", 86400)
                onSuccess()
            } catch (e: NullPointerException) {
                data.callback.onError(null, AppError(TAG, 154, EXCEPTION_LOGIN_LIBRUS_API_TOKEN, response, e, json))
            }
        }

        override fun onFailure(response: Response?, throwable: Throwable?) {
            data.callback.onError(null, AppError(TAG, 159, CODE_OTHER, response, throwable))
        }
    }

    private fun synergiaGetToken() {
        Request.builder()
                .url(LIBRUS_API_TOKEN_URL)
                .userAgent(LIBRUS_USER_AGENT)
                .addParameter("grant_type", "password")
                .addParameter("username", data.apiLogin)
                .addParameter("password", data.apiPassword)
                .addParameter("librus_long_term_token", "1")
                .addParameter("librus_rules_accepted", "1")
                .addHeader("Authorization", "Basic $LIBRUS_API_AUTHORIZATION")
                .contentType(MediaTypeUtils.APPLICATION_FORM)
                .post()
                .allowErrorCode(HTTP_BAD_REQUEST)
                .allowErrorCode(HTTP_FORBIDDEN)
                .allowErrorCode(HTTP_UNAUTHORIZED)
                .callback(tokenCallback)
                .build()
                .enqueue()
    }
    private fun synergiaRefreshToken() {
        Request.builder()
                .url(LIBRUS_API_TOKEN_URL)
                .userAgent(LIBRUS_USER_AGENT)
                .addParameter("grant_type", "refresh_token")
                .addParameter("refresh_token", data.apiRefreshToken)
                .addParameter("librus_long_term_token", "1")
                .addParameter("librus_rules_accepted", "1")
                .addHeader("Authorization", "Basic $LIBRUS_API_AUTHORIZATION")
                .contentType(MediaTypeUtils.APPLICATION_FORM)
                .post()
                .allowErrorCode(HTTP_BAD_REQUEST)
                .allowErrorCode(HTTP_FORBIDDEN)
                .allowErrorCode(HTTP_UNAUTHORIZED)
                .callback(tokenCallback)
                .build()
                .enqueue()
    }
    private fun jstGetToken() {
        Request.builder()
                .url(LIBRUS_API_TOKEN_JST_URL)
                .userAgent(LIBRUS_USER_AGENT)
                .addParameter("grant_type", "implicit_grant")
                .addParameter("client_id", LIBRUS_API_CLIENT_ID_JST)
                .addParameter("secret", LIBRUS_API_SECRET_JST)
                .addParameter("code", data.apiCode)
                .addParameter("pin", data.apiPin)
                .addParameter("librus_rules_accepted", "1")
                .addParameter("librus_mobile_rules_accepted", "1")
                .addParameter("librus_long_term_token", "1")
                .contentType(MediaTypeUtils.APPLICATION_FORM)
                .post()
                .allowErrorCode(HTTP_BAD_REQUEST)
                .allowErrorCode(HTTP_FORBIDDEN)
                .allowErrorCode(HTTP_UNAUTHORIZED)
                .callback(tokenCallback)
                .build()
                .enqueue()
    }
    private fun jstRefreshToken() {
        Request.builder()
                .url(LIBRUS_API_TOKEN_JST_URL)
                .userAgent(LIBRUS_USER_AGENT)
                .addParameter("grant_type", "refresh_token")
                .addParameter("client_id", LIBRUS_API_CLIENT_ID_JST)
                .addParameter("refresh_token", data.apiRefreshToken)
                .addParameter("librus_long_term_token", "1")
                .addParameter("mobile_app_accept_rules", "1")
                .addParameter("synergy_accept_rules", "1")
                .contentType(MediaTypeUtils.APPLICATION_FORM)
                .post()
                .allowErrorCode(HTTP_BAD_REQUEST)
                .allowErrorCode(HTTP_FORBIDDEN)
                .allowErrorCode(HTTP_UNAUTHORIZED)
                .callback(tokenCallback)
                .build()
                .enqueue()
    }
}