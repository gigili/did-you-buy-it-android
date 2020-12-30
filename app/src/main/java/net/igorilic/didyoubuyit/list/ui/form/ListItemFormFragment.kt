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
import com.bumptech.glide.Glide
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject

class ListItemFormFragment : Fragment(R.layout.fragment_list_item_form) {
    private lateinit var list: ListModel
    private var item: ListItemModel? = null
    private var newItemImage: Bitmap? = null
    private lateinit var btnAddNewImage: Button
    private lateinit var imgPreview: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddNewImage = view.findViewById(R.id.btnListItemAddImage)
        imgPreview = view.findViewById(R.id.imgListItemPreview)

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

        if (item !== null) {
            view.findViewById<EditText>(R.id.edtListItemName).setText(item?.name)
            view.findViewById<CheckBox>(R.id.cbListItemIsRepeating).isChecked =
                (item?.isRepeating == "1")

            if (!item?.image.isNullOrEmpty()) {
                imgPreview.visibility = View.VISIBLE
                Glide.with(requireActivity()).asBitmap().load(item?.getImageUrl()).into(imgPreview)
            }
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
        if (requestCode == GlobalHelper.REQUEST_CAMERA_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            btnAddNewImage.visibility = View.GONE
            AppInstance.globalHelper.notifyMSG(resources.getString(R.string.app_camera_permission_needed))
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