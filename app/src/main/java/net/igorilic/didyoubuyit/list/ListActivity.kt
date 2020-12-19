package net.igorilic.didyoubuyit.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject

class ListActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var list: ListModel
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        navView = findViewById(R.id.nav_view)

        if (intent.extras?.containsKey("list") != true) {
            AppInstance.globalHelper.notifyMSG(resources.getString(R.string.error_list_loading_failed))
            finish()
            return
        }

        list = ListModel.fromJSON(JSONObject(this.intent.extras?.getString("list")!!))

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        //navView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController)

        val bundle = bundleOf("list" to list.toJSONString())
        findNavController(R.id.nav_host_fragment).navigate(R.id.m_nav_items, bundle)

        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bn_nav_items -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.m_nav_items, bundle)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.bn_nav_users -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.m_nav_users, bundle)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        supportActionBar?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this,
                R.color.action_bar_color
            )
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navigateUpResult = NavigationUI.navigateUp(navController, null)
        navView.selectedItemId = R.id.bn_nav_items
        return navigateUpResult
    }

    override fun onBackPressed() {
        if (navView.selectedItemId == R.id.bn_nav_items) {
            finish()
        } else {
            onSupportNavigateUp()
        }
    }
}