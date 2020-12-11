package net.igorilic.didyoubuyit.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject

class ListActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var list: ListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        if (intent.extras?.containsKey("list") != true) {
            AppInstance.globalHelper.notifyMSG(resources.getString(R.string.error_list_loading_failed))
            finish()
            return
        }

        list = ListModel.fromJSON(JSONObject(this.intent.extras?.getString("list")!!))

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }
}