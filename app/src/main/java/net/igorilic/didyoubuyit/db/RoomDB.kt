package net.igorilic.didyoubuyit.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import net.igorilic.didyoubuyit.db.dao.ListDao
import net.igorilic.didyoubuyit.db.entity.ListEntity

@Database(entities = [ListEntity::class], version = 1, exportSchema = false)
abstract class RoomDB : RoomDatabase() {

    abstract fun listDao(): ListDao

    companion object {
        @Volatile
        private var INSTANCE: RoomDB? = null

        fun getDatabase(context: Context, scope: CoroutineScope): RoomDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java,
                    "did_you_buy_it"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}