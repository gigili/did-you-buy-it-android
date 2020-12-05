package net.igorilic.didyoubuyit.db.view_model

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.igorilic.didyoubuyit.db.entity.ListEntity
import net.igorilic.didyoubuyit.db.repository.ListRepository

class ListViewModel(private val repository: ListRepository) : ViewModel() {
    val allLists: LiveData<List<ListEntity>> = repository.allLists.asLiveData()

    fun insert(list: ListEntity) = viewModelScope.launch {
        repository.insert(list)
    }
}

class ListViewModelFactory(private val repository: ListRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}