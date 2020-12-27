package net.igorilic.didyoubuyit.list.ui.items

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.android.material.floatingactionbutton.FloatingActionButton
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


class ListItemFragment : Fragment(R.layout.fragment_list_item) {
    private lateinit var list: ListModel
    private lateinit var listItems: ArrayList<ListItemModel>
    private lateinit var adapter: ListItemAdapter
    private lateinit var globalHelper: GlobalHelper
    private lateinit var ctx: Context
    private var listItemDialog: AlertDialog? = null
    private lateinit var listItemDialogView: View

    private enum class ShowType {
        New,
        Edit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ctx = requireContext()
        if (arguments?.getString("list").isNullOrEmpty()) {
            return
        }

        globalHelper = GlobalHelper(requireContext())
        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))

        initializeList(view)

        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"

        val fabNewListItem = view.findViewById<FloatingActionButton>(R.id.btnAddNewListItem)
        fabNewListItem.setOnClickListener {
            showListItemDialog(ShowType.New)
        }

        listItemDialogView = layoutInflater.inflate(R.layout.dialog_list_item, null, false)

        super.onViewCreated(view, savedInstanceState)
    }

    private fun showListItemDialog(showType: ShowType) {
        val dialogTitle: String
        val positiveButtonText: String

        when (showType) {
            ShowType.New -> {
                dialogTitle = ctx.resources.getString(R.string.lbl_add_new_item)
                positiveButtonText = ctx.resources.getString(R.string.lbl_add_new_item)
            }
            ShowType.Edit -> {
                dialogTitle = ctx.resources.getString(R.string.lbl_edit_list_item)
                positiveButtonText = ctx.resources.getString(R.string.lbl_update_item)
            }
        }

        if (listItemDialog == null) {
            listItemDialog = AlertDialog
                .Builder(ctx)
                .setTitle(dialogTitle)
                .setView(listItemDialogView)
                .setNegativeButton(ctx.resources.getString(R.string.no)) { it, _ ->
                    it.dismiss()
                }
                .setPositiveButton(positiveButtonText) { it, _ ->
                    //TODO: Call add/edit API endpoints
                    it.dismiss()
                }
                .setCancelable(true)
                .create()
        }

        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            listItemDialogView.findViewById<ImageView>(R.id.btnListItemDialogAddImage).visibility =
                View.GONE
        }

        listItemDialogView.findViewById<Button>(R.id.btnListItemDialogAddImage).setOnClickListener {
            openCamera()
        }

        listItemDialog!!.show()
    }

    private fun openCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                GlobalHelper.REQUEST_CAMERA_PERMISSION_CODE
            )
        } else {
            dispatchTakePictureIntent()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GlobalHelper.REQUEST_CAMERA_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            listItemDialogView.findViewById<Button>(R.id.btnListItemDialogAddImage).visibility =
                View.GONE
            globalHelper.notifyMSG(resources.getString(R.string.app_camera_permission_nedeed))
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(
            takePictureIntent,
            GlobalHelper.REQUEST_CAMERA_PERMISSION_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GlobalHelper.REQUEST_CAMERA_PERMISSION_CODE && resultCode == Activity.RESULT_OK) {
            try {
                val bmp = data?.extras?.get("data") as Bitmap
                val nh = (bmp.height * (512.0 / bmp.width))
                val scaled = Bitmap.createScaledBitmap(bmp, 512, nh.toInt(), true)

                val imagePreview =
                    listItemDialogView.findViewById<ImageView>(R.id.imgListItemDialogPreview)
                imagePreview.visibility = View.VISIBLE
                imagePreview.setImageBitmap(scaled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initializeList(view: View) {
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
                    val fullScreenView = Intent(ctx, ImageViewActivity::class.java)
                    fullScreenView.putExtra("imageUrl", imageUrl)
                    fullScreenView.putExtra("imageDescription", item.name)
                    startActivity(fullScreenView)
                }
            })
        val lst = view.findViewById<RecyclerView>(R.id.lstListItems)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = adapter

        loadListItems()
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
        val pop = PopupMenu(ctx, view)
        pop.inflate(R.menu.list_item_menu)

        pop.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acListItemEdit -> AppInstance.globalHelper.notifyMSG("Edit item: ${item.name}")
                R.id.acListItemDelete -> {
                    globalHelper.showMessageDialog(
                        String.format(
                            ctx.resources.getString(R.string.action_confirm_list_item_delete),
                            item.name
                        ),
                        ctx.resources.getString(R.string.lbl_confirm_action),
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
                    AppInstance.globalHelper.notifyMSG(ctx.resources.getString(R.string.list_item_removed_success))
                    adapter.removeItem(position)
                    adapter.notifyItemRemoved(position)
                } else {
                    AppInstance.globalHelper.notifyMSG(ctx.resources.getString(R.string.error_deleting_list_item))
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
                    ctx.resources.getString(R.string.error_deleting_list_item),
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
                    ctx.resources.getString(R.string.error_unable_to_update_list_item),
                    "ListItemFragment@changeItemBoughtState"
                )
            )
            ProgressDialogHelper.hideProgressDialog()
        }, Request.Method.PATCH, true)
    }

    override fun onDestroy() {
        if (listItemDialog != null && listItemDialog!!.isShowing) {
            listItemDialog!!.dismiss()
            listItemDialog = null
        }

        super.onDestroy()
    }
}