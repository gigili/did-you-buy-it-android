package net.igorilic.didyoubuyit.list.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.model.ListModel
import org.json.JSONObject

class ListUserFragment : Fragment() {
    private lateinit var list: ListModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (arguments?.getString("list").isNullOrEmpty()) {
            return null
        }

        list = ListModel.fromJSON(JSONObject(arguments?.getString("list")!!))

        val view = inflater.inflate(R.layout.fragment_list_user, container, false)

        val lst = view.findViewById<RecyclerView>(R.id.lstListUsers)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = ListUserAdapter(ArrayList())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.title = "${list.name}"
        super.onViewCreated(view, savedInstanceState)
    }
}