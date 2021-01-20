package net.igorilic.didyoubuyit.profile

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.databinding.ActivityProfileBinding
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper


class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityProfileBinding
    private lateinit var globalHelper: GlobalHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        globalHelper = GlobalHelper(this@ProfileActivity)
        globalHelper.setupDrawerLayout(toolbar)

        val profileImage = globalHelper.getStringPref("user_image")

        if (profileImage.isNotBlank()) {
            val imgProfileImage = findViewById<ImageView>(R.id.imgProfileImage)
            val imageURL =
                "${AppInstance.globalHelper.getStringPref("API_URL")}/${GlobalHelper.PROFILE_IMAGE_PATH}/${profileImage}"
            Glide.with(this@ProfileActivity).asBitmap().load(imageURL).into(imgProfileImage)
            imgProfileImage.clipToOutline = true
        }

        findViewById<TextView>(R.id.lblUserName).text = globalHelper.getStringPref("user_name")
    }
}