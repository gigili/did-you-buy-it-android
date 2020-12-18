package net.igorilic.didyoubuyit.list.ui.items

import android.app.Activity
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.model.ListItemModel

class ListItemAdapter(
    private val values: ArrayList<ListItemModel>,
    private val context: Activity,
    private val mListItemInterface: ListItemInterface
) : RecyclerView.Adapter<ListItemAdapter.ViewHolder>() {

    interface ListItemInterface {
        fun onItemBoughtChangeState(position: Int, item: ListItemModel, isChecked: Boolean)
        fun onItemLongClick(view: View, position: Int, item: ListItemModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.lblListItemName.text = item.name

        if (!item.image.isNullOrEmpty()) {
            holder.imgListItemImage.visibility = View.VISIBLE
            holder.imgListItemEnlarge.visibility = View.VISIBLE
            val imageUrl =
                "${AppInstance.globalHelper.getStringPref("API_URL")}/${GlobalHelper.LIST_ITEM_IMAGE_PATH}/${item.image}"
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.imgListItemImage)
        }

        if (item.isRepeating == "1") {
            holder.lblListItemRepeating.visibility = View.VISIBLE
        }

        if (item.purchaseDate != null && item.purchasedUserID != null) {
            holder.lblListItemPurchaseInfo.visibility = View.VISIBLE
            holder.lblListItemPurchaseInfo.text = String.format(
                context.resources.getString(R.string.lbl_list_item_purchase_date),
                AppInstance.globalHelper.formatDate(
                    item.purchaseDate,
                    "yyyy-MM-dd'T'HH:mm:ss.S'Z'"
                ),
                item.purchasedUserID!!.name
            )
            holder.cbBuyItem.isChecked = true
        }
        setupPaintFlags(holder, item, item.purchasedUserID != null)

        holder.cbBuyItem.setOnClickListener {
            val isChecked = holder.cbBuyItem.isChecked
            setupPaintFlags(holder, item, isChecked)

            holder.lblListItemPurchaseInfo.visibility =
                if (isChecked) View.VISIBLE else View.GONE

            mListItemInterface.onItemBoughtChangeState(position, item, isChecked)
        }

        holder.lytCardListItem.setOnLongClickListener {
            mListItemInterface.onItemLongClick(it, position, item)
            true
        }
    }

    private fun setupPaintFlags(holder: ViewHolder, item: ListItemModel, isChecked: Boolean) {
        if (item.purchasedUserID != null && isChecked) {
            holder.lblListItemName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.lblListItemName.paintFlags = Paint.LINEAR_TEXT_FLAG
        }
    }

    override fun getItemCount(): Int = values.size

    fun addItems(items: ArrayList<ListItemModel>) {
        values.addAll(items)
    }

    fun updateItem(position: Int, mItem: ListItemModel) {
        values[position] = mItem
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lytCardListItem: RelativeLayout = view.findViewById(R.id.lytCardListItem)
        val cbBuyItem: CheckBox = view.findViewById(R.id.cbBuyItem)
        val lblListItemName: TextView = view.findViewById(R.id.lblListItemName)
        val lblListItemPurchaseInfo: TextView = view.findViewById(R.id.lblListItemPurchaseInfo)
        val imgListItemImage: ImageView = view.findViewById(R.id.imgListItemImage)
        val imgListItemEnlarge: ImageView = view.findViewById(R.id.imgListItemEnlarge)
        val lblListItemRepeating: TextView = view.findViewById(R.id.lblListItemRepeating)
    }
}