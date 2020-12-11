package net.igorilic.didyoubuyit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import net.igorilic.didyoubuyit.adapters.ListsAdapter
import net.igorilic.didyoubuyit.databinding.ActivityMainBinding
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var globalHelper: GlobalHelper

    private lateinit var lstLists: RecyclerView
    private lateinit var listsAdapter: ListsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        globalHelper = GlobalHelper(this@MainActivity)
        globalHelper.setupDrawerLayout(toolbar)

        lstLists = findViewById(R.id.lstLists)
        listsAdapter = ListsAdapter(this@MainActivity, ArrayList())
        lstLists.layoutManager = LinearLayoutManager(this@MainActivity)
        lstLists.adapter = listsAdapter

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        loadLists()
    }

    private fun loadLists() {
        AppInstance.app.callAPI("/list", null, {
            try {
                val res = JSONObject(it)

                //TODO Store lists into DB

                val listsListType = object : TypeToken<ArrayList<ListModel?>?>() {}.type
                val lists: ArrayList<ListModel> = AppInstance.gson.fromJson(
                    res.getJSONArray("data").toString(),
                    listsListType
                )

                listsAdapter.addNewItems(lists)
                listsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                AppInstance.globalHelper.logMsg("[ERROR][MainActivity loadLists] Exception: ${e.message}")
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    getString(R.string.error_list_loading_failed),
                    "MainActivity"
                )
            )
        }, Request.Method.GET, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        globalHelper.handleOptionsMenuClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        globalHelper.quitApp(false)
        //super.onBackPressed()
    }

    override fun onDestroy() {
        AppInstance.app.cancelPendingRequests()
        ProgressDialogHelper.hideProgressDialog()
        super.onDestroy()
    }
}