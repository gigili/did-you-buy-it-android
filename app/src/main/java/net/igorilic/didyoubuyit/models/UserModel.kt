package net.igorilic.didyoubuyit.models

import com.google.gson.annotations.SerializedName

data class UserModel(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null,

	@field:SerializedName("status")
	val status: String? = null


) {
	override fun toString(): String {
		return "$id | $name | $username"
	}
}
