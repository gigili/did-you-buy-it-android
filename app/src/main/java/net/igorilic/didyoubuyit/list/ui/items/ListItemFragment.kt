package net.igorilic.didyoubuyit.list.ui.items

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject

class ListItemFragment() : Fragment(R.layout.fragment_list_item) {
    private lateinit var list: ListModel
    private lateinit var listItems: ArrayList<ListItemModel>
    private lateinit var adapter: ListItemAdapter

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
                AppInstance.globalHelper.logMsg("[ERROR][ListItemFragment]${e.message}")
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            AppInstance.globalHelper.parseErrorNetworkResponse(
                it,
                resources.getString(R.string.error_list_item_loading_failed),
                "ListItemFragment"
            )
        }, Request.Method.GET, true);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (arguments?.getString("list").isNullOrEmpty()) {
            return
        }

        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))

        listItems = ArrayList()

        adapter = ListItemAdapter(listItems, requireActivity())
        val lst = view.findViewById<RecyclerView>(R.id.lstListItems)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = adapter

        loadListItems()

        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"
        super.onViewCreated(view, savedInstanceState)
    }
}