package net.igorilic.didyoubuyit.list.ui.items

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.utility.ImageViewActivity
import org.json.JSONObject


class ListItemFragment : Fragment(R.layout.fragment_list_item) {
    private lateinit var list: ListModel
    private var listItems: ArrayList<ListItemModel> = ArrayList()
    private lateinit var adapter: ListItemAdapter
    private lateinit var globalHelper: GlobalHelper
    private lateinit var ctx: Context
    private var listItemDialog: AlertDialog? = null
    private lateinit var listItemDialogView: View
    private lateinit var newItemImage: Bitmap
    private lateinit var viewModel: ListItemViewModel

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

        val listItemViewModelFactory = ListItemViewModelFactory(list.id!!)
        viewModel = ViewModelProvider(
            requireActivity(),
            listItemViewModelFactory
        ).get(ListItemViewModel::class.java)

        viewModel.getLisItems().observe(requireActivity(), {
            AppInstance.globalHelper.logMsg(it.toString())
            listItems.clear()
            adapter.notifyDataSetChanged()
            listItems.addAll(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.getErrorMessage().observe(requireActivity(), {
            globalHelper.showMessageDialog(it)
        })

        viewModel.getShowProgressDialog().observe(requireActivity(), {
            if (it) {
                ProgressDialogHelper.showProgressDialog(requireActivity())
            } else {
                ProgressDialogHelper.hideProgressDialog()
            }
        })

        initializeList(view)

        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"

        val fabNewListItem = view.findViewById<FloatingActionButton>(R.id.btnAddNewListItem)
        fabNewListItem.setOnClickListener {
            showListItemDialog(ShowType.New)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("InflateParams")
    private fun showListItemDialog(showType: ShowType) {
        if (listItemDialog == null) {
            listItemDialogView = layoutInflater.inflate(R.layout.dialog_list_item, null, false)
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
                newItemImage = data?.extras?.get("data") as Bitmap
                val nh = (newItemImage.height * (512.0 / newItemImage.width))
                val scaled = Bitmap.createScaledBitmap(newItemImage, 512, nh.toInt(), true)

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
        adapter = ListItemAdapter(
            listItems,
            requireActivity(),
            object : ListItemAdapter.ListItemInterface {
                override fun onItemBoughtChangeState(
                    position: Int,
                    item: ListItemModel,
                    isChecked: Boolean
                ) {
                    viewModel.changeItemBoughtState(item, position)
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
    }

    private fun showContextMenu(view: View, position: Int, item: ListItemModel) {
        val pop = PopupMenu(ctx, view)
        pop.inflate(R.menu.list_item_menu)

        pop.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acListItemEdit -> showListItemDialog(ShowType.Edit)
                R.id.acListItemDelete -> {
                    globalHelper.showMessageDialog(
                        String.format(
                            ctx.resources.getString(R.string.action_confirm_list_item_delete),
                            item.name
                        ),
                        ctx.resources.getString(R.string.lbl_confirm_action),
                        hasNegativeButton = true
                    ) {
                        //deleteListItem(item, position)
                        viewModel.deleteListItem(item, position)
                    }
                }
            }
            true
        }

        pop.show()
    }

    override fun onDestroy() {
        if (listItemDialog != null && listItemDialog!!.isShowing) {
            listItemDialog!!.dismiss()
            listItemDialog = null
        }

        super.onDestroy()
    }
}