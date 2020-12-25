package net.igorilic.didyoubuyit.list.ui.users

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Request.Method.GET
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.model.UserModel
import net.igorilic.didyoubuyit.utility.ImageViewActivity
import org.json.JSONObject

class ListUserFragment : Fragment(R.layout.fragment_list_user) {
    private lateinit var list: ListModel
    private lateinit var users: ArrayList<UserModel>
    private lateinit var globalHelper: GlobalHelper
    private lateinit var adapter: ListUserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))
        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"

        if (arguments?.getString("list").isNullOrEmpty()) {
            return
        }

        globalHelper = GlobalHelper(requireContext())

        users = ArrayList()
        adapter = ListUserAdapter(
            requireContext(),
            users,
            list,
            object : ListUserAdapter.ListUserAdapterInterface {
                override fun onLongItemClick(view: View, item: UserModel, position: Int) {
                    if (list.userID == globalHelper.getIntPref("user_id")) {
                        showContextMenu(view, position, item)
                    }
                }

                override fun onListUserImageEnlarge(user: UserModel, imageUrl: String) {
                    val fullScreenImage = Intent(requireContext(), ImageViewActivity::class.java)
                    fullScreenImage.putExtra("imageUrl", imageUrl)
                    startActivity(fullScreenImage)
                }
            })

        val lst = view.findViewById<RecyclerView>(R.id.lstListUsers)
        lst.layoutManager = LinearLayoutManager(requireContext())
        lst.adapter = adapter

        loadListUsers()
    }

    private fun loadListUsers() {
        ProgressDialogHelper.showProgressDialog(requireActivity())
        AppInstance.app.callAPI("/list/${list.id}/users", null, {
            try {
                val res = JSONObject(it)
                val data = res.getJSONArray("data")
                val listItemsType = object : TypeToken<ArrayList<UserModel?>?>() {}.type
                val items: ArrayList<UserModel> = AppInstance.gson.fromJson(
                    data.toString(),
                    listItemsType
                )

                adapter.addItems(items)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                globalHelper.logMsg(
                    e.message ?: "Error parsing users info",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListUserFragment@loadListUser"
                )
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    requireContext().resources.getString(R.string.error_list_users_failed_to_load),
                    "ListUserFragment@loadListusers"
                )
            )
        }, GET, true)
    }

    private fun showContextMenu(view: View, position: Int, item: UserModel) {
        val pop = PopupMenu(requireContext(), view)
        pop.inflate(R.menu.list_user_menu)

        pop.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acListUserEdit -> AppInstance.globalHelper.notifyMSG("Edit item: ${item.name}")
                R.id.acListUserDelete -> {
                    globalHelper.showMessageDialog(
                        String.format(
                            requireContext().getString(R.string.action_confirm_user_delete),
                            item.name
                        ), requireContext().getString(R.string.lbl_confirm_action),
                        true
                    ) {
                        deleteListUser(position, item)
                    }
                }
            }
            true
        }

        pop.show()
    }

    private fun deleteListUser(position: Int, item: UserModel) {
        ProgressDialogHelper.showProgressDialog(requireActivity())
        AppInstance.app.callAPI("/list/${list.id}/users/${item.id}", null, {
            try {
                val res = JSONObject(it)
                if (res.optBoolean("success", false)) {
                    adapter.removeItem(position)
                    adapter.notifyDataSetChanged()
                } else {
                    throw Exception(requireContext().getString(R.string.error_deleting_list_user))
                }
            } catch (e: Exception) {
                globalHelper.logMsg(
                    e.message ?: "Failed to parse response",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListUserFragment@deleteListUser"
                )
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    requireContext().getString(R.string.error_deleting_list_user),
                    "ListUserFragment@deleteListUser"
                )
            )
        }, Request.Method.DELETE, true)
    }
}
