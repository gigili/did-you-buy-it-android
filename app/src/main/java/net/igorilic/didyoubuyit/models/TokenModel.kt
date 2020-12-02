package net.igorilic.didyoubuyit.models

import com.google.gson.annotations.SerializedName

data class TokenModel(

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("refresh_token")
	val refreshToken: String? = null,

	@field:SerializedName("expires")
	val expires: Int? = null
)
