package net.igorilic.didyoubuyit.list.ui.form

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.ProgressDialogHelper
import net.igorilic.didyoubuyit.list.ui.users.ListUserAdapter
import net.igorilic.didyoubuyit.list.ui.users.ListUserViewModel
import net.igorilic.didyoubuyit.list.ui.users.ListUserViewModelFactory
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.model.UserModel

class ListUserFormFragment : Fragment(R.layout.fragment_list_user_form) {
    private lateinit var list: ListModel
    private lateinit var viewModel: ListUserViewModel
    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = AppInstance.gson.fromJson(arguments?.getString("list")!!, ListModel::class.java)
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.add_new_user_to_list)

        val listUserViewModelFactory = ListUserViewModelFactory(list.id!!)
        viewModel = ViewModelProvider(
            requireActivity(),
            listUserViewModelFactory
        ).get(ListUserViewModel::class.java)

        val edtUserFilter = view.findViewById<EditText>(R.id.edtUserFilter)
        edtUserFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
        })

        val lst = view.findViewById<RecyclerView>(R.id.lstFilteredUsers)
        lst.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ListUserAdapter(
                context,
                list,
                object : ListUserAdapter.ListUserAdapterInterface {
                    override fun onLongItemClick(view: View, item: UserModel, position: Int) {}
                    override fun onListUserImageEnlarge(user: UserModel, imageUrl: String) {}
                    override fun onItemClick(item: UserModel) {
                        addUserToList(item)
                    }
                },
                true
            )
        }

        setupObservables(view, lst)
    }

    private fun setupObservables(view: View, lst: RecyclerView) {
        viewModel.getErrorMessage().observe(requireActivity(), {
            if (!it.isNullOrBlank()) {
                AppInstance.globalHelper.showMessageDialog(it)
            }
        })

        viewModel.getNotifyMessage().observe(requireActivity(), {
            if (!it.isNullOrBlank()) {
                Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
            }
        })

        viewModel.getFilteredUsers().observe(requireActivity(), { data ->
            AppInstance.globalHelper.logMsg("Filtered data: $data")
            val adapter = lst.adapter as ListUserAdapter
            adapter.setData(data)
            adapter.notifyDataSetChanged()
        })
    }

    private fun filterUsers(query: String) {
        job?.cancel()

        if (query.isBlank() || query.length < 3) {
            viewModel.clearFilteredUsers()
            return
        }

        job = GlobalScope.launch {
            delay(750L)
            if (::viewModel.isInitialized) {
                viewModel.filterUsers(query)
            }
        }

        job?.start()
    }

    private fun addUserToList(user: UserModel) {
        val ctx = requireContext()
        AlertDialog.Builder(ctx)
            .setTitle(ctx.getString(R.string.lbl_confirm_action))
            .setMessage(
                String.format(
                    ctx.getString(R.string.lbl_confirm_add_user_to_list),
                    "${user.name} (${user.username})"
                )
            )
            .setNegativeButton(ctx.getString(R.string.no)) { dl, _ ->
                dl.dismiss()
            }
            .setPositiveButton(ctx.getString(R.string.yes)) { dl, _ ->
                dl.dismiss()
                viewModel.getNotifyMessage().removeObservers(requireActivity())
                viewModel.getErrorMessage().removeObservers(requireActivity())
                viewModel.getFilteredUsers().removeObservers(requireActivity())
                viewModel.addUserToList(list.id, user.id)
                ProgressDialogHelper.hideProgressDialog()
                findNavController().navigateUp()
            }
            .show()
    }
}