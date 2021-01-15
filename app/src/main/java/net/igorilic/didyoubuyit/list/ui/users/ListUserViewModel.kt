package net.igorilic.didyoubuyit.list.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.SingleLiveEvent
import net.igorilic.didyoubuyit.model.UserModel
import org.json.JSONObject

class ListUserViewModel(private val listID: Int) : ViewModel() {
    private val listUsers = MutableLiveData<ArrayList<UserModel>>()
    private var errorMessage = SingleLiveEvent<String>()
    private val notifyMessage = SingleLiveEvent<String>()
    private val showProgressDialog = MutableLiveData<Boolean>()

    init {
        loadListUsers()
        setNotifyMessage("")
        setErrorMessage("")
    }

    private fun loadListUsers() {
        showProgressDialog.postValue(true)
        AppInstance.app.callAPI("/list/${listID}/users", null, {
            try {
                val res = JSONObject(it)
                val data = res.getJSONArray("data")
                val listItemsType = object : TypeToken<ArrayList<UserModel?>?>() {}.type
                val items: ArrayList<UserModel> = AppInstance.gson.fromJson(
                    data.toString(),
                    listItemsType
                )
                listUsers.postValue(items)
            } catch (e: Exception) {
                AppInstance.globalHelper.logMsg(
                    e.message ?: "Error parsing users info",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListUserViewModel@loadListUsers"
                )
            } finally {
                showProgressDialog.postValue(false)
            }
        }, {
            showProgressDialog.postValue(false)
            setErrorMessage(
                AppInstance.globalHelper.parseErrorNetworkResponse(
                    it,
                    AppInstance.appContext?.resources?.getString(R.string.error_list_users_failed_to_load)
                        ?: "",
                    "ListUserViewModel@loadListUsers"
                )
            )
        }, Request.Method.GET, true)
    }

    fun getListUsers(): LiveData<ArrayList<UserModel>> {
        return listUsers
    }

    private fun setNotifyMessage(msg: String) {
        notifyMessage.postValue(msg)
    }

    fun getNotifyMessage(): SingleLiveEvent<String> {
        return notifyMessage
    }

    private fun setErrorMessage(msg: String) {
        errorMessage.postValue(msg)
    }

    fun getErrorMessage(): SingleLiveEvent<String> {
        return errorMessage
    }

    fun getShowProgressDialog(): LiveData<Boolean> {
        return showProgressDialog
    }
}