package net.igorilic.didyoubuyit

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import kotlinx.android.synthetic.main.activity_login.*
import net.igorilic.didyoubuyit.helpers.AppInstance
import net.igorilic.didyoubuyit.helpers.GlobalHelper
import net.igorilic.didyoubuyit.models.TokenModel
import net.igorilic.didyoubuyit.models.UserModel
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        edtPassword.apply {
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        login()
                    }
                }
                false
            }
        }

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val globalHelper = GlobalHelper(this@LoginActivity)
        val username = edtUsername.text.toString()
        val password = edtPassword.text.toString()

        if (username.isEmpty()) {
            edtUsername.error = getString(R.string.error_empty_username)
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = getString(R.string.error_empty_password)
            return
        } else if (password.length < 6) {
            edtPassword.error = getString(R.string.error_password_length)
            return
        }

        val params = JSONObject();
        params.put("username", username)
        params.put("password", AppInstance.globalHelper.makeHash(password))

        AppInstance.app.callAPI("/login", params, { response ->
            //AppInstance.globalHelper.logMsg("[INFO][LOGIN] $response")
            try {
                val res = JSONObject(response)

                if (!res.getBoolean("success")) {
                    globalHelper.showMessageDialog(getString(R.string.error_login_failed))
                    return@callAPI
                }

                val data = res.getJSONObject("data");
                AppInstance.globalHelper.setSessionData(
                    AppInstance.gson.fromJson(
                        data.getJSONObject("user").toString(),
                        UserModel::class.java
                    ),
                    AppInstance.gson.fromJson(
                        data.getJSONObject("token").toString(),
                        TokenModel::class.java
                    )
                )

                //TODO: Open the main activity here...
            } catch (e: Exception) {
                AppInstance.globalHelper.logMsg("[ERROR][LOGIN] Exception: ${e.message}")
            }
        }, { error ->
            val data = JSONObject(String(error.networkResponse.data))
            val errorObject = data.optJSONObject("error")
            val errorMessage = if (errorObject != null) {
                errorObject.optString("message");
            } else {
                getString(R.string.error_login_failed)
            }

            AppInstance.globalHelper.logMsg(
                "[ERROR][LOGIN] Error: ${
                    data.getJSONObject("error").optString("message")
                }"
            )
            globalHelper.showMessageDialog(errorMessage)
        },
            Request.Method.POST
        )
    }
}