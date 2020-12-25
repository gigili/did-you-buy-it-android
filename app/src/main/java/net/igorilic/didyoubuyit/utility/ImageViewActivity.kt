package net.igorilic.didyoubuyit.utility

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aghajari.zoomhelper.ZoomHelper
import com.bumptech.glide.Glide
import net.igorilic.didyoubuyit.R

class ImageViewActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var imageDescription: String? = null
    private var shownDescription: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_view)
        supportActionBar?.hide()

        imageView = findViewById(R.id.imgFullScreen)

        if (intent.extras !== null && !intent.getStringExtra("imageUrl").isNullOrEmpty()) {
            val imageUrl = intent.getStringExtra("imageUrl")
            Glide.with(this).asBitmap().load(imageUrl).into(imageView)
            if (!intent.getStringExtra("imageDescription").isNullOrEmpty()) {
                imageDescription = intent.getStringExtra("imageDescription").toString()
                imageView.contentDescription = imageDescription
            }
        }

        ZoomHelper.addZoomableView(imageView)

        ZoomHelper.getInstance()
            .addOnZoomStateChangedListener(object : ZoomHelper.OnZoomStateChangedListener {
                override fun onZoomStateChanged(
                    zoomHelper: ZoomHelper,
                    zoomableView: View,
                    isZooming: Boolean
                ) {
                    if (isZooming && !imageDescription.isNullOrEmpty() && !shownDescription) {
                        Toast.makeText(zoomableView.context, imageDescription, Toast.LENGTH_SHORT)
                            .show()

                        shownDescription = true
                    }
                }
            })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ZoomHelper.getInstance().dispatchTouchEvent(ev!!, this) || super.dispatchTouchEvent(
            ev
        )
    }
}