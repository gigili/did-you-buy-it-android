package net.igorilic.didyoubuyit.model

import com.google.gson.annotations.SerializedName
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import org.json.JSONObject

data class ListItemModel(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("name")
    var name: String,

    @field:SerializedName("image")
    var image: String? = null,

    @field:SerializedName("is_repeating")
    var isRepeating: String = "0",

    @field:SerializedName("purchase_date")
    val purchaseDate: String? = null,

    @field:SerializedName("purchasedUserID")
    var purchasedUserID: UserModel? = null
) {
    override fun toString(): String {
        //return "$id | $name | $isRepeating | $purchasedUserID"
        return toJSON().toString()
    }

    fun toJSON(): JSONObject {
        val obj = JSONObject()

        if (id == null) return obj
        obj.put("id", id)
        obj.put("name", name)
        obj.put("image", image)
        obj.put("isRepeating", isRepeating)
        obj.put("purchaseDate", purchaseDate)
        //obj.put("isRepeating", isRepeating)

        return obj
    }

    fun getImageUrl(): String {
        if (image.isNullOrEmpty()) return ""

        return "${AppInstance.globalHelper.getStringPref("API_URL")}/${GlobalHelper.LIST_ITEM_IMAGE_PATH}/${image}"
    }
}
