package net.igorilic.didyoubuyit

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import kotlinx.android.synthetic.main.activity_login.*
import net.igorilic.didyoubuyit.helpers.AppInstance
import net.igorilic.didyoubuyit.helpers.GlobalHelper
import net.igorilic.didyoubuyit.helpers.ProgressDialogHelper
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

        checkLoginState()
    }

    private fun checkLoginState() {
        val now = System.currentTimeMillis() / 1000
        val tokenTime = AppInstance.globalHelper.getLongPref("token_expires")

        if (tokenTime == -1L) return //Token expiry time not set
        if (tokenTime < now) return //Token has expired

        openMainActivity()
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
            edtPassword.error = getString(R.string.error_min_password_length)
            return
        }

        val params = JSONObject();
        params.put("username", username)
        params.put("password", AppInstance.globalHelper.makeHash(password))

        ProgressDialogHelper.showProgressDialog(this@LoginActivity)

        AppInstance.app.callAPI("/login", params, { response ->
            try {
                val res = JSONObject(response)
                if (!res.getBoolean("success")) {
                    AppInstance.globalHelper.logMsg("[INFO][LoginActivity] $response")
                    globalHelper.showMessageDialog(getString(R.string.error_login_failed))
                    return@callAPI
                }

                val data = res.getJSONObject("data");
                val userModel = AppInstance.gson.fromJson(
                    data.getJSONObject("user").toString(),
                    UserModel::class.java
                )

                val tokenModel = AppInstance.gson.fromJson(
                    data.getJSONObject("token").toString(),
                    TokenModel::class.java
                )

                userModel.saveToSession()
                tokenModel.saveToSession()

                openMainActivity()
            } catch (e: Exception) {
                AppInstance.globalHelper.logMsg("[ERROR][LOGIN] Exception: ${e.message}")
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, { error ->
            ProgressDialogHelper.hideProgressDialog()
            var errorMessage = ""
            if (error.networkResponse !== null && error.networkResponse.data !== null) {
                val data = JSONObject(String(error.networkResponse.data))
                val errorObject = data.getJSONObject("error")
                errorMessage = errorObject.getString("message")

                AppInstance.globalHelper.logMsg(
                    "[ERROR][LOGIN] Error: ${
                        data.getJSONObject("error").optString("message")
                    }"
                )
            } else {
                errorMessage = getString(R.string.error_login_failed)
            }
            error.printStackTrace()

            globalHelper.showMessageDialog(errorMessage)
        },
            Request.Method.POST
        )
    }

    private fun openMainActivity() {
        val intentMainActivity = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intentMainActivity)
        finish()
    }
}