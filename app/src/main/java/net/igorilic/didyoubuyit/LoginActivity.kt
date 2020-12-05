package net.igorilic.didyoubuyit

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import net.igorilic.didyoubuyit.databinding.ActivityLoginBinding
import net.igorilic.didyoubuyit.helpers.AppInstance
import net.igorilic.didyoubuyit.helpers.GlobalHelper
import net.igorilic.didyoubuyit.helpers.ProgressDialogHelper
import net.igorilic.didyoubuyit.models.TokenModel
import net.igorilic.didyoubuyit.models.UserModel
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        loginBinding.edtPassword.apply {
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        login()
                    }
                }
                false
            }
        }

        loginBinding.btnLogin.setOnClickListener {
            login()
        }

        loginBinding.btnRegister.setOnClickListener {
            val intentRegisterActivity = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intentRegisterActivity)
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
        val username = loginBinding.edtUsername.text.toString()
        val password = loginBinding.edtPassword.text.toString()

        if (username.isEmpty()) {
            loginBinding.edtUsername.error = getString(R.string.error_empty_username)
            return
        }

        if (password.isEmpty()) {
            loginBinding.edtPassword.error = getString(R.string.error_empty_password)
            return
        } else if (password.length < 6) {
            loginBinding.edtPassword.error = getString(R.string.error_min_password_length)
            return
        }

        val params = JSONObject()
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

                val data = res.getJSONObject("data")
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
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    error,
                    getString(R.string.error_login_failed),
                    "LoginActivity"
                )
            )
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