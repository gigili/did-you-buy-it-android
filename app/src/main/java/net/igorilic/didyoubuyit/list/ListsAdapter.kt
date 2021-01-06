package net.igorilic.didyoubuyit.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.model.ListModel

class ListsAdapter(
    private val context: Context,
    private var items: ArrayList<ListModel>?,
    private val mListItemClickListener: OnListItemClickListener
) :
    RecyclerView.Adapter<ListsAdapter.ViewHolder>() {

    interface OnListItemClickListener {
        fun onItemClicked(item: ListModel)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var lytCardList: LinearLayout = view.findViewById(R.id.lytCardList)
        var lblListName: TextView = view.findViewById(R.id.lblListName)
        var lblListItemsCount: TextView = view.findViewById(R.id.lblListItemsCount)
        var lblListUsersCount: TextView = view.findViewById(R.id.lblListUsersCount)
        var lblListCreatedAt: TextView = view.findViewById(R.id.lblListCreatedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_list, parent, false)

        AppInstance.globalHelper.logMsg("Items: $items")

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.lblListName.text = item.name
        holder.lblListItemsCount.text = String.format(
            context.resources.getString(R.string.lbl_list_items_count),
            item.cntBoughtItems,
            item.cntItems
        )
        holder.lblListUsersCount.text = String.format(
            context.resources.getString(R.string.lbl_list_users_count),
            item.cntUsers
        )
        holder.lblListCreatedAt.text = AppInstance.globalHelper.formatDate(
            item.createdAt!!,
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'"
        )

        holder.lytCardList.setOnClickListener {
            mListItemClickListener.onItemClicked(item)
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.id?.toLong() ?: 0L
    }

    private fun getItem(position: Int): ListModel {
        return items?.get(position) ?: ListModel()
    }

    fun setData(newItems: ArrayList<ListModel>){
        items = newItems
    }
}