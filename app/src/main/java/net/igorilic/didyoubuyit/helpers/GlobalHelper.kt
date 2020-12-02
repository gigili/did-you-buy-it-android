package net.igorilic.didyoubuyit.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import net.igorilic.didyoubuyit.BuildConfig
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.models.TokenModel
import net.igorilic.didyoubuyit.models.UserModel
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

open class GlobalHelper constructor(private var context: Context) {
    private val preferences: SharedPreferences
    private val preferencesEditor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(PREFERENCE_TAG, 0)
        preferencesEditor = preferences.edit()
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

    fun showMessageDialog(msg: String, title: String = "") {
        try {
            val mTitle = if (title.isNotEmpty()) title else context.getString(R.string.warning)
            val dl = AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setMessage(msg)
                .setPositiveButton(context.getString(R.string.ok)) { dl, _ -> dl.dismiss() }
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

    fun setSessionData(user: UserModel, tokenData: TokenModel) {
        //TODO("Not yet implemented")
        logMsg("[INFO] $user")
    }

    companion object {
        var API_URL = "http://192.168.0.13:3030"
        var LOG_TAG = "dybi_tag"
        val DEFAULT_APP_LOCALE: Locale = Locale.UK
        private var PREFERENCE_TAG = "DidYouBuyItPreference"

        fun convertToHex(data: ByteArray): String {
            return data.joinToString("") { "%02x".format(it) }
        }
    }
}