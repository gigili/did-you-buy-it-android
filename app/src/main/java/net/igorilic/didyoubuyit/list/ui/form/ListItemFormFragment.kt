package net.igorilic.didyoubuyit.list.ui.form

import android.Manifest
import android.app.Activity
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
import net.igorilic.didyoubuyit.list.ui.items.ListItemViewModel
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject

class ListItemFormFragment : Fragment(R.layout.fragment_list_item_form) {
    private lateinit var list: ListModel
    private var item: ListItemModel? = null
    private var newItemImage: Bitmap? = null
    private lateinit var btnAddNewImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var viewModel: ListItemViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddNewImage = view.findViewById(R.id.btnListItemAddImage)
        imgPreview = view.findViewById(R.id.imgListItemPreview)

        viewModel = ViewModelProvider(requireActivity()).get(ListItemViewModel::class.java)

        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            btnAddNewImage.visibility = View.GONE
            AppInstance.globalHelper.notifyMSG(requireContext().resources.getString(R.string.error_no_camera_found))
        }

        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))
        item = AppInstance.gson.fromJson(arguments?.getString("item")!!, ListItemModel::class.java)
            ?: null
        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"

        btnAddNewImage.setOnClickListener {
            openCamera()
        }

        val cbIsRepeating = view.findViewById<CheckBox>(R.id.cbListItemIsRepeating)

        item?.let {
            view.findViewById<EditText>(R.id.edtListItemName).setText(item?.name)
            cbIsRepeating.isChecked =
                (it.isRepeating == "1")

            if (!it.image.isNullOrEmpty()) {
                imgPreview.visibility = View.VISIBLE
                Glide.with(requireActivity()).asBitmap().load(it.getImageUrl()).into(imgPreview)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.btnListItemSave).setOnClickListener {
            Snackbar.make(view, "Trigger submit action", Snackbar.LENGTH_LONG).show()

            val edtItemName = view.findViewById<EditText>(R.id.edtListItemName)

            if (edtItemName.text.toString().trim().isEmpty()) {
                edtItemName.error = context?.resources?.getString(R.string.error_value_required)
                return@setOnClickListener
            }
            val isRepeating = if (cbIsRepeating.isChecked) "1" else "0"

            val params = JSONObject()
            params.put("name", edtItemName.text.toString())
            params.put("isRepeating", isRepeating)

            viewModel.addNewListItem(list.id!!, params, newItemImage)
            requireActivity().findNavController(R.id.nav_host_fragment).navigateUp()
        }
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
}