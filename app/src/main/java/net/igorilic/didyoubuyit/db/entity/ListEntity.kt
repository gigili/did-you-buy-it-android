package net.igorilic.didyoubuyit.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list")
data class ListEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "userID")
    val userID: Int,

    @ColumnInfo(name = "created_at")
    val created_at: String
)