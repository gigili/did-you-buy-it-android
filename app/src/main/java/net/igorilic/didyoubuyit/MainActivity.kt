package net.igorilic.didyoubuyit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.igorilic.didyoubuyit.databinding.ActivityMainBinding
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.list.ListActivity
import net.igorilic.didyoubuyit.list.ListViewModel
import net.igorilic.didyoubuyit.list.ListsAdapter
import net.igorilic.didyoubuyit.model.ListModel


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

        val viewModel = ViewModelProvider(this@MainActivity).get(ListViewModel::class.java)
        ProgressDialogHelper.showProgressDialog(this@MainActivity)
        viewModel.getLists().observe(this@MainActivity, {
            ProgressDialogHelper.hideProgressDialog()
            listsAdapter.setData(it)
            listsAdapter.notifyDataSetChanged()
        })

        viewModel.getErrorMessages().observe(this@MainActivity, {
            globalHelper.showMessageDialog(it)
        })

        lstLists = findViewById(R.id.lstLists)
        listsAdapter = ListsAdapter(
            this@MainActivity,
            ArrayList(),
            object : ListsAdapter.OnListItemClickListener {
                override fun onItemClicked(item: ListModel) {
                    openListDetails(item)
                }
            })
        lstLists.layoutManager = LinearLayoutManager(this@MainActivity)
        lstLists.adapter = listsAdapter

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            var newListItemFormView: View? = layoutInflater.inflate(R.layout.dialog_list_form, null, false)
            val ad = AlertDialog
                .Builder(this@MainActivity)
                .setTitle(R.string.add_new_list)
                .setView(newListItemFormView)
                .setNegativeButton(R.string.cancel) { it, _ ->
                    it.dismiss()
                }
                .setPositiveButton(R.string.save) { it, _ ->
                    it.dismiss()
                    //
                    newListItemFormView?.let{
                        val edtListName = it.findViewById<EditText>(R.id.edtListName)
                        viewModel.addNewList(edtListName.text.toString())
                        Snackbar.make(
                            view,
                            String.format(
                                getString(R.string.lbl_new_list_created),
                                edtListName.text.toString()
                            ),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                .setCancelable(true)
                .setOnDismissListener {
                    newListItemFormView = null
                }
                .create()

            ad.show()
        }

        //loadLists()
    }

    private fun openListDetails(list: ListModel) {
        val intentListActivity = Intent(this@MainActivity, ListActivity::class.java)
        intentListActivity.putExtra("list", list.toJSONString())
        startActivity(intentListActivity)
        //finish()
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