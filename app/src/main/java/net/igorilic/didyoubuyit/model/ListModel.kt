package net.igorilic.didyoubuyit.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

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
) {
    override fun toString(): String {
        return name ?: ""
    }

    private fun toJSON(): JSONObject {
        val json = JSONObject()

        if (name.isNullOrEmpty()) return json

        json.put("id", id)
        json.put("name", name)
        json.put("userID", userID)
        json.put("cntUsers", cntUsers)
        json.put("cntItems", cntItems)
        json.put("cntBoughtItems", cntBoughtItems)
        json.put("createdAt", createdAt)

        return json
    }

    fun toJSONString(): String {
        return toJSON().toString()
    }

    companion object {
        fun fromJSON(obj: JSONObject): ListModel {
            return ListModel(
                obj.getInt("id"),
                obj.getString("name"),
                obj.getInt("userID"),
                obj.getInt("cntUsers"),
                obj.getInt("cntItems"),
                obj.getInt("cntBoughtItems"),
                obj.getString("createdAt")
            )
        }
    }
}
