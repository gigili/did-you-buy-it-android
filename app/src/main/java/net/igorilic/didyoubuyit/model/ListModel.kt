package net.igorilic.didyoubuyit.model

import com.google.gson.annotations.SerializedName

data class ListModel(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("userID")
    val userID: Int? = null,

    @field:SerializedName("cntUsers")
    val cntUsers: Int? = null,

    @field:SerializedName("cntItems")
    val cntItems: Int? = null,

    @field:SerializedName("cntBoughtItems")
    val cntBoughtItems: Int? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null
)
