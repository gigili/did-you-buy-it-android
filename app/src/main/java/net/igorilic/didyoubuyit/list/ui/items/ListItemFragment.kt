package net.igorilic.didyoubuyit.list.ui.items

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.GlobalHelper.Companion.LogLevelTypes
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.utility.ImageViewActivity
import org.json.JSONObject

class ListItemFragment() : Fragment(R.layout.fragment_list_item) {
    private lateinit var list: ListModel
    private lateinit var listItems: ArrayList<ListItemModel>
    private lateinit var adapter: ListItemAdapter
    private lateinit var globalHelper: GlobalHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (arguments?.getString("list").isNullOrEmpty()) {
            return
        }

        globalHelper = GlobalHelper(requireContext())
        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))

        listItems = ArrayList()
        adapter = ListItemAdapter(
            listItems,
            requireActivity(),
            object : ListItemAdapter.ListItemInterface {
                override fun onItemBoughtChangeState(
                    position: Int,
                    item: ListItemModel,
                    isChecked: Boolean
                ) {
                    changeItemBoughtState(position, item)
                }

                override fun onItemLongClick(view: View, position: Int, item: ListItemModel) {
                    showContextMenu(view, position, item)
                }

                override fun onListItemImageEnlarge(item: ListItemModel, imageUrl: String) {
                    val fullScreenView = Intent(requireContext(), ImageViewActivity::class.java)
                    fullScreenView.putExtra("imageUrl", imageUrl)
                    fullScreenView.putExtra("imageDescription", item.name)
                    startActivity(fullScreenView)
                }
            })
        val lst = view.findViewById<RecyclerView>(R.id.lstListItems)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = adapter

        loadListItems()

        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"

        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadListItems() {
        ProgressDialogHelper.showProgressDialog(requireActivity())
        AppInstance.app.callAPI("/list/${list.id}", null, {
            try {
                val res = JSONObject(it)
                val data = res.getJSONObject("data")
                val listItemsType = object : TypeToken<ArrayList<ListItemModel?>?>() {}.type
                val items: ArrayList<ListItemModel> = AppInstance.gson.fromJson(
                    data.getJSONArray("items").toString(),
                    listItemsType
                )

                adapter.addItems(items)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                AppInstance.globalHelper.logMsg(
                    e.message ?: "",
                    LogLevelTypes.Error,
                    "ListItemFragment@loadListItems"
                )
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    resources.getString(R.string.error_list_item_loading_failed),
                    "ListItemFragment"
                )
            )
        }, Request.Method.GET, true)
    }

    private fun showContextMenu(view: View, position: Int, item: ListItemModel) {
        val pop = PopupMenu(requireContext(), view)
        pop.inflate(R.menu.list_item_menu)

        pop.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acListItemEdit -> AppInstance.globalHelper.notifyMSG("Edit item: ${item.name}")
                R.id.acListItemDelete -> {
                    globalHelper.showMessageDialog(
                        String.format(
                            requireContext().resources.getString(R.string.action_confirm_list_item_delete),
                            item.name
                        ),
                        requireContext().resources.getString(R.string.lbl_confirm_action),
                        hasNegativeButton = true
                    ) {
                        deleteListItem(item, position)
                    }
                }
            }
            true
        }

        pop.show()
    }

    private fun deleteListItem(item: ListItemModel, position: Int) {
        ProgressDialogHelper.showProgressDialog(requireActivity())
        AppInstance.app.callAPI("/list/item/${list.id}/${item.id}", null, {
            try {
                val res = JSONObject(it)
                if (res.optBoolean("success", false)) {
                    AppInstance.globalHelper.notifyMSG(requireContext().resources.getString(R.string.list_item_removed_success))
                    adapter.removeItem(position)
                    adapter.notifyItemRemoved(position)
                } else {
                    AppInstance.globalHelper.notifyMSG(requireContext().resources.getString(R.string.error_deleting_list_item))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppInstance.globalHelper.logMsg(
                    "${e.message}",
                    LogLevelTypes.Error,
                    "ListItemFragment@deleteListItem"
                )
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    requireContext().resources.getString(R.string.error_deleting_list_item),
                    "ListItemFragment@deleteListItem"
                )
            )
        }, Request.Method.DELETE, true)
    }

    private fun changeItemBoughtState(position: Int, item: ListItemModel) {
        ProgressDialogHelper.showProgressDialog(requireActivity())
        AppInstance.app.callAPI("/list/item/${list.id}/${item.id}/bought", null, {
            try {
                val res = JSONObject(it)
                val mItem = AppInstance.gson.fromJson(
                    res.getJSONObject("data").toString(),
                    ListItemModel::class.java
                )

                adapter.updateItem(position, mItem)
                adapter.notifyDataSetChanged()
            } catch (e: java.lang.Exception) {
                AppInstance.globalHelper.logMsg(
                    "${e.message}",
                    LogLevelTypes.Error,
                    "ListItemFragment@changeItemBoughtState"
                )
                e.printStackTrace()
            } finally {
                ProgressDialogHelper.hideProgressDialog()
                AppInstance.globalHelper.notifyMSG(requireActivity().resources.getString(R.string.list_item_bought_state_update_success))
            }
        }, {
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    requireContext().resources.getString(R.string.error_unable_to_update_list_item),
                    "ListItemFragment@changeItemBoughtState"
                )
            )
            ProgressDialogHelper.hideProgressDialog()
        }, Request.Method.PATCH, true)
    }
}