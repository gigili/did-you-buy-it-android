package net.igorilic.didyoubuyit.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.igorilic.didyoubuyit.db.entity.ListEntity

@Dao
interface ListDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(list: ListEntity)

    @Update()
    suspend fun update(list: ListEntity)

    @Delete()
    suspend fun delete(list: ListEntity)

    @Query("SELECT * FROM list WHERE id = :id")
    fun getList(id: Int): ListEntity

    @Query("SELECT * FROM list")
    fun getLists(): Flow<List<ListEntity>>

}