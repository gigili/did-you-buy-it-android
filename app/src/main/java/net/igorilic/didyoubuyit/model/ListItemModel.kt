package net.igorilic.didyoubuyit.model

import com.google.gson.annotations.SerializedName

data class ListItemModel(
	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("is_repeating")
	val isRepeating: String = "0",

	@field:SerializedName("purchase_date")
	val purchaseDate: String? = null,

	@field:SerializedName("__purchasedUserID__")
	val __purchasedUserID__: UserModel? = null
)
