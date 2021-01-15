package net.igorilic.didyoubuyit.list.ui.items

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.SingleLiveEvent
import net.igorilic.didyoubuyit.model.ListItemModel
import org.json.JSONObject

class ListItemViewModel(private val listID: Int) : ViewModel() {
    private val listItems: MutableLiveData<ArrayList<ListItemModel>> = MutableLiveData()
    private var errorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    private val notifyMessage: SingleLiveEvent<String> = SingleLiveEvent()
    private val showProgressDialog: MutableLiveData<Boolean> = MutableLiveData()

    init {
        loadListItems()
        setNotifyMessage("")
        setErrorMessage("")
    }

    fun getLisItems(): LiveData<ArrayList<ListItemModel>> {
        return listItems
    }

    private fun loadListItems() {
        showProgressDialog.postValue(true)
        AppInstance.app.callAPI("/list/${listID}", null, {
            try {
                val res = JSONObject(it)
                val data = res.getJSONObject("data")
                val listItemsType = object : TypeToken<ArrayList<ListItemModel?>?>() {}.type
                listItems.postValue(
                    AppInstance.gson.fromJson(
                        data.getJSONArray("items").toString(),
                        listItemsType
                    )
                )
            } catch (e: Exception) {
                setErrorMessage(e.message)
                AppInstance.globalHelper.logMsg(
                    e.message ?: "",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListItemViewModel@loadListItems"
                )
            } finally {
                showProgressDialog.postValue(false)
            }
        }, {
            showProgressDialog.postValue(false)
            setErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    AppInstance.appContext?.resources?.getString(R.string.error_list_item_loading_failed)
                        ?: "",
                    "ListItemsViewModel@loadListitems"
                )
            )
        }, Request.Method.GET, true)
    }

    private fun setErrorMessage(msg: String?) {
        errorMessage.postValue(msg)
    }

    fun getErrorMessage(): SingleLiveEvent<String> {
        return errorMessage
    }

    fun getShowProgressDialog(): LiveData<Boolean> {
        return showProgressDialog
    }

    fun deleteListItem(item: ListItemModel, position: Int) {
        val ctx = AppInstance.appContext
        showProgressDialog.postValue(true)
        AppInstance.app.callAPI("/list/item/${listID}/${item.id}", null, {
            try {
                val res = JSONObject(it)
                if (res.optBoolean("success", false)) {
                    setNotifyMessage(
                        ctx?.resources?.getString(R.string.list_item_removed_success) ?: ""
                    )
                    val oldItems = listItems.value
                    oldItems?.removeAt(position)

                    listItems.value = oldItems
                } else {
                    setNotifyMessage(
                        ctx?.resources?.getString(R.string.error_deleting_list_item) ?: ""
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppInstance.globalHelper.logMsg(
                    "${e.message}",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListItemViewModel@deleteListItem"
                )
            } finally {
                showProgressDialog.postValue(false)
            }
        }, {
            showProgressDialog.postValue(false)
            setErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    ctx?.resources?.getString(R.string.error_deleting_list_item) ?: "",
                    "ListItemViewmodel@deleteListItem"
                )
            )
        }, Request.Method.DELETE, true)
    }

    fun changeItemBoughtState(item: ListItemModel, position: Int) {
        val ctx = AppInstance.appContext
        AppInstance.app.callAPI("/list/item/${listID}/${item.id}/bought", null, {
            try {
                val res = JSONObject(it)
                val mItem = AppInstance.gson.fromJson(
                    res.getJSONObject("data").toString(),
                    ListItemModel::class.java
                )

                val items = listItems.value
                items?.set(position, mItem)
                listItems.postValue(items)

                setNotifyMessage(
                    AppInstance.appContext?.resources?.getString(R.string.list_item_bought_state_update_success)
                        ?: ""
                )
            } catch (e: java.lang.Exception) {
                AppInstance.globalHelper.logMsg(
                    "${e.message}",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListItemViewModel@changeItemBoughtState"
                )
                e.printStackTrace()
            } finally {
                showProgressDialog.postValue(false)
            }
        }, {
            showProgressDialog.postValue(false)
            setErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    ctx?.resources?.getString(R.string.error_unable_to_update_list_item) ?: "",
                    "ListItemViewModel@changeItemBoughtState"
                )
            )
        }, Request.Method.PATCH, true)
    }

    fun addNewListItem(listID: Int, params: HashMap<String, String>, newItemImage: Bitmap?) {
        showProgressDialog.postValue(true)
        AppInstance.app.callApiUpload(Request.Method.POST, "/list/item/$listID",
            params, newItemImage, {
                showProgressDialog.postValue(false)
                loadListItems()
                setNotifyMessage(
                    AppInstance.appContext?.resources?.getString(R.string.list_item_add_success)
                        ?: ""
                )
            }, {
                showProgressDialog.postValue(false)
                setErrorMessage(
                    AppInstance.globalHelper.parseErrorNetworkResponse(
                        it,
                        AppInstance.appContext?.resources?.getString(R.string.error_failed_to_add_list_item)
                            ?: "",
                        "ListItemViewModel@addNewListItem"
                    )
                )
            }
        )
    }

    private fun setNotifyMessage(msg: String?) {
        notifyMessage.postValue(msg)
    }

    fun getNotifyMessage(): SingleLiveEvent<String> {
        return notifyMessage
    }

    fun removeItemImage(listID: Int?, itemID: Int?, position: Int?) {
        val ctx = AppInstance.appContext
        showProgressDialog.postValue(true)
        AppInstance.app.callAPI("/list/item/$listID/$itemID/image", null, {
            showProgressDialog.postValue(false)
            position?.let {
                val items = listItems.value
                items?.let { mItems ->
                    val mItem = mItems[it]
                    mItem.image = null
                    mItems[position] = mItem
                    listItems.postValue(mItems)
                    setNotifyMessage(ctx?.resources?.getString(R.string.list_item_image_removed_success))
                }
            }
        }, {
            showProgressDialog.postValue(false)
            setErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    ctx?.resources?.getString(R.string.error_unable_to_remove_list_item_image)
                        ?: "",
                    "ListItemViewModel@removeItemImage"
                )
            )
        }, Request.Method.DELETE, true)
    }

    fun updateListItem(
        listID: Int?,
        itemID: Int?,
        params: java.util.HashMap<String, String>,
        newItemImage: Bitmap?,
        position: Int?
    ) {
        val ctx = AppInstance.appContext
        showProgressDialog.postValue(true)
        AppInstance.app.callApiUpload(
            Request.Method.PATCH,
            "/list/item/$listID/$itemID",
            params,
            newItemImage, {
                showProgressDialog.postValue(false)
                position?.let {
                    val items = listItems.value
                    items?.let { mItems ->
                        val mItem = mItems[it]
                        mItem.name = params["name"].toString()
                        mItem.is_repeating = params["is_repeating"].toString()
                        mItems[position] = mItem
                        listItems.postValue(mItems)
                        setNotifyMessage(ctx?.resources?.getString(R.string.list_item_update_success))
                    }
                }
            }, {
                showProgressDialog.postValue(false)
                setErrorMessage(
                    AppInstance.globalHelper.parseErrorNetworkResponse(
                        it,
                        ctx?.resources?.getString(R.string.error_failed_to_update_list_item) ?: "",
                        "ListItemViewModel@updateListItem"
                    )
                )
            }
        )
    }
}