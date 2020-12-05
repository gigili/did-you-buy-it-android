package net.igorilic.didyoubuyit.db.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import net.igorilic.didyoubuyit.db.dao.ListDao
import net.igorilic.didyoubuyit.db.entity.ListEntity

class ListRepository(private val listDao: ListDao) {
    val allLists: Flow<List<ListEntity>> = listDao.getLists()

    @WorkerThread
    suspend fun insert(list: ListEntity) {
        listDao.insert(list)
    }
}