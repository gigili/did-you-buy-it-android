package net.igorilic.didyoubuyit.list.ui.users

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import net.igorilic.didyoubuyit.R
import net.igorilic.didyoubuyit.helper.AppInstance
import net.igorilic.didyoubuyit.helper.GlobalHelper
import net.igorilic.didyoubuyit.model.ListModel
import net.igorilic.didyoubuyit.model.UserModel

class ListUserAdapter(
    private val context: Context,
    private val list: ListModel,
    private val mInterface: ListUserAdapterInterface,
    private val usedForFilter: Boolean = false
) : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {
    private val values = ArrayList<UserModel>()

    interface ListUserAdapterInterface {
        fun onLongItemClick(view: View, item: UserModel, position: Int)
        fun onListUserImageEnlarge(user: UserModel, imageUrl: String)
        fun onItemClick(item: UserModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_list_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        holder.lblListUserName.text = item.name
        holder.lblListUserEmail.text = if (usedForFilter) item.username else item.email

        if (!item.image.isNullOrEmpty()) {
            val imageUrl =
                "${AppInstance.globalHelper.getStringPref("API_URL")}/${GlobalHelper.PROFILE_IMAGE_PATH}/${item.image}"
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.imgListUserImage)

            holder.imgListUserImage.visibility = View.VISIBLE
            holder.imgListUserEnlarge.visibility = View.VISIBLE

            holder.imgListUserImage.setOnClickListener {
                mInterface.onListUserImageEnlarge(item, imageUrl)
            }

            holder.imgListUserEnlarge.setOnClickListener {
                mInterface.onListUserImageEnlarge(item, imageUrl)
            }
        } else {
            holder.imgListUserImage.visibility = View.GONE
            holder.imgListUserEnlarge.visibility = View.GONE
        }

        if (list.userID == item.id) {
            holder.lblListUserInfo.visibility = View.VISIBLE
        } else {
            holder.lblListUserInfo.visibility = View.GONE
        }

        holder.lytCardListUser.setOnLongClickListener {
            mInterface.onLongItemClick(it, item, position)
            true
        }

        holder.lytCardListUser.setOnClickListener {
            AppInstance.globalHelper.logMsg("onClick triggered")
            mInterface.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = values.size

    fun removeItem(position: Int) {
        values.removeAt(position)
    }

    fun setData(data: java.util.ArrayList<UserModel>) {
        values.clear()
        values.addAll(data)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lytCardListUser: RelativeLayout = view.findViewById(R.id.lytCardListUser)
        val imgListUserImage: ImageView = view.findViewById(R.id.imgListUserImage)
        val imgListUserEnlarge: ImageView = view.findViewById(R.id.imgListUserEnlarge)
        val lblListUserName: TextView = view.findViewById(R.id.lblListUserName)
        val lblListUserEmail: TextView = view.findViewById(R.id.lblListUserEmail)
        val lblListUserInfo: TextView = view.findViewById(R.id.lblListUserInfo)
    }
}