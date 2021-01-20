package net.igorilic.didyoubuyit.helper

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import net.igorilic.didyoubuyit.R
import org.json.JSONObject


@Suppress("PrivatePropertyName")
class AppInstance : Application() {

    private var volleyRequestQueue: RequestQueue? = null

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        app = this
        globalHelper = GlobalHelper(this@AppInstance)
    }

    fun callAPI(
        operationPath: String,
        params: JSONObject?,
        successListener: Response.Listener<String>,
        errorListener: Response.ErrorListener,
        method: Int,
        protectedRoute: Boolean = false,
    ) {
        val url = "${globalHelper.getStringPref("API_URL")}$operationPath"

        val req: StringRequest

        req = object : StringRequest(method, url, successListener, errorListener) {

            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["Content-Type"] = "application/json"
                if (protectedRoute) {
                    val token = globalHelper.getStringPref("access_token")
                    header["Authorization"] = "Bearer: $token"
                }
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

        if (protectedRoute) {
            globalHelper.logMsg("VALIDATING TOKEN")
            validateToken(req, errorListener)
        } else {
            cancelPendingRequests()
            addToRequestQueue(req)
        }
    }

    private fun validateToken(req: StringRequest, errorListener: Response.ErrorListener) {
        val now = System.currentTimeMillis() / 1000
        val tokenTime = globalHelper.getLongPref("token_expires")

        var isTokenValid = true
        if (tokenTime == -1L) isTokenValid = false //Token expiry time not set
        if (tokenTime < now) isTokenValid = false //Token has expired

        if (isTokenValid) {
            cancelPendingRequests()
            addToRequestQueue(req)
            return
        }

        val url = "${globalHelper.getStringPref("API_URL")}/refresh"
        val reqRefreshToken =
            object : StringRequest(Method.POST, url, {
                try {
                    val res = JSONObject(it)
                    if (res.optBoolean("success", false)) {
                        val data = res.optJSONObject("data")
                        data?.let { dt ->
                            val token = dt.optString("access_token", "")

                            if (token.isNotEmpty()) {
                                globalHelper.setStringPref("access_token", token)
                                globalHelper.setLongPref(
                                    "token_expires",
                                    data.optLong("expires", -1L)
                                )

                                cancelPendingRequests()
                                addToRequestQueue(req)
                            } else {
                                globalHelper.showMessageDialog(
                                    appContext?.getString(R.string.error_token_expired) ?: ""
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, errorListener) {
                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header["Content-Type"] = "application/json"
                    val token = globalHelper.getStringPref("refresh_token")
                    header["Authorization"] = "Bearer: $token"
                    return header
                }
            }

        reqRefreshToken.retryPolicy = DefaultRetryPolicy(
            15000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        cancelPendingRequests()
        addToRequestQueue(reqRefreshToken)
    }

    fun callApiUpload(
        method: Int,
        operationPath: String,
        data: HashMap<String, String>?,
        image: Bitmap? = null,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) {

        if (image == null) {
            val jsParams = JSONObject()
            jsParams.put("name", data?.get("name"))
            jsParams.put("is_repeating", data?.get("is_repeating"))
            callAPI(operationPath, jsParams, listener, errorListener, method, true)
            return
        }

        val url = "${globalHelper.getStringPref("API_URL")}$operationPath"
        val req: VolleyMultipartRequest = object : VolleyMultipartRequest(
            method, url,
            listener,
            errorListener
        ) {
            override fun getByteData(): Map<String, DataPart> {
                val params: MutableMap<String, DataPart> = HashMap()
                val imageName =
                    "${globalHelper.getIntPref("user_id")}-${System.currentTimeMillis()}-${
                        data?.get("listID")
                    }"
                params["image"] = DataPart(
                    "$imageName.png",
                    globalHelper.getFileDataFromDrawable(image),
                    "image/png"
                )

                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                val token = globalHelper.getStringPref("access_token")
                header["Authorization"] = "Bearer: $token"
                return header
            }

            override fun getParams(): MutableMap<String, String> {
                return data?.toMutableMap() ?: super.getParams()
            }
        }

        req.tag = this
        req.retryPolicy = DefaultRetryPolicy(
            15000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        addToRequestQueue(req)
    }

    private fun getRequestQueue(): RequestQueue? {
        if (volleyRequestQueue == null) {
            volleyRequestQueue = Volley.newRequestQueue(applicationContext)
        }
        return volleyRequestQueue
    }


    private fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = VOLLEY_DEFAULT_TAG
        getRequestQueue()?.add(req)
    }

    fun cancelPendingRequests() {
        getRequestQueue()?.cancelAll(VOLLEY_DEFAULT_TAG)
    }

    companion object {
        //private val applicationScope = CoroutineScope(SupervisorJob())
        const val VOLLEY_DEFAULT_TAG = "dybiVolleyDefaultTag"
        private var mAppContext: Context? = null
        lateinit var globalHelper: GlobalHelper
        lateinit var app: AppInstance
        val gson by lazy { Gson() }
        //lateinit var db: RoomDB

        var appContext: Context?
            get() = mAppContext
            set(mAppContext) {
                AppInstance.mAppContext = mAppContext
                //db = RoomDB.getDatabase(mAppContext!!, applicationScope)
            }
    }

}

