package net.igorilic.didyoubuyit.list.ui.form

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.model.ListModel

class ListUserFormFragment : Fragment(R.layout.fragment_list_user_form) {
    private lateinit var list: ListModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list = AppInstance.gson.fromJson(arguments?.getString("list")!!, ListModel::class.java)
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.add_new_user_to_list)
    }
}