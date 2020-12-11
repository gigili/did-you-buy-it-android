package net.igorilic.didyoubuyit.list.ui.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.igorilic.didyoubuyit.R

class ListItemFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_item, container, false)

        val lst = view.findViewById<RecyclerView>(R.id.lstListItems)
        lst.layoutManager = LinearLayoutManager(context)
        lst.adapter = ListItemAdapter(ArrayList())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.title = "Example 1"
        super.onViewCreated(view, savedInstanceState)
    }
}