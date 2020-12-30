package net.igorilic.didyoubuyit.list.ui.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.model.ListItemModel
import org.json.JSONObject

class ListItemViewModel(private val listID: Int) : ViewModel() {
    private val listItems: MutableLiveData<ArrayList<ListItemModel>> = MutableLiveData()
    private var errorMessage: MutableLiveData<String> = MutableLiveData()
    private val showProgressDialog: MutableLiveData<Boolean> = MutableLiveData()

    init {
        loadListItems()
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
        if (!msg.isNullOrEmpty())
            errorMessage.postValue(msg)
    }

    fun getErrorMessage(): LiveData<String> {
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
                    AppInstance.globalHelper.notifyMSG(
                        ctx?.resources?.getString(R.string.list_item_removed_success) ?: ""
                    )
                    val oldItems = listItems.value
                    oldItems?.removeAt(position)

                    listItems.value = oldItems
                } else {
                    AppInstance.globalHelper.notifyMSG(
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
            } catch (e: java.lang.Exception) {
                AppInstance.globalHelper.logMsg(
                    "${e.message}",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListItemViewModel@changeItemBoughtState"
                )
                e.printStackTrace()
            } finally {
                showProgressDialog.postValue(false)
                AppInstance.globalHelper.notifyMSG(
                    ctx?.resources?.getString(R.string.list_item_bought_state_update_success) ?: ""
                )
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

    fun addEditListItem(item: ListItemModel, position: Int) {
        val items = listItems.value
        items?.set(position, item)
        listItems.postValue(items)
    }
}