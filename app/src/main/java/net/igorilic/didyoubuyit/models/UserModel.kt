package net.igorilic.didyoubuyit.models

import com.google.gson.annotations.SerializedName
import net.igorilic.didyoubuyit.helpers.AppInstance

data class UserModel(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("username")
    val username: String

) {
    override fun toString(): String {
        return name
    }

    fun saveToSession() {
        AppInstance.globalHelper.setIntPref("user_id", id)
        AppInstance.globalHelper.setStringPref("user_name", name)
        AppInstance.globalHelper.setStringPref("user_username", username)
        AppInstance.globalHelper.setStringPref("user_email", email)
        AppInstance.globalHelper.setStringPref("user_image", image ?: "")
    }
}
