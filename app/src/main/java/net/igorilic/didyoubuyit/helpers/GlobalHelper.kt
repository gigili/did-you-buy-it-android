package net.igorilic.didyoubuyit.helpers

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.navigation.NavigationView
import net.igorilic.didyoubuyit.BuildConfig
import net.igorilic.didyoubuyit.R
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.logging.Level
import java.util.logging.Logger


open class GlobalHelper constructor(private var context: Context) {
    private val preferences: SharedPreferences
    private val preferencesEditor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(PREFERENCE_TAG, 0)
        preferencesEditor = preferences.edit()

        if (getStringPref("API_URL").isEmpty())
            setStringPref("API_URL", API_URL)
    }

    fun getPreferences(): SharedPreferences {
        return preferences
    }

    fun getStringPref(name: String): String {
        val x = preferences.getString(name, "")
        return when (x == null) {
            true -> ""
            false -> x
        }
    }

    fun getBooleanPref(name: String): Boolean {
        return preferences.getBoolean(name, false)
    }

    fun getIntPref(name: String): Int {
        return preferences.getInt(name, -1)
    }

    fun getLongPref(name: String): Long {
        return preferences.getLong(name, -1L)
    }

    fun setLongPref(name: String, value: Long?) {
        preferencesEditor.putLong(name, value!!)
        preferencesEditor.commit()
    }

    fun setBooleanPref(name: String, value: Boolean?) {
        preferencesEditor.putBoolean(name, value!!)
        preferencesEditor.commit()
    }

    fun setStringPref(name: String, value: String) {
        preferencesEditor.putString(name, value)
        preferencesEditor.commit()
    }

    fun setIntPref(name: String, value: Int) {
        preferencesEditor.putInt(name, value)
        preferencesEditor.commit()
    }

    fun logMsg(str: String, tag: String? = null, logLevel: Level = Level.INFO) {
        val log = Logger.getAnonymousLogger()
        val logTag = tag ?: LOG_TAG
        val logString = "$logTag | $str"

        if (BuildConfig.DEBUG) {
            log.log(logLevel, logString)
        }
    }

    fun showMessageDialog(msg: String, title: String = "", callback: Callable<Void>? = null) {
        try {
            val mTitle = if (title.isNotEmpty()) title else context.getString(R.string.warning)
            val dl = AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setMessage(msg)
                .setPositiveButton(context.getString(R.string.ok)) { dl, _ ->
                    dl.dismiss()

                    if (callback !== null) {
                        callback.call()
                    }
                }
                .create()
            //.show()

            dl.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun notifyMSG(str: String) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show()
    }

    fun formatDate(
        lastUpdate: String,
        dateFormat: String,
        inputFormat: String = "yyyy-MM-dd HH:mm:ss",
        timeZone: TimeZone? = TimeZone.getTimeZone("Europe/Belgrade")
    ): String {
        try {
            var format = SimpleDateFormat(inputFormat, DEFAULT_APP_LOCALE)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val newDate = format.parse(lastUpdate)

            format = SimpleDateFormat(dateFormat, DEFAULT_APP_LOCALE)
            if (timeZone != null)
                format.timeZone = timeZone

            return format.format(newDate!!).toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun makeHash(text: String, algo: String = "SHA-256"): String {
        val md = MessageDigest.getInstance(algo)
        val textBytes = text.toByteArray(charset("UTF-8"))
        md.update(textBytes, 0, textBytes.size)
        val shaHash = md.digest()
        return convertToHex(shaHash).removePrefix("0");
    }

    private fun handleNavigationDrawerItemClick(id: Int) {
        when (id) {
            R.id.nav_exit -> {
                this.quitApp(false)
            }
        }
    }

    fun quitApp(clearSession: Boolean = false) {
        val dl = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.confirm_quit_app))
            .setMessage(context.getString(R.string.confirm_quit_app_message))
            .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                if (clearSession) {
                    unsetSessionData()
                }
                dialog.cancel()
                (context as Activity).finish()
            }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            .create()

        dl.show()
    }

    private fun unsetSessionData() {
        setIntPref("user_id", -1)
        setStringPref("user_name", "")
        setStringPref("user_username", "")
        setStringPref("user_email", "")
        setStringPref("user_image", "")

        setStringPref("access_token", "")
        setStringPref("refresh_token", "")
        setLongPref("token_expires", -1L)
    }

    fun handleOptionsMenuClick(id: Int, activity: AppCompatActivity) {
        when (id) {
            R.id.action_settings -> {
                /*if (!context.javaClass.toString().equals("SettingsActivity", ignoreCase = true)) {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                    (context as Activity).finish()
                }*/

                //TODO Create settings activity
            }

            R.id.action_logout -> {
                quitApp(true)
            }
        }
    }

    fun setupDrawerLayout(toolbar: Toolbar) {
        val drawerLayout: DrawerLayout = (context as Activity).findViewById(R.id.drawer_layout)
        val navView: NavigationView = (context as Activity).findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener {
            handleNavigationDrawerItemClick(it.itemId)
            true
        }

        val imgProfileImage =
            navView.getHeaderView(0).findViewById<ImageView>(R.id.imgSidebarProfileImage)
        val lblUserFullName =
            navView.getHeaderView(0).findViewById<TextView>(R.id.lblSidebarUserFullName)
        val lblUserEmail = navView.getHeaderView(0).findViewById<TextView>(R.id.lblSidebarUserEmail)

        lblUserFullName.text = getStringPref("user_name")
        lblUserEmail.text = getStringPref("user_email")
        val userImage = getStringPref("user_image")

        if (userImage.isNotEmpty()) {
            val imageUrl = "${getStringPref("API_URL")}/$PROFILE_IMAGE_PATH/$userImage"
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imgProfileImage)
            imgProfileImage.clipToOutline = true
        }

        val toggle = ActionBarDrawerToggle(
            (context as Activity),
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color =
            ContextCompat.getColor((context as Activity).baseContext, R.color.colorIconPrimary)
    }

    fun parseErrorNetworkResponse(
        error: VolleyError,
        defaultErrorMessage: String = "",
        activity: String = ""
    ): String {
        var errorMessage = ""
        if (error.networkResponse !== null && error.networkResponse.data !== null) {
            val data = JSONObject(String(error.networkResponse.data))
            val errorObject = data.getJSONObject("error")
            errorMessage = errorObject.getString("message")

            val activityTag = if (activity.isNotEmpty()) "[$activity]" else ""
            AppInstance.globalHelper.logMsg(
                "[ERROR]$activityTag Error: ${
                    data.getJSONObject("error").optString("message")
                }"
            )
        } else {
            errorMessage = defaultErrorMessage
        }
        error.printStackTrace()

        return errorMessage
    }

    companion object {
        var API_URL = "http://192.168.0.14:3030"
        var LOG_TAG = "dybi_tag"
        val DEFAULT_APP_LOCALE: Locale = Locale.UK
        var PROFILE_IMAGE_PATH = "images/user"
        val EMAIL_PATTERN =
            Regex("^(([a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)*)|(\\\".+\\\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))\$")
        private var PREFERENCE_TAG = "DidYouBuyItPreference"

        fun convertToHex(data: ByteArray): String {
            return data.joinToString("") { "%02x".format(it) }
        }
    }
}