package net.igorilic.didyoubuyit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import net.igorilic.didyoubuyit.helpers.AppInstance
import net.igorilic.didyoubuyit.helpers.GlobalHelper
import net.igorilic.didyoubuyit.helpers.ProgressDialogHelper

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var globalHelper: GlobalHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        globalHelper = GlobalHelper(this@MainActivity)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        setupDrawerLayout(toolbar)
    }

    private fun setupDrawerLayout(toolbar: Toolbar) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color =
            ContextCompat.getColor(baseContext, R.color.colorIconPrimary)
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

    override fun onDestroy() {
        AppInstance.app.cancelPendingRequests()
        ProgressDialogHelper.hideProgressDialog()
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        globalHelper.handleNavigationDrawerItemClick(item.itemId)
        return true
    }
}