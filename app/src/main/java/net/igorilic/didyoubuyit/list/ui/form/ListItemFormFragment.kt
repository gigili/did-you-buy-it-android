package net.igorilic.didyoubuyit.list.ui.form

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.GlobalHelper.Companion.EditMode
import net.igorilic.didyoubuyit.list.ui.items.ListItemViewModel
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel

class ListItemFormFragment : Fragment(R.layout.fragment_list_item_form) {
    private lateinit var list: ListModel
    private var item: ListItemModel? = null
    private var position: Int? = null
    private var newItemImage: Bitmap? = null
    private lateinit var btnAddNewImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var btnRemoveImage: ImageView
    private lateinit var viewModel: ListItemViewModel
    private lateinit var editMode: EditMode

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddNewImage = view.findViewById(R.id.btnListItemAddImage)
        imgPreview = view.findViewById(R.id.imgListItemPreview)
        btnRemoveImage = view.findViewById(R.id.btnListItemRemoveImage)

        viewModel = ViewModelProvider(requireActivity()).get(ListItemViewModel::class.java)

        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            btnAddNewImage.visibility = View.GONE
            AppInstance.globalHelper.notifyMSG(requireContext().resources.getString(R.string.error_no_camera_found))
        }

        list = AppInstance.gson.fromJson(arguments?.getString("list")!!, ListModel::class.java)
        item = if (arguments?.getString("item") !== null && arguments?.getString("item")
                .equals("null")
        ) {
            AppInstance.gson.fromJson(arguments?.getString("item")!!, ListItemModel::class.java)
        } else {
            null
        }

        position = arguments?.getString("position")?.toInt()
        editMode = arguments?.get("editMode") as EditMode

        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"

        btnAddNewImage.setOnClickListener {
            openCamera()
        }

        val cbIsRepeating = view.findViewById<CheckBox>(R.id.cbListItemIsRepeating)

        item?.let {
            view.findViewById<EditText>(R.id.edtListItemName).setText(item?.name)

            AppInstance.globalHelper.logMsg("it: $it")
            cbIsRepeating.isChecked =
                (it.is_repeating == "1")

            if (!it.image.isNullOrEmpty()) {
                imgPreview.visibility = View.VISIBLE
                btnRemoveImage.visibility = View.VISIBLE

                Glide.with(requireActivity()).asBitmap().load(it.getImageUrl()).into(imgPreview)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.btnListItemSave).setOnClickListener {
            handleSaveButton()
        }

        btnRemoveImage.setOnClickListener {
            handleImageRemove()
        }

        viewModel.getNotifyMessage().observe(requireActivity(), {
            if (!it.isNullOrBlank() && view.parent != null) {
                Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
            }
        })

        viewModel.getLisItems().observe(requireActivity(), {
            item?.let { i ->
                position?.let { p ->
                    if (it[p].id == i.id && it[p].image == null) {
                        imgPreview.visibility = View.GONE
                        btnRemoveImage.visibility = View.GONE
                    }
                }
            }
        })
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
        if (requestCode == GlobalHelper.REQUEST_CAMERA_PERMISSION_CODE) {

            for (res in grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    btnAddNewImage.visibility = View.GONE
                    AppInstance.globalHelper.notifyMSG(resources.getString(R.string.app_camera_permission_needed))
                    return
                }
            }

            dispatchTakePictureIntent()
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
                if (newItemImage != null) {
                    val nh = (newItemImage!!.height * (512.0 / newItemImage!!.width))
                    val scaled = Bitmap.createScaledBitmap(newItemImage!!, 512, nh.toInt(), true)

                    imgPreview.visibility = View.VISIBLE
                    imgPreview.setImageBitmap(scaled)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleSaveButton() {
        val edtItemName = view?.findViewById<EditText>(R.id.edtListItemName)
        val cbIsRepeating = view?.findViewById<CheckBox>(R.id.cbListItemIsRepeating)

        if (edtItemName?.text.toString().trim().isEmpty()) {
            edtItemName?.error = context?.resources?.getString(R.string.error_value_required)
            return
        }
        val isRepeating = if (cbIsRepeating?.isChecked == true) "1" else "0"

        val params = HashMap<String, String>()
        params["name"] = edtItemName?.text.toString()
        params["is_repeating"] = isRepeating

        if (editMode == EditMode.New) {
            viewModel.addNewListItem(list.id!!, params, newItemImage)
        } else {
            viewModel.updateListItem(list.id, item?.id, params, newItemImage, position)
        }
        requireActivity().findNavController(R.id.nav_host_fragment).navigateUp()
    }

    private fun handleImageRemove() {
        AlertDialog
            .Builder(requireContext())
            .setTitle(getString(R.string.lbl_confirm_action))
            .setMessage(getString(R.string.lbl_confirm_image_deletion))
            .setNegativeButton(getString(R.string.no)) { dl, _ ->
                dl.dismiss()
            }
            .setPositiveButton(getString(R.string.lbl_delete)) { dl, _ ->
                viewModel.removeItemImage(list.id, item?.id, position)
                dl.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}