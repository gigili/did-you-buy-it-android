package net.igorilic.didyoubuyit.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject


class ListViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var lists: MutableLiveData<ArrayList<ListModel>>

    fun getLists(): LiveData<ArrayList<ListModel>> {
        if (::lists.isInitialized) return lists

        getAllList()
        return lists
    }

    private fun getAllList() {
        lists = MutableLiveData()
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
                AppInstance.globalHelper.logMsg(
                    "Exception: ${e.message}",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "MainActivity@loadLists"
                )
            }
        }, {}, Request.Method.GET, true)
    }
}