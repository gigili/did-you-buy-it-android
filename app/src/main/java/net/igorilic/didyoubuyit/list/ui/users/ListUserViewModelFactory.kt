package net.igorilic.didyoubuyit.list.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ListUserViewModelFactory(private val listID: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ListUserViewModel::class.java)) {
            ListUserViewModel(this.listID) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}