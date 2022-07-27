package com.bytedance.sdk.demo.auth.userinfo

import android.util.Log
import com.bytedance.sdk.demo.auth.BuildConfig
import com.bytedance.sdk.demo.auth.userinfo.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val CLIENT_SECRET = "28cd717c25d74a38c00dc87301523448"
object UserInfoQuery {

    fun getAccessToken(authCode: String, callback: ((response: AccessTokenInfo?, errorMessage: String?) -> Unit)) {
        val accessTokenApi = NetworkUtils.createApi(GetUserInfoService::class.java)
        accessTokenApi.getAccessToken(authCode, BuildConfig.CLIENT_KEY, CLIENT_SECRET, "authorization_code").enqueue(object: Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    Log.d("access token response", "message is ${response.message()}")
                    response.body()?.data?.let {
                        return callback(it, null)
                    }
                }
                callback(null, "Fetching access token failed. Please try again later.")
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    fun getUserInfo(accessToken: String, openId: String, callback: ((userInfo: UserInfo?, errorMessage: String?) -> Unit)) {
        val userInfoApi = NetworkUtils.createApi(GetUserInfoService::class.java)
        userInfoApi.getUserInfo(accessToken, openId).enqueue(object: Callback<UserInfoResponse> {
            override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        return callback(it, null)
                    }
                }
                callback(null, "Getting user basic info failed. Please try again later.")
            }

            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }
}

