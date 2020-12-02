package net.igorilic.didyoubuyit.helpers

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

@Suppress("PrivatePropertyName")
class AppInstance : Application() {

    private var volleyRequestQueue: RequestQueue? = null
    private val VOLLEY_DEFAULT_TAG = "dybiVolleyDefaultTag"

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        app = this
        globalHelper = GlobalHelper(applicationContext)
    }

    fun callAPI(
        operationPath: String,
        params: JSONObject?,
        successListener: Response.Listener<String>,
        errorListener: Response.ErrorListener,
        method: Int,
    ) {
        val url = "${GlobalHelper.API_URL}$operationPath"

        val req: StringRequest

        req = object : StringRequest(method, url, successListener, errorListener) {

            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["Content-Type"] = "application/json"
                return header
            }
            override fun getBody(): ByteArray {
                return params?.toString()?.toByteArray() ?: ByteArray(0)
            }
        }

        req.retryPolicy = DefaultRetryPolicy(
            15000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        cancelPendingRequests(url)
        addToRequestQueue(req, url)
    }

    private fun getRequestQueue(): RequestQueue? {
        if (volleyRequestQueue == null) {
            volleyRequestQueue = Volley.newRequestQueue(applicationContext)
        }
        return volleyRequestQueue
    }


    private fun <T> addToRequestQueue(req: Request<T>, tag: String) {
        req.tag = if (TextUtils.isEmpty(tag)) VOLLEY_DEFAULT_TAG else tag
        getRequestQueue()?.add(req)
    }

    private fun cancelPendingRequests(tag: String) {
        getRequestQueue()?.cancelAll(tag)
    }

    companion object {
        private var mAppContext: Context? = null
        lateinit var globalHelper: GlobalHelper
        lateinit var app: AppInstance

        var appContext: Context?
            get() = mAppContext
            set(mAppContext) {
                AppInstance.mAppContext = mAppContext
            }
    }

}

