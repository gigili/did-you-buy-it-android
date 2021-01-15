package net.igorilic.didyoubuyit.list.ui.users

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
import com.android.volley.Request
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.model.UserModel
import net.igorilic.didyoubuyit.utility.ImageViewActivity
import org.json.JSONObject

class ListUserFragment : Fragment(R.layout.fragment_list_user) {
    private lateinit var list: ListModel
    private lateinit var users: ArrayList<UserModel>
    private lateinit var globalHelper: GlobalHelper
    private lateinit var adapter: ListUserAdapter
    private lateinit var viewModel: ListUserViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))
        (activity as AppCompatActivity).supportActionBar?.title = String.format(
            getString(R.string.title_2_columns),
            list.name,
            getString(R.string.lbl_list_users)
        )

        if (arguments?.getString("list").isNullOrEmpty()) {
            return
        }

        val listUserViewModelFactory = ListUserViewModelFactory(list.id!!)
        viewModel = ViewModelProvider(
            requireActivity(),
            listUserViewModelFactory
        ).get(ListUserViewModel::class.java)

        globalHelper = GlobalHelper(requireContext())

        users = ArrayList()
        adapter = ListUserAdapter(
            requireContext(),
            list,
            object : ListUserAdapter.ListUserAdapterInterface {
                override fun onLongItemClick(view: View, item: UserModel, position: Int) {
                    if (list.userID == globalHelper.getIntPref("user_id")) {
                        showContextMenu(view, position, item)
                    }
                }

                override fun onListUserImageEnlarge(user: UserModel, imageUrl: String) {
                    val fullScreenImage = Intent(requireContext(), ImageViewActivity::class.java)
                    fullScreenImage.putExtra("imageUrl", imageUrl)
                    startActivity(fullScreenImage)
                }
            })

        val lst = view.findViewById<RecyclerView>(R.id.lstListUsers)
        lst.layoutManager = LinearLayoutManager(requireContext())
        lst.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.btnAddNewListUser).setOnClickListener {
            val bundle = bundleOf(
                "list" to list.toJSONString()
            )
            findNavController().navigate(R.id.m_nav_users_form, bundle)
        }

        setupObservers(view, adapter)
    }

    private fun setupObservers(view: View, adapter: ListUserAdapter) {
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
                Snackbar.make(view.findViewById(R.id.btnAddNewListUser), it, Snackbar.LENGTH_LONG)
                    .show()
            }
        })

        viewModel.getListUsers().observe(requireActivity(), {
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun showContextMenu(view: View, position: Int, item: UserModel) {
        val pop = PopupMenu(requireContext(), view)
        pop.inflate(R.menu.list_user_menu)

        pop.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acListUserEdit -> AppInstance.globalHelper.notifyMSG("Edit item: ${item.name}")
                R.id.acListUserDelete -> {
                    globalHelper.showMessageDialog(
                        String.format(
                            requireContext().getString(R.string.action_confirm_user_delete),
                            item.name
                        ), requireContext().getString(R.string.lbl_confirm_action),
                        true
                    ) {
                        deleteListUser(position, item)
                    }
                }
            }
            true
        }

        pop.show()
    }

    private fun deleteListUser(position: Int, item: UserModel) {
        ProgressDialogHelper.showProgressDialog(requireActivity())
        AppInstance.app.callAPI("/list/${list.id}/users/${item.id}", null, {
            try {
                val res = JSONObject(it)
                if (res.optBoolean("success", false)) {
                    adapter.removeItem(position)
                    adapter.notifyDataSetChanged()
                } else {
                    throw Exception(requireContext().getString(R.string.error_deleting_list_user))
                }
            } catch (e: Exception) {
                globalHelper.logMsg(
                    e.message ?: "Failed to parse response",
                    GlobalHelper.Companion.LogLevelTypes.Error,
                    "ListUserFragment@deleteListUser"
                )
            } finally {
                ProgressDialogHelper.hideProgressDialog()
            }
        }, {
            ProgressDialogHelper.hideProgressDialog()
            globalHelper.showMessageDialog(
                globalHelper.parseErrorNetworkResponse(
                    it,
                    requireContext().getString(R.string.error_deleting_list_user),
                    "ListUserFragment@deleteListUser"
                )
            )
        }, Request.Method.DELETE, true)
    }
}
