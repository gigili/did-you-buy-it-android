package net.igorilic.didyoubuyit.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject


class ListViewModel : ViewModel() {
    private val lists: MutableLiveData<ArrayList<ListModel>> = MutableLiveData()
    private val errorMessage: MutableLiveData<String> = MutableLiveData()

    init {
        getAllList()
    }

    fun getLists(): LiveData<ArrayList<ListModel>> {
        return lists
    }

    private fun getAllList() {
        AppInstance.app.callAPI("/list", null, {
            try {
                val res = JSONObject(it)
                val listsListType = object : TypeToken<ArrayList<ListModel?>?>() {}.type
                lists.postValue(
                    AppInstance.gson.fromJson(
                        res.getJSONArray("data").toString(),
                        listsListType
                    )
                )
            } catch (e: Exception) {
                addErrorMessage(e.message)
                AppInstance.globalHelper.logMsg(
                    "Exception: ${e.message}",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListViewModel@getLists"
                )
            }
        }, {
            addErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    AppInstance.appContext?.resources?.getString(
                        R.string.error_lists_loading_failed
                    ) ?: "",
                    "ListViewModel@getLists"
                )
            )
        }, Request.Method.GET, true)
    }

    private fun addErrorMessage(msg: String?) {
        if (msg != null)
            errorMessage.postValue(msg)
    }

    fun getErrorMessages(): LiveData<String> {
        return errorMessage
    }

    fun addNewList(listName: String) {
        val params = JSONObject()
        params.put("name", listName)
        AppInstance.app.callAPI("/list", params, {
            getAllList()
        }, {
            addErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    AppInstance.appContext?.resources?.getString(R.string.error_failed_to_create_list)
                        ?: "",
                    "ListViewModel@addNewList"
                )
            )
        }, Request.Method.POST, true)
    }
}