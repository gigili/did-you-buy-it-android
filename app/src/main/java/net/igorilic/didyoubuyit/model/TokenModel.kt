package net.igorilic.didyoubuyit.model

import com.google.gson.annotations.SerializedName
import net.igorilic.didyoubuyit.helper.AppInstance

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
