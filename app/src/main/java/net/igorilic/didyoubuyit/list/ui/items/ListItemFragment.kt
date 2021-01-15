package net.igorilic.didyoubuyit.list.ui.items

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.GlobalHelper.Companion.EditMode
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListItemModel
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.utility.ImageViewActivity
import org.json.JSONObject


class ListItemFragment : Fragment(R.layout.fragment_list_item) {
    private lateinit var ctx: Context
    private lateinit var globalHelper: GlobalHelper
    private lateinit var list: ListModel
    private lateinit var adapter: ListItemAdapter
    private lateinit var viewModel: ListItemViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ctx = requireContext()
        if (arguments?.getString("list").isNullOrEmpty()) {
            return
        }

        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))
        globalHelper = GlobalHelper(requireContext())

        initializeList(view)

        val listItemViewModelFactory = ListItemViewModelFactory(list.id!!)
        viewModel = ViewModelProvider(
            requireActivity(),
            listItemViewModelFactory
        ).get(ListItemViewModel::class.java)

        viewModel.getLisItems().observe(requireActivity(), {
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.getErrorMessage().observe(requireActivity(), {
            if (!it.isNullOrBlank()) {
                globalHelper.showMessageDialog(it)
            }
        })

        viewModel.getShowProgressDialog().observe(requireActivity(), {
            if (it) {
                ProgressDialogHelper.showProgressDialog(requireActivity())
            } else {
                ProgressDialogHelper.hideProgressDialog()
            }
        })

        viewModel.getNotifyMessage().observe(requireActivity(), {
            if (!it.isNullOrBlank()) {
                Snackbar.make(view.findViewById(R.id.btnAddNewListItem), it, Snackbar.LENGTH_LONG)
                    .show()
            }
        })

        (activity as AppCompatActivity).supportActionBar?.title = String.format(
            getString(R.string.title_2_columns),
            list.name,
            getString(R.string.lbl_list_items)
        )

        val fabNewListItem = view.findViewById<FloatingActionButton>(R.id.btnAddNewListItem)
        fabNewListItem.setOnClickListener {
            showListItemDialog(EditMode.New)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("InflateParams")
    private fun showListItemDialog(
        editMode: EditMode,
        item: ListItemModel? = null,
        position: Int = -1
    ) {
        val bundle = bundleOf(
            "list" to list.toJSONString(),
            "item" to item?.toJSON().toString(),
            "position" to position.toString(),
            "editMode" to editMode
        )

        findNavController().navigate(R.id.m_nav_items_form, bundle)
    }

    private fun initializeList(view: View) {
        adapter = ListItemAdapter(
            ArrayList(),
            requireActivity(),
            object : ListItemAdapter.ListItemInterface {
                override fun onItemBoughtChangeState(
                    position: Int,
                    item: ListItemModel,
                    isChecked: Boolean
                ) {
                    viewModel.changeItemBoughtState(item, position)
                }

                override fun onItemLongClick(view: View, position: Int, item: ListItemModel) {
                    showContextMenu(view, position, item)
                }

                override fun onListItemImageEnlarge(item: ListItemModel, imageUrl: String) {
                    val fullScreenView = Intent(ctx, ImageViewActivity::class.java)
                    fullScreenView.putExtra("imageUrl", imageUrl)
                    fullScreenView.putExtra("imageDescription", item.name)
                    startActivity(fullScreenView)
                }
            })
        val lst = view.findViewById<RecyclerView>(R.id.lstListItems)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = adapter
    }

    private fun showContextMenu(view: View, position: Int, item: ListItemModel) {
        val pop = PopupMenu(ctx, view)
        pop.inflate(R.menu.list_item_menu)

        pop.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acListItemEdit -> showListItemDialog(EditMode.Edit, item, position)
                R.id.acListItemDelete -> {
                    globalHelper.showMessageDialog(
                        String.format(
                            ctx.resources.getString(R.string.action_confirm_list_item_delete),
                            item.name
                        ),
                        ctx.resources.getString(R.string.lbl_confirm_action),
                        hasNegativeButton = true
                    ) {
                        //deleteListItem(item, position)
                        viewModel.deleteListItem(item, position)
                    }
                }
            }
            true
        }

        pop.show()
    }
}