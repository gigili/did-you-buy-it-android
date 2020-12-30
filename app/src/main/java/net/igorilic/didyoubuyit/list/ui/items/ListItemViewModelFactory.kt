package net.igorilic.didyoubuyit.list.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ListItemViewModelFactory(private val listID: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ListItemViewModel::class.java)) {
            ListItemViewModel(this.listID) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}