package net.igorilic.didyoubuyit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.igorilic.didyoubuyit.helpers.AppInstance
import net.igorilic.didyoubuyit.helpers.GlobalHelper
import net.igorilic.didyoubuyit.helpers.ProgressDialogHelper

class MainActivity : AppCompatActivity() {

    private lateinit var globalHelper: GlobalHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        globalHelper = GlobalHelper(this@MainActivity)
        globalHelper.setupDrawerLayout(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        globalHelper.handleOptionsMenuClick(item.itemId, this)
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        globalHelper.quitApp(false)
        super.onBackPressed()
    }

    override fun onDestroy() {
        AppInstance.app.cancelPendingRequests()
        ProgressDialogHelper.hideProgressDialog()
        super.onDestroy()
    }
}