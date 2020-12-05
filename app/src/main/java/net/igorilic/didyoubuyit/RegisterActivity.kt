package net.igorilic.didyoubuyit

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import kotlinx.android.synthetic.main.activity_register.*
import net.igorilic.didyoubuyit.helpers.AppInstance
import net.igorilic.didyoubuyit.helpers.GlobalHelper
import net.igorilic.didyoubuyit.helpers.ProgressDialogHelper
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var globalHelper: GlobalHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        globalHelper = GlobalHelper(this@RegisterActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val name = edtName.text.toString()
        val email = edtEmail.text.toString()
        val username = edtUsername.text.toString()
        val password = edtPassword.text.toString()

        //<editor-fold desc="Form validation logic">
        if (name.isEmpty()) {
            edtName.error = getString(R.string.error_empty_name)
            return
        }

        if (email.isEmpty()) {
            edtEmail.error = getString(R.string.error_empty_email)
            return
        }

        if (!email.trim().matches(GlobalHelper.EMAIL_PATTERN)) {
            edtEmail.error = getString(R.string.error_invalid_email)
            return
        }

        if (username.isEmpty()) {
            edtUsername.error = getString(R.string.error_empty_username)
            return
        }

        if (username.trim().length < 3) {
            edtUsername.error = getString(R.string.error_min_username_length)
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = getString(R.string.error_empty_password)
            return
        }

        if (password.trim().length < 6) {
            edtPassword.error = getString(R.string.error_min_password_length)
            return
        }
        //</editor-fold>

        val params = JSONObject()
        params.put("name", name)
        params.put("email", email)
        params.put("username", username)
        params.put("password", globalHelper.makeHash(password))

        ProgressDialogHelper.showProgressDialog(this@RegisterActivity)
        AppInstance.app.callAPI("/register", params, { response ->
            try {
                val res = JSONObject(response)
                if (res.getBoolean("success")) {

                    edtName.setText("")
                    edtEmail.setText("")
                    edtUsername.setText("")
                    edtPassword.setText("")

                    globalHelper.showMessageDialog(
                        getString(R.string.sign_up_success),
                        getString(R.string.sign_up_success_title)
                    ) {
                        finish()
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppInstance.globalHelper.logMsg("[ERROR][SIGN UP] Exception: ${e.message}")
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, { error ->
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    error,
                    getString(R.string.error_sign_up_failed),
                    "RegisterActivity"
                )
            )
        }, Request.Method.POST)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}