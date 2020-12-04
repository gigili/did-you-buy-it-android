package net.igorilic.didyoubuyit.models

import com.google.gson.annotations.SerializedName
import net.igorilic.didyoubuyit.helpers.AppInstance

data class TokenModel(

    @field:SerializedName("access_token")
    val accessToken: String,

    @field:SerializedName("refresh_token")
    val refreshToken: String,

    @field:SerializedName("expires")
    val expires: Long
) {
    fun saveToSession() {
        AppInstance.globalHelper.setStringPref("access_token", accessToken)
        AppInstance.globalHelper.setStringPref("refresh_token", refreshToken)
        AppInstance.globalHelper.setLongPref("token_expires", expires)
    }
}
