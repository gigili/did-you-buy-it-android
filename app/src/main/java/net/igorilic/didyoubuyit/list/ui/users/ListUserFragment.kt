package net.igorilic.didyoubuyit.list.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.igorilic.didyoubuyit.R

class ListUserFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_user, container, false)

        val lst = view.findViewById<RecyclerView>(R.id.lstListUsers)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = ListUserAdapter(ArrayList())

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListUserFragment()
    }
}