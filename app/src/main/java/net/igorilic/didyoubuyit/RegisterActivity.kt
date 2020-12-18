package net.igorilic.didyoubuyit

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import net.igorilic.didyoubuyit.databinding.ActivityRegisterBinding
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var globalHelper: GlobalHelper
    private lateinit var registerBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)

        globalHelper = GlobalHelper(this@RegisterActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this,
                R.color.action_bar_color
            )
        )

        registerBinding.btnRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val name = registerBinding.edtName.text.toString()
        val email = registerBinding.edtEmail.text.toString()
        val username = registerBinding.edtUsername.text.toString()
        val password = registerBinding.edtPassword.text.toString()

        //<editor-fold desc="Form validation logic">
        if (name.isEmpty()) {
            registerBinding.edtName.error = getString(R.string.error_empty_name)
            return
        }

        if (email.isEmpty()) {
            registerBinding.edtEmail.error = getString(R.string.error_empty_email)
            return
        }

        if (!email.trim().matches(GlobalHelper.EMAIL_PATTERN)) {
            registerBinding.edtEmail.error = getString(R.string.error_invalid_email)
            return
        }

        if (username.isEmpty()) {
            registerBinding.edtUsername.error = getString(R.string.error_empty_username)
            return
        }

        if (username.trim().length < 3) {
            registerBinding.edtUsername.error = getString(R.string.error_min_username_length)
            return
        }

        if (password.isEmpty()) {
            registerBinding.edtPassword.error = getString(R.string.error_empty_password)
            return
        }

        if (password.trim().length < 6) {
            registerBinding.edtPassword.error = getString(R.string.error_min_password_length)
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

                    registerBinding.edtName.setText("")
                    registerBinding.edtEmail.setText("")
                    registerBinding.edtUsername.setText("")
                    registerBinding.edtPassword.setText("")

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
                AppInstance.globalHelper.logMsg(
                    "Exception: ${e.message}",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "RegisterActivity"
                )
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